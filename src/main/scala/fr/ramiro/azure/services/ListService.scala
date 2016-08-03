package fr.ramiro.azure.services

import com.microsoft.rest.ServiceResponse
import fr.ramiro.azure.model.ListResponse
import okhttp3.ResponseBody
import retrofit2.Call

import scala.reflect.ClassTag

trait ListService[T] extends GetService[T] {

  def listInternal: Call[ResponseBody]

  def list(implicit classTag: ClassTag[T]): ServiceResponse[Seq[T]] = {
    val response = createServiceResponse[ListResponse[T]](listInternal.execute, buildBodyList)
    new ServiceResponse[Seq[T]](
      response.getBody.value,
      response.getResponse
    )
  }

  private def buildBodyList(body: String)(implicit classTag: ClassTag[T]): ListResponse[T] = {
    val typeResult = objectMapper.getTypeFactory.constructParametricType(classOf[ListResponse[T]], classTag.runtimeClass)
    val result = objectMapper.readValue(body, typeResult).asInstanceOf[ListResponse[T]]
    result.copy(value = result.value.map { addParent })
  }
}
