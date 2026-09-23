package sbt.softwaremill

import sbt.internal.util.{SimpleReader, Terminal}

object TerminalReader {

  /** Read input from the terminal of the client that issued the currently running command. */
  def readLine(prompt: String): Option[String] = SimpleReader(Terminal.get).readLine(prompt)
}
