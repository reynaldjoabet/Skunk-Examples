import java.util.concurrent.SynchronousQueue

import cats.effect._
import fs2.Stream

import natchez.Trace.Implicits.noop
import skunk._
import skunk.codec.all._
import skunk.data.Type
import skunk.implicits._

val session: Resource[IO, Session[IO]] =
  Session.single( // (2)
    host = "localhost",
    port = 5432,
    user = "jimmy",
    database = "world",
    password = Some("banana")
  )

case class User(id: Int, username: String, email: String, age: Int)

val query: Query[Void, Int *: String *: String *: Int *: EmptyTuple] =
  sql"SELECT * FROM Users".query(int4 *: varchar(255) *: varchar(255) *: int4)
// Since Skunk isn’t able to automatically detect the types returned by the table, we need to provide Encoder instances for all the expected types as arguments to the query() method.
//This tells Skunk that the Fragment represents a Query, and it returns the provided types.

//It’s worth noticing that the first type parameter of our Query is Void. That’s because our query doesn’t take any parameters.

session.use(_.execute(query))

session.use(_.option(query))

session.use(_.unique(query))

val mappedQuery: Query[Void, User] = query.to[User]

session.use(_.unique(mappedQuery))

val userDecoder: Decoder[User] = (int4 *: varchar(255) *: varchar(255) *: int4).to[User]

val query2: Query[Int *: String *: EmptyTuple, User] =
  sql"""
      SELECT * FROM Users WHERE
        id = $int4 AND username LIKE $varchar
    """.query(userDecoder)

query2

//Command[A] A represening the number of parameters
val command: Command[Short *: String *: EmptyTuple] = sql"INSERT INTO foo VALUES ($int2, $varchar)"
  .command

val preparedQuery: Resource[IO, PreparedQuery[IO, Int *: String *: EmptyTuple, User]] = session
  .flatMap(session => session.prepareR(query2))

preparedQuery.use(_.unique(1, "helloUser"))

preparedQuery.use(_.option(1, "helloUser"))

preparedQuery.use(_.stream((1, "helloUser"), 12).compile.toList)

def removePrepared(resource: Resource[IO, Session[IO]]) = {
  val command: Command[Int *: String *: EmptyTuple] =
    sql"""
      DELETE FROM Users WHERE
        id = $int4 and username = $varchar
    """.command
  resource.flatMap(session => session.prepareR(command)).use(pc => pc.execute((1, "baeldungUser")))
}

//PreparedCommand only offers a single method, execute(), to pass the parameters and run the command.

val c: Command[String] =
  sql"DELETE FROM country WHERE name = $varchar".command

session.use {
  _.prepare(c)
    .flatMap { pc =>
      pc.execute("xyzzy") *>
        pc.execute("fnord") *>
        pc.execute("blech")
    }
}

import cats.implicits._
session.use {
  _.prepare(c)
    .flatMap { pc =>
      List("xyzzy", "fnord", "blech").traverse(s => pc.execute(s))
    }
}

Stream
  .eval(session.use(_.prepare(c)))
  .flatMap { pc =>
    Stream("xyzzy", "fnord", "blech").through(pc.pipe)
  }

//Similar to mapping the output of a Query, we can contramap the input to a command or query.
//Here we provide a function that turns an Info into a String ~ String, yielding a Command[Info].

case class Info(code: String, hos: String)

val update2: Command[Info] =
  sql"""
    UPDATE country
    SET    headofstate = $varchar
    WHERE  code = ${bpchar(3)}
  """
    .command // Command[String *: String *: EmptyTuple]
    .contramap[Info] { case Info(code, hos) => code *: hos *: EmptyTuple } // Command[Info]

val updateCommand: Command[String *: String *: EmptyTuple] =
  sql"""
          UPDATE country
          SET    headofstate = $varchar
          WHERE  code = ${bpchar(3)}
        """.command

updateCommand.contramap[Info] { case Info(code, hos) => code *: hos *: EmptyTuple }

