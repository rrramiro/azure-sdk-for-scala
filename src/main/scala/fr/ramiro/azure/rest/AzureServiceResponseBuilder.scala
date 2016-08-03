package fr.ramiro.azure.rest

import java.io.InputStream

import com.fasterxml.jackson.databind.ObjectMapper
import com.google.common.reflect.TypeToken
import com.microsoft.azure.{ CloudError, CloudException }
import com.microsoft.rest._
import fr.ramiro.azure.model.{ ListResponse, PageImpl }
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

  def buildList(response: Response[ResponseBody], convert: T => T)(implicit classTag: ClassTag[T]): ServiceResponse[ListResponse[T]] = {
    val statusCode: Int = response.code
    val responseBody: ResponseBody = if (response.isSuccessful) { response.body } else { response.errorBody }
    if (expectedStatusCode.contains(statusCode) || response.isSuccessful) {
      new ServiceResponse[ListResponse[T]](buildBodyList(statusCode, responseBody, convert), response)
    } else {
      throw new CloudException("Invalid status code " + statusCode) {
        setResponse(response)
        setBody(buildError(statusCode, responseBody))
      }
    }
  }

  def buildPaged(response: Response[ResponseBody], convert: T => T)(implicit classTag: ClassTag[T]): ServiceResponse[PageImpl[T]] = {
    val statusCode: Int = response.code
    val responseBody: ResponseBody = if (response.isSuccessful) { response.body } else { response.errorBody }
    if (expectedStatusCode.contains(statusCode) || response.isSuccessful) {
      new ServiceResponse[PageImpl[T]](buildBodyPaged(statusCode, responseBody, convert), response)
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

  private def buildBodyList(statusCode: Int, responseBody: ResponseBody, convert: T => T)(implicit classTag: ClassTag[T]): ListResponse[T] = {
    val body = responseBody.string
    responseBody.close()
    if (body.isEmpty) {
      null
    } else {
      val result = deserializeList[T](body)
      result.copy(value = result.value.map { convert })
    }
  }

  private def buildBodyPaged(statusCode: Int, responseBody: ResponseBody, convert: T => T)(implicit classTag: ClassTag[T]): PageImpl[T] = {
    val body = responseBody.string
    responseBody.close()
    if (body.isEmpty) {
      null
    } else {
      deserializePagedList[T](body).updateItems { convert }
    }
  }

  private def buildBody(statusCode: Int, responseBody: ResponseBody, convert: T => T)(implicit classTag: ClassTag[T]): T = {
    if (classTag.runtimeClass eq classOf[Void]) {
      null.asInstanceOf[T]
    } else if (classTag.runtimeClass eq classOf[InputStream]) {
      responseBody.byteStream.asInstanceOf[T]
    } else {
      try {
        convert(deserialize[T](responseBody.string))
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
      deserialize(responseBody.string)
    } catch {
      case e: Throwable => new CloudError
    } finally {
      responseBody.close()
    }
  }

  private def deserialize[U](json: String)(implicit classTag: ClassTag[U]): U = {
    objectMapper.readValue(json, classTag.runtimeClass).asInstanceOf[U]
  }

  def deserializePagedList[T](body: String)(implicit classTag: ClassTag[T]): PageImpl[T] = {
    val typeResult = objectMapper.getTypeFactory.constructParametricType(classOf[PageImpl[T]], classTag.runtimeClass)
    objectMapper.readValue(body, typeResult).asInstanceOf[PageImpl[T]]
  }

  def deserializeList[T](body: String)(implicit classTag: ClassTag[T]): ListResponse[T] = {
    val typeResult = objectMapper.getTypeFactory.constructParametricType(classOf[ListResponse[T]], classTag.runtimeClass)
    objectMapper.readValue(body, typeResult).asInstanceOf[ListResponse[T]]
  }

}
