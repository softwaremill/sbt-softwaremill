package jsenv.playwright

import cats.effect.IO
import cats.effect.Resource
import com.microsoft.playwright.Browser
import com.microsoft.playwright.BrowserType
import com.microsoft.playwright.BrowserType.LaunchOptions
import com.microsoft.playwright.Page
import com.microsoft.playwright.Playwright

object PageFactory {
  def pageBuilder(browser: Browser): Resource[IO, Page] = {
    Resource.make(IO {
      val pg = browser.newContext().newPage()
      scribe.debug(s"Creating page ${pg.hashCode()} ")
      pg
    })(page => IO { page.close() })
  }

  private def browserBuilder(
      playwright: Playwright,
      browserName: String,
      headless: Boolean,
      launchOptions: LaunchOptions
  ): Resource[IO, Browser] =
    Resource.make(IO {

      val browserType: BrowserType = browserName.toLowerCase match {
        case "chromium" | "chrome" =>
          playwright.chromium()
        case "firefox" =>
          playwright.firefox()
        case "webkit" =>
          playwright.webkit()
        case _ => throw new IllegalArgumentException("Invalid browser type")
      }
      val browser = browserType.launch(launchOptions.setHeadless(headless))
      scribe.info(
        s"Creating browser ${browser.browserType().name()} version ${browser.version()} with ${browser.hashCode()}"
      )
      browser
    })(browser =>
      IO {
        scribe.debug(s"Closing browser with ${browser.hashCode()}")
        browser.close()
      })

  private def playWrightBuilder: Resource[IO, Playwright] =
    Resource.make(IO {
      scribe.debug(s"Creating playwright")
      Playwright.create()
    })(pw =>
      IO {
        scribe.debug("Closing playwright")
        pw.close()
      })

  def createPage(
      browserName: String,
      headless: Boolean,
      launchOptions: LaunchOptions
  ): Resource[IO, Page] =
    for {
      playwright <- playWrightBuilder
      browser <- browserBuilder(
        playwright,
        browserName,
        headless,
        launchOptions
      )
      page <- pageBuilder(browser)
    } yield page

}
