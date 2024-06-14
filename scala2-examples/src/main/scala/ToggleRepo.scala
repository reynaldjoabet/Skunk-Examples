import cats.effect.IO

/**
  * define curd operations of toggles table return values wrapped with IO monad
  */
trait ToggleRepo {

  def createToggle(toggle: Toggle): IO[Unit]

  def updateToggle(name: String, value: String): IO[Unit]

  def deleteToggle(name: String): IO[Unit]

  def getToggle(name: String): IO[Option[Toggle]]

  def getServiceToggles(service: String): IO[List[Toggle]]

  def getAllToggles: IO[List[Toggle]]

}
