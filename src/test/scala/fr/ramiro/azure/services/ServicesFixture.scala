package fr.ramiro.azure.services

import okhttp3.{ MediaType, ResponseBody }
import retrofit2.Response

trait ServicesFixture {
  def createSuccessResponse(content: String) = Response.success[ResponseBody](
    ResponseBody.create(MediaType.parse("application/json"), content)
  )
}
