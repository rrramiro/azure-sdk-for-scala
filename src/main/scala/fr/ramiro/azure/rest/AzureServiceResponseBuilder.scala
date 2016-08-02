package fr.ramiro.azure.rest

import com.microsoft.rest.serializer.JacksonMapperAdapter
import java.io.InputStream
import java.lang.reflect.Type

import com.google.common.reflect.TypeToken
import com.microsoft.azure.{ CloudError, CloudException }
import com.microsoft.rest._
import fr.ramiro.azure.model.PageImpl
import okhttp3.ResponseBody
import retrofit2.Response

class AzureServiceResponseBuilder[T](
    mapperAdapter: JacksonMapperAdapter,
    resultType: Type,
    expectedStatusCode: Int*
) {

  def build(response: Response[ResponseBody], convert: T => T): ServiceResponse[T] = {
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

  def buildPaged(response: Response[ResponseBody], convert: T => T): ServiceResponse[PageImpl[T]] = {
    val statusCode: Int = response.code
    val responseBody: ResponseBody = if (response.isSuccessful) { response.body } else { response.errorBody }
    if (expectedStatusCode.contains(statusCode) || response.isSuccessful) {
      val paged = buildBodyPaged(statusCode, responseBody, convert)
      new ServiceResponse[PageImpl[T]](paged, response)
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

  private def buildBodyPaged(statusCode: Int, responseBody: ResponseBody, convert: T => T): PageImpl[T] = {
    val body = responseBody.string
    responseBody.close()
    if (body.isEmpty) {
      null
    } else {
      mapperAdapter.deserialize[PageImpl[T]](body, resultType).updateItems { convert }
    }
  }

  private def buildBody(statusCode: Int, responseBody: ResponseBody, convert: T => T): T = {
    if (resultType eq classOf[Void]) {
      null.asInstanceOf[T]
    } else if (resultType eq classOf[InputStream]) {
      responseBody.byteStream.asInstanceOf[T]
    } else {
      try {
        convert(mapperAdapter.deserialize[T](responseBody.string, resultType))
      } catch {
        case e: Throwable => null.asInstanceOf[T]
      } finally {
        responseBody.close()
      }
    }
  }

  private def buildError(statusCode: Int, responseBody: ResponseBody): CloudError = {
    try {
      mapperAdapter.deserialize(responseBody.string, classOf[CloudError])
    } catch {
      case e: Throwable => new CloudError
    } finally {
      responseBody.close()
    }
  }
}
