package fr.ramiro.azure.services

import com.microsoft.rest.ServiceResponse
import fr.ramiro.azure.model.ListResponse
import okhttp3.ResponseBody
import retrofit2.{ Call, Response }

import scala.reflect.ClassTag

trait ListService[T] extends GetService[T] {

  def listInternal: Call[ResponseBody]

  def list(implicit classTag: ClassTag[T]): ServiceResponse[Seq[T]] = {
    val response = listDelegate(listInternal.execute)
    new ServiceResponse[Seq[T]](
      response.getBody.value,
      response.getResponse
    )
  }

  private def listDelegate(response: Response[ResponseBody])(implicit classTag: ClassTag[T]): ServiceResponse[ListResponse[T]] = {
    if (response.isSuccessful) {
      new ServiceResponse[ListResponse[T]](buildBodyList(response.body), response)
    } else {
      throw createCloudException(response)
    }
  }

  private def buildBodyList(responseBody: ResponseBody)(implicit classTag: ClassTag[T]): ListResponse[T] = {
    try {
      val typeResult = objectMapper.getTypeFactory.constructParametricType(classOf[ListResponse[T]], classTag.runtimeClass)
      val result = objectMapper.readValue(responseBody.string, typeResult).asInstanceOf[ListResponse[T]]
      result.copy(value = result.value.map { addParent })
    } finally {
      responseBody.close()
    }
  }
}
