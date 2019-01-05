package scala.c_engine

import java.io.File
import java.util.HashMap

import org.eclipse.cdt.core.dom.ast.{IASTNode, _}
import org.eclipse.cdt.core.dom.ast.gnu.c.GCCLanguage
import org.eclipse.cdt.core.parser.{DefaultLogService, FileContent, IncludeFileContentProvider, ScannerInfo}
import scala.collection.mutable.ListBuffer
import org.eclipse.cdt.core.dom.ast.IASTBinaryExpression._
//import org.anarres.cpp.Preprocessor
import java.nio.charset.StandardCharsets
//import org.anarres.cpp.InputLexerSource
import java.io.ByteArrayInputStream
//import org.anarres.cpp.Token
import better.files._
import org.eclipse.cdt.internal.core.dom.parser.c.CBasicType

sealed abstract class Direction
object Stage1 extends Direction
object Stage2 extends Direction
object Stage3 extends Direction
object PreLoop extends Direction
object Exiting extends Direction
object Gotoing extends Direction

object Utils {
  
  def stripQuotes(str: String): String = {
    str.tail.reverse.tail.reverse
  }

  def getAncestors(node: IASTNode): Seq[IASTNode] = {
    var current = node.getParent
    val results = new ListBuffer[IASTNode]()
    while (current != null) {
      results += current
      current = current.getParent
    }
    results
  }
  
  def isAssignment(op: Int) = {
      op == op_assign ||
      op == op_plusAssign ||
      op == op_minusAssign ||
      op == op_multiplyAssign ||
      op == op_divideAssign ||
      op == op_moduloAssign ||
      op == op_binaryXorAssign ||
      op == op_binaryAndAssign ||
      op == op_binaryOrAssign ||
      op == op_multiplyAssign ||
      op == op_shiftLeftAssign ||
      op == op_shiftRightAssign
    }

  def getDescendants(node: IASTNode): Seq[IASTNode] = {
    Seq(node) ++ node.getChildren.flatMap{x => getDescendants(x)}
  }

  val mainPath = raw"."
  val mainAdditionalPath = raw"./tests/scala/c/engine/libds-master"
  val minGWIncludes = raw"C:\MinGW\include"

  val minGWAdditionalIncludes = if (new File(raw"C:\MinGW\lib\gcc\mingw32\5.3.0\include").exists) {
		raw"C:\MinGW\lib\gcc\mingw32\5.3.0\include"
	} else {
		raw"C:\MinGW\lib\gcc\mingw32\4.6.2\include"
	}

  val minGWMoreIncludes = raw"C:\MinGW\include\GL"
  
  def getTranslationUnit(codes: Seq[String]): IASTTranslationUnit = {

		val preprocessResults = new StringBuilder
		
		val newCodes = codes///List(better.files.File("app\\ee_printf.c").contentAsString) ++ codes
		
		newCodes.map{theCode =>
		  
		  var lines = if (theCode != newCodes.head) {
		    theCode.split("\\r?\\n").toList
		  } else {
		    theCode.split("\\r?\\n").toList
		  }
		  
		  // solution to deal with var args
		  val linesWithInclude = lines.zipWithIndex.filter{case (line, index) => line.contains("#include")}
		  val lastInclude = linesWithInclude.reverse.headOption.map{case (line, index) => index + 1}.getOrElse(-1)
		  if (lastInclude != -1) {
		    lines = lines.take(lastInclude) ++
           // eclipse cdt cant handle function string args that aren't in quotes
		       List("#define va_arg(x,y) va_arg(x, #y)\n") ++ 
		       List("#define va_start(x,y) va_start(&x, &y)\n") ++ 
		       List("#define va_end(x) va_end(x)\n") ++
           List("#define __builtin_offsetof(x, y) offsetof(#x, #y)") ++
          lines.drop(lastInclude)
		  }

      preprocessResults.append(lines.reduce{_ + "\n" + _})

//		  val	pp = new Preprocessor();
//
//  		pp.getSystemIncludePath.add(minGWIncludes)
//  		pp.getSystemIncludePath.add(minGWAdditionalIncludes)
//  		pp.addMacro("__cdecl", "")
//  		pp.getQuoteIncludePath.add(minGWIncludes)
//  		pp.getQuoteIncludePath.add(mainAdditionalPath)
  		//pp.addMacro("ALLOC_TESTING");

//  		val stream = new ByteArrayInputStream(code.getBytes(StandardCharsets.UTF_8))
//
//  		pp.addInput(new InputLexerSource(stream))
//
//  		var shouldBreak = false
//  		var skipline = false
//  		var startLine = 0
//  		var currentLine = 0
//
//  		while (!shouldBreak) {
//  		  try {
//  				var	tok = pp.token
//  				currentLine = tok.getLine
//
//  				while (skipline && currentLine == startLine) {
//  				  tok = pp.token
//  				  currentLine = tok.getLine
//  				}
//  				skipline = false
//
//  				if (tok == null)
//  					shouldBreak = true
//
//  				if (!shouldBreak && tok.getType == Token.EOF)
//  					shouldBreak = true
//
//  				if (!shouldBreak) {
//  				  preprocessResults ++= tok.getText
//  				}
//  		  } catch {
//  		    case e => skipline = true; startLine = currentLine + 1
//  		  }
//  			}
		}
		
		//val preprocess = preprocessResults.toString.replaceAll("(?m)(^ *| +(?= |$))", "").replaceAll("(?m)^$([\r\n]+?)(^$[\r\n]+?^)+", "$1")

		//better.files.File("what.txt").write(code)

    val fileContent = FileContent.create("test", preprocessResults.toString.toCharArray)
    val symbolMap = new HashMap[String, String];

    val systemIncludes = List(new File(mainPath), new File(minGWIncludes), new File(minGWMoreIncludes), new File(minGWAdditionalIncludes))

    val info = new ScannerInfo(symbolMap, systemIncludes.toArray.map(_.getAbsolutePath))
    val log = new DefaultLogService()
    val opts = 8
    val includes = IncludeFileContentProvider.getEmptyFilesProvider

    val tUnit = GCCLanguage.getDefault().getASTTranslationUnit(fileContent, info, includes, null, opts, log)

    tUnit
  }
}
