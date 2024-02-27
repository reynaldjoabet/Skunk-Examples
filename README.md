# Skunk-Examples

`Skunk` is a functional data access layer for Postgres.
Skunk doesn't use JDBC. It speaks the Postgres wire protocol. It will not work with any other database backend
Statements  Skunk recognizes two classes of statements: `Query`, for statements that return rows; and `Command`, for statements that do not return rows. These values can be constructed directly but typically arise via the `sql` interpolator.

`Session` Skunk's central abstraction is the `Session`, which represents a connection to Postgres. From the `Session` we can produce prepared statements, cursors, and other resources that ultimately depend on their originating `Session`
Skunk uses a Session object to represent a connection to the database.

Codecs When you construct a statement each parameter is specified via an `Encoder`, and row data is specified via a `Decoder`. In some cases encoders and decoders are symmetric and are defined together, as a `Codec`. There are many variants of this pattern in functional Scala libraries; this is closest in spirit to the strategy adopted by scodec.

HLists This idea was borrowed from scodec. We use `~` to build left-associated nested pairs of values and types, and can destructure with `~` the same way.

```scala
val a: Int ~ String ~ Boolean = 1 ~ "foo" ~ true
 a match { case n ~ s ~ b => ...}
 ```
Skunk provides two types of queries: simple queries and prepared queries

A simple query is a query with no parameters.(Simple queries are queries that don’t contain any parameters and, generally, aren’t going to be reused.)

`Session` provides the following methods for direct execution of simple queries
- `execute` returns	F[List[A]]:	All results, as a list.
- `option` returns	F[Option[A]]: Zero or one result, otherwise an error is raised.
- `unique`	returns F[A]: Exactly one result, otherwise an error is raised.
```scala
def execute[A](query: Query[Void, A]): F[List[A]]
def option[A](query: Query[Void, A]): F[Option[A]]

def unique[A](query: Query[Void, A]): F[A]
```

An extended query is a query with parameters, or a simple query that is executed via the extended query protocol.

Postgres provides a protocol for executing extended queries which is more involved than simple query protocol. It provides for prepared statements that can be reused with different sets of arguments, and provides cursors which allow results to be paged and streamed.

`PreparedQuery` provides the following methods for execution
`stream`->	Stream[F,B]	: All results, as a stream.
`option`->	F[Option[B]]:	Zero or one result, otherwise an error is raised.
`unique`->	F[B]	:Exactly one result, otherwise an error is raised.
`cursor`->	Resource[F,Cursor[F,B]]	:A cursor that returns pages of results.
`pipe`->	Pipe[F, A, B]:	A pipe that executes the query for each input value, concatenating the results.

only Query[A,B] and Command[A] can be turned into PreparedQuery
```scala
trait PreparedQuery[F[_], A, B] {
def stream(args: A, chunkSize: Int)(implicit or: Origin): Stream[F, B]
def option(args: A)(implicit or: Origin): F[Option[B]]
 def unique(args: A)(implicit or: Origin): F[B]
def cursor(args: A)(implicit or: Origin): Resource[F, Cursor[F, B]]
def pipe(chunkSize: Int)(implicit or: Origin): Pipe[F, A, B]
}
```

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


## Type class

```scala
trait Sync[F[_]] extends MonadCancel[F, Throwable] with Clock[F] with Unique[F] with Defer[F] {}

trait MonadCancel[F[_], E] extends MonadError[F, E] {}


trait Clock[F[_]] extends ClockPlatform[F] with Serializable {
}

trait Unique[F[_]] extends Serializable {
  def applicative: Applicative[F]
  def unique: F[Unique.Token]
}

trait Defer[F[_]] extends Serializable {
  def defer[A](fa: => F[A]): F[A]
}
trait MonadError[F[_], E] extends ApplicativeError[F, E] with Monad[F] {}


trait Monad[F[_]] extends FlatMap[F] with Applicative[F] {}

trait FlatMap[F[_]] extends Apply[F]
trait ApplicativeError[F[_], E] extends Applicative[F] {}


trait Applicative[F[_]] extends Apply[F] with InvariantMonoidal[F]

trait Apply[F[_]] extends Functor[F]




trait Async[F[_]] extends AsyncPlatform[F] with Sync[F] with Temporal[F] {}

//A typeclass that encodes the notion of suspending fibers for a given duration
trait GenTemporal[F[_], E] extends GenConcurrent[F, E] with Clock[F] {}

trait GenConcurrent[F[_], E] extends GenSpawn[F, E] {}

trait GenSpawn[F[_], E] extends MonadCancel[F, E] with Unique[F] {}
```


