package pl.softwaremill.asamal.scala.controller

import pl.softwaremill.asamal.controller.annotation.{Controller, Get}
import pl.softwaremill.asamal.controller.ControllerBean

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
}
