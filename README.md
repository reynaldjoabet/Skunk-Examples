# Skunk-Examples

`Skunk` is a functional data access layer for Postgres.
Skunk doesn't use JDBC. It speaks the Postgres wire protocol. It will not work with any other database back end
Statements  Skunk recognizes two classes of statements: `Query`, for statements that return rows; and `Command`, for statements that do not return rows. These values can be constructed directly but typically arise via the `sql` interpolator.

`Session` Skunk's central abstraction is the `Session`, which represents a connection to Postgres. From the `Session` we can produce prepared statements, cursors, and other resources that ultimately depend on their originating `Session`

Codecs When you construct a statement each parameter is specified via an `Encoder`, and row data is specified via a `Decoder`. In some cases encoders and decoders are symmetric and are defined together, as a `Codec`. There are many variants of this pattern in functional Scala libraries; this is closest in spirit to the strategy adopted by scodec.

HLists This idea was borrowed from scodec. We use `~` to build left-associated nested pairs of values and types, and can destructure with `~` the same way.

```scala
val a: Int ~ String ~ Boolean = 1 ~ "foo" ~ true
 a match { case n ~ s ~ b => ...}
 ```


A simple query is a query with no parameters.
`Session` provides the following methods for direct execution of simple queries
- `execute` returns	F[List[A]]:	All results, as a list.
- `option` returns	F[Option[A]]: Zero or one result, otherwise an error is raised.
- `unique`	returns F[A]: Exactly one result, otherwise an error is raised.


An extended query is a query with parameters, or a simple query that is executed via the extended query protocol.

Postgres provides a protocol for executing extended queries which is more involved than simple query protocol. It provides for prepared statements that can be reused with different sets of arguments, and provides cursors which allow results to be paged and streamed.

`PreparedQuery` provides the following methods for execution
`stream`->	Stream[F,B]	: All results, as a stream.
`option`->	F[Option[B]]:	Zero or one result, otherwise an error is raised.
`unique`->	F[B]	:Exactly one result, otherwise an error is raised.
`cursor`->	Resource[F,Cursor[F,B]]	:A cursor that returns pages of results.
`pipe`->	Pipe[F, A, B]:	A pipe that executes the query for each input value, concatenating the results.

## Summary of Query Types
The simple query protocol (i.e., `Session#execute`) is slightly more efficient in terms of message exchange, so use it if:
- Your query has no parameters; and
- you are querying for a small number of rows; and
- you will be using the query only once per session.

The extend query protocol (i.e., `Session#prepare`) is more powerful and more general, but requires additional network exchanges. Use it if:
- Your query has parameters; and/or
- you are querying for a large or unknown number of rows; and/or
- you intend to stream the results; and/or
- you will be using the query more than once per session.

profunctor can be contramapped to change the input type, and mapped to change the output type.

## Heterogenous list, or HList
represent lists of varying lengths with different element types
`HList` type class provides a way to create a list of more than a single type. Remember that the `List` type class in Scala always provides a list of a specific type (e.g. List[Int]). With HList, we can create lists of more than a single type.

[Shapeless and Scala3](http://www.limansky.me/posts/2021-07-26-from-scala-2-shapeless-to-scala-3.html)

[Tuples in Scala3](https://www.scala-lang.org/2021/02/26/tuples-bring-generic-programming-to-scala-3.html)

In Scala 2, we can access the elements by using the _1, _2 and so on. In Scala 3, we can access the tuple elements by its position, just like an Array or a List. Let's look at an example:

```scala
val tuple = ("This", "is", "Scala", 3, "Tuple")
assert(tuple(0) == "This")
assert(tuple(3) == 3)
assert(tuple._1 == tuple(0))
```


implicit parameters in Scala2 are called context parameters in scala3


Doobie uses shapeless in Scala2 but not in Scala3

[skunk](https://www.baeldung.com/scala/skunk-postgresql-driver)