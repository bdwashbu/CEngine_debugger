package controllers

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import akka.stream.ActorMaterializer
import javax.inject._
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit
import play.api.libs.streams.ActorFlow
import play.api.mvc.Results.Ok
import play.api.mvc._

import scala.c_engine.{AstUtils, Utils}
import scala.concurrent.{ExecutionContext, Future}

/**
 * This controller creates an `Action` to handle HTTP requests to the
 * application's home page.
 */
@Singleton
class HomeController @Inject()(cc: ControllerComponents)(implicit assetsFinder: AssetsFinder, ec: ExecutionContext)
  extends AbstractController(cc) {

  var tUnit: IASTTranslationUnit = null

  def index = Action {
    val f = new java.io.File(s"./public/main.html")
    Ok(scala.io.Source.fromFile(f.getCanonicalPath()).mkString).as("text/html");
  }

  def favicon = Action {
    Ok.sendFile(new java.io.File(s"./public/img/favicon.png"))
  }

  def javascript(file: String) = Action {
    Ok.sendFile(new java.io.File(s"./public/js/$file"))
  }

  def css(file: String) = Action {
    Ok.sendFile(new java.io.File(s"./public/css/$file"))
  }

  def getAst(code: String, width: Int, height: Int) = Action {
    println("GETTING AST")
    println(code)
    tUnit = Utils.getTranslationUnit(Seq(code))
    Ok(AstUtils.getAllChildren(tUnit))
  }

  //val app = new AkkaWebSockets()
  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()

  def websocket = WebSocket.accept[String, String] { request =>
    ActorFlow.actorRef { out =>
      MyWebSocketActor.props(out)
    }
  }

  object MyWebSocketActor {
    def props(out: ActorRef) = Props(new MyWebSocketActor(out))
  }

  class MyWebSocketActor(out: ActorRef) extends Actor {
    def receive = {
      case msg: String =>
        if (msg == "Step") {
          //Executor.tick(state)
          //out ! ("Step Response:" + state.current.hashCode)
        } else if (msg.startsWith("Get Node Class Name:")) {
          val id = msg.split(":").last.trim.toInt

          val node = Utils.getDescendants(tUnit).find{x => x.hashCode == id}.get

          out ! ("Current Node Class:" + node.getClass.getSimpleName)
        } else {
          out ! ("Unexpected request: " + msg)
        }

    }
  }



}
