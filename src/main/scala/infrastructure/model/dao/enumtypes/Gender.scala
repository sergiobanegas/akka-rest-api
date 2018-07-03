package infrastructure.model.dao.enumtypes

object Gender extends Enumeration {
  val M, F = Value

  def isGenderType(n: String): Boolean = values.exists(_.toString == n)
}
