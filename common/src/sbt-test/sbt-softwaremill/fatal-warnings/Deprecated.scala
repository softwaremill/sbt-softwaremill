package example

// calling a deprecated method emits a warning on every Scala version;
object Deprecated {
  @deprecated("for testing", "0.1")
  def old: Int = 1

  def usesOld: Int = old
}
