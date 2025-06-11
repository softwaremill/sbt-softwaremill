package jsenv.playwright

import org.scalajs.jsenv.RunConfig

import java.io._

object OutputStreams {
  final class Streams(val out: PrintStream, val err: PrintStream) {
    def close(): Unit = {
      out.close()
      err.close()
    }
  }

  def prepare(config: RunConfig): Streams = {
    val outp = optPipe(!config.inheritOutput)
    val errp = optPipe(!config.inheritError)

    config.onOutputStream.foreach(f => f(outp.map(_._1), errp.map(_._1)))

    val out = outp.fold[OutputStream](new UnownedOutputStream(System.out))(_._2)
    val err = errp.fold[OutputStream](new UnownedOutputStream(System.err))(_._2)

    new Streams(new PrintStream(out), new PrintStream(err))
  }

  private def optPipe(want: Boolean) = {
    if (want) {
      val i = new PipedInputStream()
      val o = new PipedOutputStream(i)
      Some((i, o))
    } else {
      None
    }
  }

  private class UnownedOutputStream(out: OutputStream) extends FilterOutputStream(out) {
    override def close(): Unit = flush()
  }
}
