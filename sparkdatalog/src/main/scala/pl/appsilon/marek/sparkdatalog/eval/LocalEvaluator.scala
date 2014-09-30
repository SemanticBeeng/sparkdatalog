package pl.appsilon.marek.sparkdatalog.eval

import pl.appsilon.marek.sparkdatalog.Database
import pl.appsilon.marek.sparkdatalog.ast.Program
import pl.appsilon.marek.sparkdatalog.ast.rule.Rule
import pl.appsilon.marek.sparkdatalog.util.{NTimed, Timed}

object LocalEvaluator {

  private def makeIteration(
      staticContext: StaticEvaluationContext,
      rules: Iterable[Rule],
      state: Seq[(Long, StateShard)]): Seq[(Long, StateShard)] = {
    def generateMessages(key: Long, shard: StateShard): Seq[(Long, RelationInstance)] = {
      NTimed("generateMessages " + key, () => rules.map(_.evaluate(staticContext, shard)).reduce(_ ++ _))
    }

    val rawMessages = state.flatMap(Function.tupled(generateMessages))
    val messages = rawMessages.groupBy(_._1).map({
      case (key, values) => key -> values.map(_._2).reduce(_.merge(_, staticContext))
    })
    val oldStateShards = state.toMap
    val newStateShards = (state.map(_._1) ++ messages.keys).map({
      case key =>
        (oldStateShards.get(key), messages.get(key)) match {
          case (Some(left), Some(right)) => (key, left.merge(right, staticContext))
          case (Some(left), None) => (key, left)
          case (None, Some(right)) => (key, StateShard.fromRelationInstance(right, staticContext))
          case _ => ???
        }
    })
    println("All messages = " + messages + "\n+old shards = " + state + " \n --> new shards = " + newStateShards)

    val result = newStateShards.map({
      case (key, left) => key -> left.delted(oldStateShards.get(key))
    })
    result
  }

  def evaluate(database: Database, program: Program): Seq[(Long, StateShard)] = {
    val context = StaticEvaluationContext(program.aggregations)

    val relationsWithKeys = database.relations.toSeq.map({
      case (name, instance) => RelationInstance(name, instance.data.collect()).toKeyValue }).flatten
    var state: Seq[(Long, StateShard)] = relationsWithKeys.groupBy(_._1).map({
      case (key, relations) =>
        key -> relations.map(_._2).map(rel => StateShard(Map(rel.name -> rel))).reduce(_ ++ _ )
    }).toSeq

    var iteration = 0

    println("Initial state= " + state.toString)

    do {
      println("Making iteration " + iteration)
      state = makeIteration(context, program.rules, state)
      iteration += 1
   //   println("stan: " + state)
    } while (!state.forall(_._2.delta.forall(_._2.isEmpty)))

    println(state.toString)

    state
  }
}
