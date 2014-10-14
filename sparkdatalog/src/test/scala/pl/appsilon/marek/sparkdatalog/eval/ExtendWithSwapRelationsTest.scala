package pl.appsilon.marek.sparkdatalog.eval

import org.scalatest._
import pl.appsilon.marek.sparkdatalog.ast.Program
import pl.appsilon.marek.sparkdatalog.ast.exp.EValue
import pl.appsilon.marek.sparkdatalog.ast.predicate.Predicate
import pl.appsilon.marek.sparkdatalog.ast.rule.{RuleBody, Head, Rule}
import pl.appsilon.marek.sparkdatalog.ast.subgoal.{GoalAssign, GoalPredicate}
import pl.appsilon.marek.sparkdatalog.ast.types.TypeInt
import pl.appsilon.marek.sparkdatalog.ast.value.{ValueLiteral, ValueVar}
import socialite.Absyn.ValueInt

class ExtendWithSwapRelationsTest extends FlatSpec with Matchers {

  "ExtendWithSwapRelations" should "add swap relations for moving to var location" in {
    val program = Program(Seq(), Seq(
      Rule(Head("P", Seq("x", "y")),
        RuleBody(Seq(
          GoalPredicate(Predicate("P", Seq(ValueVar("y"), ValueVar("x")))),
          GoalPredicate(Predicate("P", Seq(ValueVar("x"), ValueVar("y"))))
        )))
    ))

    val result = ExtendWithSwapRelations(program)

    result should be (
      Program(Seq(), Seq(
        Rule(Head("_aux_P_arg_1", Seq("_location", "_aux_var_0", "_aux_var_1")),
          RuleBody(Seq(
            GoalPredicate(Predicate("P", Seq(ValueVar("_aux_var_0"), ValueVar("_aux_var_1")))),
            GoalAssign("_location", EValue(ValueVar("_aux_var_1")))
          ))),
        Rule(Head("P", Seq("x", "y")),
          RuleBody(Seq(
            GoalPredicate(Predicate("_aux_P_arg_1", Seq(ValueVar("x"), ValueVar("y"), ValueVar("x")))),
            GoalPredicate(Predicate("P", Seq(ValueVar("x"), ValueVar("y"))))
          )))
      ))
    )
  }

  "ExtendWithSwapRelations" should "add swap relations for moving to const location" in {
    val program = Program(Seq(), Seq(
      Rule(Head("P", Seq("x", "y")),
        RuleBody(Seq(
          GoalPredicate(Predicate("P", Seq(ValueVar("y"), ValueVar("x")))),
          GoalPredicate(Predicate("P", Seq(ValueLiteral(new TypeInt, 1), ValueVar("y"))))
        )))
    ))

    val result = ExtendWithSwapRelations(program)

    result should be (
      Program(Seq(), Seq(
        Rule(Head("_aux_P_literal_1", Seq("_location", "_aux_var_0", "_aux_var_1")),
          RuleBody(Seq(
            GoalPredicate(Predicate("P", Seq(ValueVar("_aux_var_0"), ValueVar("_aux_var_1")))),
            GoalAssign("_location", EValue(ValueLiteral(new TypeInt, 1)))
          ))),
        Rule(Head("P", Seq("x", "y")),
          RuleBody(Seq(
            GoalPredicate(Predicate("_aux_P_literal_1", Seq(ValueLiteral(new TypeInt, 1), ValueVar("y"), ValueVar("x")))),
            GoalPredicate(Predicate("P", Seq(ValueLiteral(new TypeInt, 1), ValueVar("y"))))
          )))
      ))
    )
  }

}
