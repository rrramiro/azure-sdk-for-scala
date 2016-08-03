package fr.ramiro.azure.rest

import java.io.InputStream

import com.fasterxml.jackson.databind.ObjectMapper
import com.google.common.reflect.TypeToken
import com.microsoft.azure.{ CloudError, CloudException }
import com.microsoft.rest._
import okhttp3.ResponseBody
import retrofit2.Response
import scala.reflect.ClassTag

class AzureServiceResponseBuilder[T](
    objectMapper: ObjectMapper,
    expectedStatusCode: Int*
) {

  def build(response: Response[ResponseBody], convert: T => T)(implicit classTag: ClassTag[T]): ServiceResponse[T] = {
    val statusCode: Int = response.code
    val responseBody: ResponseBody = if (response.isSuccessful) { response.body } else { response.errorBody }
    if (expectedStatusCode.contains(statusCode) || response.isSuccessful) {
      new ServiceResponse[T](buildBody(statusCode, responseBody, convert), response)
    } else {
      throw new CloudException("Invalid status code " + statusCode) {
        setResponse(response)
        setBody(buildError(statusCode, responseBody))
      }
    }
  }

  def buildEmpty(response: Response[Void]): ServiceResponse[T] = {
    val statusCode: Int = response.code
    if (expectedStatusCode.contains(statusCode)) {
      if (new TypeToken[T](getClass) {}.getRawType.isAssignableFrom(classOf[Boolean])) {
        new ServiceResponse[T](response) {
          setBody((statusCode / 100 == 2).asInstanceOf[T])
        }
      } else {
        new ServiceResponse[T](response)
      }
    } else {
      throw new CloudException("Invalid status code " + statusCode) {
        setResponse(response)
      }
    }
  }

  private def buildBody(statusCode: Int, responseBody: ResponseBody, convert: T => T)(implicit classTag: ClassTag[T]): T = {
    if (classTag.runtimeClass eq classOf[Void]) {
      null.asInstanceOf[T]
    } else if (classTag.runtimeClass eq classOf[InputStream]) {
      responseBody.byteStream.asInstanceOf[T]
    } else {
      try {
        convert(objectMapper.readValue[T](responseBody.string, classTag.runtimeClass.asInstanceOf[Class[T]]))

        //TODO handle parsing errors
        //} catch {
        //case e: Throwable => null.asInstanceOf[T]
      } finally {
        responseBody.close()
      }
    }
  }

  private def buildError(statusCode: Int, responseBody: ResponseBody): CloudError = {
    try {
      objectMapper.readValue(responseBody.string, classOf[CloudError])
    } catch {
      case e: Throwable => new CloudError
    } finally {
      responseBody.close()
    }
  }
}
