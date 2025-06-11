package jsenv.playwright

import cats.effect.IO
import cats.effect.Resource
import com.microsoft.playwright.Page
import jsenv.playwright.PWEnv.Config
import org.scalajs.jsenv.Input
import org.scalajs.jsenv.RunConfig

import java.util
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.atomic.AtomicBoolean
import java.util.function.Consumer
import scala.annotation.tailrec
import scala.concurrent.duration.DurationInt

object ResourcesFactory {
  def preparePageForJsRun(
      pageInstance: Page,
      materializerResource: Resource[IO, FileMaterializer],
      input: Seq[Input],
      enableCom: Boolean
  ): Resource[IO, Unit] =
    for {
      m <- materializerResource
      _ <- Resource.pure(
        scribe.debug(s"Page instance is ${pageInstance.hashCode()}")
      )
      _ <- Resource.pure {
        val setupJsScript = Input.Script(JSSetup.setupFile(enableCom))
        val fullInput = setupJsScript +: input
        val materialPage =
          m.materialize(
            "scalajsRun.html",
            CEUtils.htmlPage(fullInput, m)
          )
        pageInstance.navigate(materialPage.toString)
      }
    } yield ()

  private def fetchMessages(
      pageInstance: Page,
      intf: String
  ): java.util.Map[String, java.util.List[String]] = {
    val data =
      pageInstance
        .evaluate(s"$intf.fetch();")
        .asInstanceOf[java.util.Map[String, java.util.List[String]]]
    data
  }

  def processUntilStop(
      stopSignal: AtomicBoolean,
      pageInstance: Page,
      intf: String,
      sendQueue: ConcurrentLinkedQueue[String],
      outStream: OutputStreams.Streams,
      receivedMessage: String => Unit
  ): Resource[IO, Unit] = {
    Resource.pure[IO, Unit] {
      scribe.debug(s"Started processUntilStop")
      while (!stopSignal.get()) {
        sendAll(sendQueue, pageInstance, intf)
        val jsResponse = fetchMessages(pageInstance, intf)
        streamWriter(jsResponse, outStream, Some(receivedMessage))
        IO.sleep(100.milliseconds)
      }
      scribe.debug(s"Stop processUntilStop")
    }
  }

  def isConnectionUp(
      pageInstance: Page,
      intf: String
  ): Resource[IO, Boolean] = {
    Resource.pure[IO, Boolean] {
      val status = pageInstance.evaluate(s"!!$intf;").asInstanceOf[Boolean]
      scribe.debug(
        s"Page instance is ${pageInstance.hashCode()} with status $status"
      )
      status
    }

  }

  def materializer(pwConfig: Config): Resource[IO, FileMaterializer] =
    Resource.make {
      IO.blocking(FileMaterializer(pwConfig.materialization)) // build
    } { fileMaterializer =>
      IO {
        scribe.debug("Closing the fileMaterializer")
        fileMaterializer.close()
      }.handleErrorWith(_ => {
        scribe.error("Error in closing the fileMaterializer")
        IO.unit
      }) // release
    }

  /*
   * Creates resource for outputStream
   */
  def outputStream(
      runConfig: RunConfig
  ): Resource[IO, OutputStreams.Streams] =
    Resource.make {
      IO.blocking(OutputStreams.prepare(runConfig)) // build
    } { outStream =>
      IO {
        scribe.debug(s"Closing the stream ${outStream.hashCode()}")
        outStream.close()
      }.handleErrorWith(_ => {
        scribe.error(s"Error in closing the stream ${outStream.hashCode()})")
        IO.unit
      }) // release
    }

  private def streamWriter(
      jsResponse: util.Map[String, util.List[String]],
      outStream: OutputStreams.Streams,
      onMessage: Option[String => Unit] = None
  ): Unit = {
    val data = jsResponse.get("consoleLog")
    val consoleError = jsResponse.get("consoleError")
    val error = jsResponse.get("errors")
    onMessage match {
      case Some(f) =>
        val msgs = jsResponse.get("msgs")
        msgs.forEach(consumer(f))
      case None => scribe.debug("No onMessage function")
    }
    data.forEach(outStream.out.println _)
    error.forEach(outStream.out.println _)
    consoleError.forEach(outStream.out.println _)

    if (!error.isEmpty) {
      val errList = error.toArray(Array[String]()).toList
      throw new WindowOnErrorException(errList)
    }
  }

  @tailrec
  def sendAll(
      sendQueue: ConcurrentLinkedQueue[String],
      pageInstance: Page,
      intf: String
  ): Unit = {
    val msg = sendQueue.poll()
    if (msg != null) {
      scribe.debug(s"Sending message")
      val script = s"$intf.send(arguments[0]);"
      val wrapper = s"function(arg) { $script }"
      pageInstance.evaluate(s"$wrapper", msg)
      val pwDebug = sys.env.getOrElse("PWDEBUG", "0")
      if (pwDebug == "1") {
        pageInstance.pause()
      }
      sendAll(sendQueue, pageInstance, intf)
    }
  }
  private def consumer[A](f: A => Unit): Consumer[A] = (v: A) => f(v)
  private def logStackTrace(): Unit = {
    try {
      throw new Exception("Logging stack trace")
    } catch {
      case e: Exception => e.printStackTrace()
    }
  }
}
