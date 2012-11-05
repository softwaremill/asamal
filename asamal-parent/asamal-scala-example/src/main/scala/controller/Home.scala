package pl.softwaremill.asamal.scala.controller

import pl.softwaremill.asamal.controller.annotation._
import pl.softwaremill.asamal.controller.ControllerBean
import controller.User

/**
 * 
 * User: szimano
 */
@Controller("home")
class Home extends ControllerBean {

  @Get
  def index() {
    putInContext("hello", "Hello Tomek!")
  }

  @Get
  @Json
  def user() : User = {
    new User("Tomek", "Szymanski")
  }

  @Put
  def user(@JSONObject user: User) {
    println("Got new user " + user)
  }
}
