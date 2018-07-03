package core.http.util

import com.google.gson.Gson

object JsonParser {
  def toJson[O](o: O): String = (new Gson).toJson(o)
}
