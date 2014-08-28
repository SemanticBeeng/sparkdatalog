package s2g.ast.value

import s2g.eval.{Context, PartialSolution}
import s2g.ast.types.Type
import s2g.spark.Valuation

case class ValueLiteral(typ: Type, value: AnyVal) extends Value {
  override def evaluate(context: Context): ValueLiteral = this

  override def toString: String = value.toString

  override def tryToEvaluate(context: Context): Value = this

  override def getFreeVariables: Set[String] = Set()

  override def evaluate(valuation: Valuation): Int = value.asInstanceOf[Int]
}
