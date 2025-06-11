package jsenv.playwright

import java.net._
import java.nio.file._
import java.util

abstract class FileMaterializer extends AutoCloseable {
  private val tmpSuffixRE = """[a-zA-Z0-9-_.]*$""".r

  private var tmpFiles: List[Path] = Nil

  def materialize(path: Path): URL = {
    val tmp = newTmp(path.toString)
    // if file with extension .map exist then copy it too
    val mapPath = Paths.get(path.toString + ".map")
    Files.copy(path, tmp, StandardCopyOption.REPLACE_EXISTING)
    if (Files.exists(mapPath)) {
      val tmpMap = newTmp(mapPath.toString)
      Files.copy(mapPath, tmpMap, StandardCopyOption.REPLACE_EXISTING)
    }
    toURL(tmp)
  }

  final def materialize(name: String, content: String): URL = {
    val tmp = newTmp(name)
    Files.write(tmp, util.Arrays.asList(content))
    toURL(tmp)
  }

  final def close(): Unit = {
    tmpFiles.foreach(Files.delete)
    tmpFiles = Nil
  }

  private def newTmp(path: String): Path = {
    val suffix = tmpSuffixRE.findFirstIn(path).orNull
    val p = createTmp(suffix)
    tmpFiles ::= p
    p
  }

  protected def createTmp(suffix: String): Path
  protected def toURL(file: Path): URL
}

object FileMaterializer {
  import PWEnv.Config.Materialization
  def apply(m: Materialization): FileMaterializer = m match {
    case Materialization.Temp =>
      new TempDirFileMaterializer

    case Materialization.Server(contentDir, webRoot) =>
      new ServerDirFileMaterializer(contentDir, webRoot)
  }
}

/**
 * materializes virtual files in a temp directory (uses file:// schema).
 */
private class TempDirFileMaterializer extends FileMaterializer {
  override def materialize(path: Path): URL = {
    try {
      path.toFile.toURI.toURL
    } catch {
      case _: UnsupportedOperationException =>
        super.materialize(path)
    }
  }

  protected def createTmp(suffix: String): Path =
    Files.createTempFile(null, suffix)
  protected def toURL(file: Path): URL = file.toUri.toURL
}

private class ServerDirFileMaterializer(contentDir: Path, webRoot: URL)
    extends FileMaterializer {
  Files.createDirectories(contentDir)

  protected def createTmp(suffix: String): Path =
    Files.createTempFile(contentDir, null, suffix)

  protected def toURL(file: Path): URL = {
    val rel = contentDir.relativize(file)
    assert(!rel.isAbsolute)
    val nameURI = new URI(null, null, rel.toString, null)
    webRoot.toURI.resolve(nameURI).toURL
  }
}
