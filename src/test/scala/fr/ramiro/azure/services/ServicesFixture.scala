package fr.ramiro.azure.services

import okhttp3.{ MediaType, ResponseBody }
import retrofit2.Response

trait ServicesFixture {
  def createSuccessResponse(content: String) = Response.success[ResponseBody](
    ResponseBody.create(MediaType.parse("application/json"), content)
  )

  def createErrorResponse(jsonError: String): Response[ResponseBody] = {
    Response.error(404, ResponseBody.create(MediaType.parse("application/json"), Option(jsonError).getOrElse("")))
  }
}
