package controller

import org.codehaus.jackson.annotate.JsonProperty

/**
 *
 * User: szimano
 */
class User(@JsonProperty name : String, @JsonProperty lastname: String) {

  def this() = this(null, null)

  override def toString = "[User; name="+name+"; lastname="+lastname+"]"
}
