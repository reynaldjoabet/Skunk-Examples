import skunk.SessionPool

import skunk.Channel

import skunk.Codec

import skunk.Decoder

import skunk.Cursor

import skunk.Encoder

import skunk.Fragment
import skunk.PreparedCommand

import skunk.PreparedQuery

import skunk.Query

import skunk.SSL

import skunk.Session
import skunk.SqlState
import skunk.Statement
import skunk.Transaction

import skunk.Void

import skunk.feature

import skunk.featureFlags

import skunk.codec.AllCodecs
import skunk.data.Notification
import skunk.exception

import skunk.net.protocol.Exchange

import skunk.net.protocol

import skunk.net.protocol
import skunk.syntax

import skunk.util.Text

import skunk.util.Pool
import skunk.implicits
import natchez.Trace.Implicits.noop

import skunk.~
import cats.effect.IO
//Session represents a connection to a Postgres database.
//Skunk currently supports the trust (no password necessary), password, md5 and scram-sha-256 authentication methods.
object Main extends App {

  val kunkConnectionPool=Session.pooled[IO](
         host     = "localhost",
         port     = 5432,
         user     = "jimmy",
         database = "world",
         password = Some("banana"),
         max = 10,
         debug = false
  )

  //An encoder is needed any time you want to send a value to Postgres; i.e., any time a statement has parameters.

  //use one or more existing encoders (see Schema Types) composed or transformed as desired.

  //A base encoder maps a Scala type to a single Postgres schema type
  println("Hello, World!")
}