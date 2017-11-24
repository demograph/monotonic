import io.demograph.monotonic.`var`.MVar

import scala.reflect.runtime.universe
import scala.reflect.runtime.universe._

val map: Map[universe.TypeTag[_], List[_]] = Map(
  implicitly[TypeTag[String]] → List(1),
  implicitly[TypeTag[Int]] → List(""))


map.get(implicitly[TypeTag[Double]])
map.get(implicitly[TypeTag[Int]])
map.get(implicitly[TypeTag[String]])