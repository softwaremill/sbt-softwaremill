package jsenv.playwright

import jsenv.playwright.PWEnv.Config
import org.scalajs.jsenv._

import java.net.URI
import java.net.URL
import java.nio.file.Path
import java.nio.file.Paths
import scala.util.control.NonFatal

/**
 * Playwright JS environment
 *
 * @param browserName
 *   browser name, options are "chromium", "chrome", "firefox", "webkit", default is "chromium"
 * @param headless
 *   headless mode, default is true
 * @param showLogs
 *   show logs, default is false
 * @param debug
 *   debug mode, default is false
 * @param pwConfig
 *   Playwright configuration
 * @param launchOptions
 *   override launch options, if not provided default launch options are used
 * @param additionalLaunchOptions
 *   additional launch options (added to (default) launch options)
 */
class PWEnv(
    browserName: String = "chromium",
    headless: Boolean = true,
    showLogs: Boolean = false,
    debug: Boolean = false,
    pwConfig: Config = Config(),
    launchOptions: List[String] = Nil,
    additionalLaunchOptions: List[String] = Nil
) extends JSEnv {

  private lazy val validator = {
    RunConfig.Validator().supportsInheritIO().supportsOnOutputStream()
  }
  override val name: String = s"CEEnv with $browserName"
  System.setProperty("playwright.driver.impl", "jsenv.DriverJar")
  CEUtils.setupLogger(showLogs, debug)

  override def start(input: Seq[Input], runConfig: RunConfig): JSRun = {
    try {
      validator.validate(runConfig)
      new CERun(
        browserName,
        headless,
        pwConfig,
        runConfig,
        input,
        launchOptions,
        additionalLaunchOptions)
    } catch {
      case ve: java.lang.IllegalArgumentException =>
        scribe.error(s"CEEnv.startWithCom failed with throw ve $ve")
        throw ve
      case NonFatal(t) =>
        scribe.error(s"CEEnv.start failed with $t")
        JSRun.failed(t)
    }
  }

  override def startWithCom(
      input: Seq[Input],
      runConfig: RunConfig,
      onMessage: String => Unit
  ): JSComRun = {
    try {
      validator.validate(runConfig)
      new CEComRun(
        browserName,
        headless,
        pwConfig,
        runConfig,
        input,
        launchOptions,
        additionalLaunchOptions,
        onMessage
      )
    } catch {
      case ve: java.lang.IllegalArgumentException =>
        scribe.error(s"CEEnv.startWithCom failed with throw ve $ve")
        throw ve
      case NonFatal(t) =>
        scribe.error(s"CEEnv.startWithCom failed with $t")
        JSComRun.failed(t)
    }
  }

}

object PWEnv {
  case class Config(
      materialization: Config.Materialization = Config.Materialization.Temp
  ) {
    import Config.Materialization

    /**
     * Materializes purely virtual files into a temp directory.
     *
     * Materialization is necessary so that virtual files can be referred to by name. If you do
     * not know/care how your files are referred to, this is a good default choice. It is also
     * the default of [[PWEnv.Config]].
     */
    def withMaterializeInTemp: Config =
      copy(materialization = Materialization.Temp)

    /**
     * Materializes files in a static directory of a user configured server.
     *
     * This can be used to bypass cross origin access policies.
     *
     * @param contentDir
     *   Static content directory of the server. The files will be put here. Will get created if
     *   it doesn't exist.
     * @param webRoot
     *   URL making `contentDir` accessible thorugh the server. This must have a trailing slash
     *   to be interpreted as a directory.
     *
     * @example
     *
     * The following will make the browser fetch files using the http:// schema instead of the
     * file:// schema. The example assumes a local webserver is running and serving the ".tmp"
     * directory at http://localhost:8080.
     *
     * {{{
     *  jsSettings(
     *    jsEnv := new SeleniumJSEnv(
     *        new org.openqa.selenium.firefox.FirefoxOptions(),
     *        SeleniumJSEnv.Config()
     *          .withMaterializeInServer(".tmp", "http://localhost:8080/")
     *    )
     *  )
     * }}}
     */
    def withMaterializeInServer(contentDir: String, webRoot: String): Config =
      withMaterializeInServer(Paths.get(contentDir), new URI(webRoot).toURL)

    /**
     * Materializes files in a static directory of a user configured server.
     *
     * Version of `withMaterializeInServer` with stronger typing.
     *
     * @param contentDir
     *   Static content directory of the server. The files will be put here. Will get created if
     *   it doesn't exist.
     * @param webRoot
     *   URL making `contentDir` accessible thorugh the server. This must have a trailing slash
     *   to be interpreted as a directory.
     */
    def withMaterializeInServer(contentDir: Path, webRoot: URL): Config =
      copy(materialization = Materialization.Server(contentDir, webRoot))

    def withMaterialization(materialization: Materialization): Config =
      copy(materialization = materialization)
  }

  object Config {

    abstract class Materialization private ()
    object Materialization {
      final case object Temp extends Materialization
      final case class Server(contentDir: Path, webRoot: URL) extends Materialization {
        require(
          webRoot.getPath.endsWith("/"),
          "webRoot must end with a slash (/)"
        )
      }
    }
  }

  val chromeLaunchOptions = List(
    "--disable-extensions",
    "--disable-web-security",
    "--allow-running-insecure-content",
    "--disable-site-isolation-trials",
    "--allow-file-access-from-files",
    "--disable-gpu"
  )

  val firefoxLaunchOptions = List("--disable-web-security")

  val webkitLaunchOptions = List(
    "--disable-extensions",
    "--disable-web-security",
    "--allow-running-insecure-content",
    "--disable-site-isolation-trials",
    "--allow-file-access-from-files"
  )
}
