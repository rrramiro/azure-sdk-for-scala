package fr.ramiro.azure.services

import com.fasterxml.jackson.databind.ObjectMapper
import com.microsoft.azure.{ CloudError, CloudException }
import okhttp3.ResponseBody
import retrofit2.Response

trait BaseService[T] {
  val objectMapper: ObjectMapper
  val defaultApiVersion = "2015-11-01"
  val defaultAcceptLanguage = "en-US"
  val defaultUserAgent = s"Azure-SDK-For-Java/${getClass.getPackage.getImplementationVersion} (SubscriptionClient, $defaultApiVersion)"

  def addParent(child: T): T

  def createCloudException(response: Response[ResponseBody]): CloudException = {
    new CloudException("Invalid status code " + response.code) {
      setResponse(response)
      setBody(buildCloudError(response.code, response.errorBody))
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