//updateCommand.contramap[Info]{info => info.code *: info.hos *: EmptyTuple}

// for INSERT and UPDATE, we can use contramap
val update3: Command[Info] =
  sql"""
    UPDATE country
    SET    headofstate = $varchar
    WHERE  code = ${bpchar(3)}
  """
    .command  // Command[String *: String *: EmptyTuple]
    .to[Info] // Command[Info]

session.use(_.execute(update2)(Info("", "")))

def deleteMany(n: Int): Command[List[String]] =
  sql"DELETE FROM country WHERE name IN (${varchar.list(n)})".command

val delete3 = deleteMany(3) // takes a list of size 3
// delete3: Command[List[String]] = Command(
//   sql = "DELETE FROM country WHERE name IN ($1, $2, $3)",
//   origin = Origin(file = "Command.md", line = 126),
//   encoder = Encoder(varchar, varchar, varchar)
// )

def insertMany(n: Int): Command[List[(String, Short)]] = {
  val enc = (varchar ~ int2).values.list(n)
  sql"INSERT INTO pets VALUES $enc".command
}

//ase class User(id: Int, username: String, email: String, age: Int)
def insertf(li: List[User]) = {
  val enc = (int4 ~ text ~ text ~ int4).values.list(6)
  enc
  sql"INSERT INTO d VALUES $enc".command
}

(int4 ~ text ~ text ~ int4).values.list(6)
(int4 ~ text ~ text ~ int4).list(6)

val insert3 = insertMany(3)
// insert3: Command[List[(String, Short)]] = Command(
//   sql = "INSERT INTO pets VALUES ($1, $2), ($3, $4), ($5, $6)",
//   origin = Origin(file = "Command.md", line = 137),
//   encoder = Encoder(varchar, int2, varchar, int2, varchar, int2)
// )

session.use(_.execute(insert3)(List(("hello", 12), ("hd", 3), ("jhe", 3)))).void

//returns a singleton type, hence the only type that can passed must be an exact match
def insertExactly(ps: List[(String, Short)]): Command[ps.type] = {
  val enc = (varchar ~ int2).values.list(ps)
  sql"INSERT INTO pets VALUES $enc".command
}

val pairs = List[(String, Short)](("Bob", 3), ("Alice", 6))
// pairs: List[(String, Short)] = List(("Bob", 3), ("Alice", 6))

val k: pairs.type = pairs
// Note the type!
val insertPairs = insertExactly(pairs)

session.use(_.prepare(insertPairs).flatMap(pc => pc.execute(pairs)))

//session.use{_.prepare(insertPairs).flatMap { pc => pc.execute(pairs.drop(1)) }}//error

//An encoder is needed any time you want to send a value to Postgres; i.e., any time a statement has parameters.

//A base encoder maps a Scala type to a single Postgres schema type.

//Opaque  StudentId=Int

case class Person(name: String, age: Int)

val person: Encoder[Person] = (varchar *: int4).values.contramap((p: Person) => (p.name, p.age))
val person1                 = (varchar *: int4).values.to[Person]

sql"INSERT INTO person (name, age) VALUES $person"

//CREATE TYPE myenum AS ENUM ('foo', 'bar')

// An enumerated type
sealed abstract class MyEnum(val label: String)
object MyEnum {

  case object Foo extends MyEnum("foo")
  case object Bar extends MyEnum("bar")

  val values = List(Foo, Bar)

  def fromLabel(label: String): Option[MyEnum] =
    values.find(_.label == label)

}

// A codec that maps Postgres type `myenum` to Scala type `MyEnum`
val myenum: Codec[MyEnum] = enum[MyEnum](_.label, MyEnum.fromLabel, Type("myenum"))

"""|Hello 
   |Dude
""".stripMargin

val n: 9 = 9

def h(l: 9): l.type = 9

h(n)

"9898".startsWith(n.toString())

(1 to 10).mkString(",")
