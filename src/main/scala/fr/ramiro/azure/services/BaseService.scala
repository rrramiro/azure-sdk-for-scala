package fr.ramiro.azure.services

import com.fasterxml.jackson.databind.ObjectMapper
import com.microsoft.azure.CloudError
import com.microsoft.rest.ServiceResponse
import fr.ramiro.azure.model.CloudException
import okhttp3.ResponseBody
import retrofit2.Response

trait BaseService[T] {
  val objectMapper: ObjectMapper
  val defaultApiVersion = "2015-11-01"
  val defaultAcceptLanguage = "en-US"
  val defaultUserAgent = s"Azure-SDK-For-Java/${getClass.getPackage.getImplementationVersion} (SubscriptionClient, $defaultApiVersion)"

  def addParent(child: T): T

  def createCloudException(response: Response[ResponseBody]): CloudException = {
    new CloudException("Invalid status code " + response.code, buildCloudError(response.code, response.errorBody), response)
  }

  def createServiceResponse[U](response: Response[ResponseBody], buildBody: (String) => U): ServiceResponse[U] = {
    try {
      if (response.isSuccessful) {
        new ServiceResponse[U](buildBody(response.body().string), response)
      } else {
        throw createCloudException(response)
      }
    } finally {
      Option(response.body()) match {
        case Some(body) => body.close()
        case _ =>
      }
    }
  }

  //TODO Send more info
  private def buildCloudError(statusCode: Int, responseBody: ResponseBody): CloudError = {
    try {
      objectMapper.readValue(responseBody.string, classOf[CloudError])
    } catch {
      case e: Throwable => new CloudError {
        setMessage(e.getMessage)
      }
    } finally {
      responseBody.close()
    }
  }
}
