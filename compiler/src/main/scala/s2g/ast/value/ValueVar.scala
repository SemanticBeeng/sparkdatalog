package s2g.ast.value

import s2g.eval.Context
import s2g.spark.Valuation

case class ValueVar(name: String) extends Value {
  override def evaluate(context: Context): ValueLiteral = context(name)

  override def toString: String = name

  override def tryToEvaluate(context: Context): Value = context.get(name) match {
    case Some(valueLiteral) => valueLiteral
    case None => this
  }

  override def getFreeVariables: Set[String] = Set(name)

  override def evaluate(valuation: Valuation): Int = valuation(name)
}