Products:

ProductID (Primary Key)
Name
Description
Price
StockQuantity
CategoryID (Foreign Key referencing Categories)
ImageURL
Categories:

CategoryID (Primary Key)
CategoryName
Customers:

CustomerID (Primary Key)
FirstName
LastName
Email
Password
Address
PhoneNumber
Orders:

OrderID (Primary Key)
CustomerID (Foreign Key referencing Customers)
OrderDate
TotalAmount
ShippingAddress
PaymentStatus
OrderDetails:

OrderDetailID (Primary Key)
OrderID (Foreign Key referencing Orders)
ProductID (Foreign Key referencing Products)
Quantity
UnitPrice

Relationships:

One-to-Many relationship between Categories and Products (one category can have many products).
One-to-Many relationship between Customers and Orders (one customer can place multiple orders).
One-to-Many relationship between Orders and OrderDetails (one order can have multiple order details).
Indexes:

Index on ProductID in the Products table for faster product retrieval.
Index on CategoryID in the Products table for efficient category-based queries.
Index on CustomerID in the Orders table for quick retrieval of customer orders.
Index on OrderID in the OrderDetails table for efficient retrieval of order details.



In a many-to-one relationship, the foreign key is placed on the "many" side of the relationship to reference the "one" side.


In a many-to-many relationship, an intermediary or junction table is used to connect the two entities involved. Let's consider an example using the Products and Orders tables in the context of an online merch store, where each order can contain multiple products, and each product can be a part of multiple orders.

Create a junction table, often referred to as an "OrderDetails" table, to link products to orders. This table will include foreign keys referencing both the Products and Orders tables.

```sql
CREATE TABLE Products (
    ProductID SERIAL PRIMARY KEY,
    Name VARCHAR(255) NOT NULL,
    Description TEXT,
    Price DECIMAL(10, 2) NOT NULL,
    StockQuantity INT NOT NULL
    -- Other product-related columns
);

CREATE TABLE Orders (
    OrderID SERIAL PRIMARY KEY,
    CustomerID INT REFERENCES Customers(CustomerID), -- Foreign key referencing Customers table
    OrderDate TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    TotalAmount DECIMAL(10, 2) NOT NULL,
    ShippingAddress TEXT,
    PaymentStatus VARCHAR(50) DEFAULT 'Pending'
    -- Other order-related columns
);

-- Junction table for the many-to-many relationship
CREATE TABLE OrderDetails (
    OrderDetailID SERIAL PRIMARY KEY,
    OrderID INT REFERENCES Orders(OrderID),         -- Foreign key referencing Orders table
    ProductID INT REFERENCES Products(ProductID),   -- Foreign key referencing Products table
    Quantity INT NOT NULL,
    UnitPrice DECIMAL(10, 2) NOT NULL
    -- Other columns related to the association between products and orders
);

````


- Postgres
- Cockroach Db
- AWS Aurora



```scala

final case class TradeRepositoryLive(postgres: Resource[Task, Session[Task]]) extends TradeRepository{

}

```


CockroachDB supports the PostgreSQL wire protocol, so you can use any available PostgreSQL client drivers to connect from various languages.

https://aws.amazon.com/rds/aurora/
[skunk](https://www.baeldung.com/scala/skunk-postgresql-driver)

[scala-functional-database-libraries](https://medium.com/rahasak/scala-functional-database-libraries-31364b2cf7b2)