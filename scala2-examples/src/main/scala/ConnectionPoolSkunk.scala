import cats.effect.IO
import cats.effect.kernel.Resource
import natchez.Trace.Implicits.noop
import skunk.Session

object ConnectionPoolSkunk {

val kunkConnectionPool: Resource[IO, Resource[IO, Session[IO]]] = Session.pooled[IO](
  host = "localhost",
  port = 5432,
  user = "jimmy",
  database = "world",
  password = Some("banana"),
  max = 10,
  debug = false
)

}
