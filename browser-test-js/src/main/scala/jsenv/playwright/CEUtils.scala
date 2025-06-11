package jsenv.playwright

import org.scalajs.jsenv.Input
import org.scalajs.jsenv.UnsupportedInputException
import scribe.format.FormatterInterpolator
import scribe.format.classNameSimple
import scribe.format.dateFull
import scribe.format.level
import scribe.format.mdc
import scribe.format.messages
import scribe.format.methodName
import scribe.format.threadName

import java.nio.file.Path

object CEUtils {
  def htmlPage(
      fullInput: Seq[Input],
      materializer: FileMaterializer
  ): String = {
    val tags = fullInput.map {
      case Input.Script(path) => makeTag(path, "text/javascript", materializer)
      case Input.CommonJSModule(path) =>
        makeTag(path, "text/javascript", materializer)
      case Input.ESModule(path) => makeTag(path, "module", materializer)
      case _ => throw new UnsupportedInputException(fullInput)
    }

    s"""<html>
       |  <meta charset="UTF-8">
       |  <body>
       |    ${tags.mkString("\n    ")}
       |  </body>
       |</html>
    """.stripMargin
  }

  private def makeTag(
      path: Path,
      tpe: String,
      materializer: FileMaterializer
  ): String = {
    val url = materializer.materialize(path)
    s"<script defer type='$tpe' src='$url'></script>"
  }

  def setupLogger(showLogs: Boolean, debug: Boolean): Unit = {
    val formatter =
      formatter"$dateFull [$threadName] $classNameSimple $level $methodName - $messages$mdc"
    scribe
      .Logger
      .root
      .clearHandlers()
      .withHandler(
        formatter = formatter
      )
      .replace()
    // default log level is error
    scribe.Logger.root.withMinimumLevel(scribe.Level.Error).replace()

    if (showLogs) {
      scribe.Logger.root.withMinimumLevel(scribe.Level.Info).replace()
    }
    if (debug) {
      scribe.Logger.root.withMinimumLevel(scribe.Level.Trace).replace()
    }
  }

}
