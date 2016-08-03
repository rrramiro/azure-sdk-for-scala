package fr.ramiro.azure.services

import java.lang.reflect.Type

import com.microsoft.rest.ServiceResponse
import fr.ramiro.azure.model.ListResponse
import fr.ramiro.azure.rest.AzureServiceResponseBuilder
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
    new AzureServiceResponseBuilder[T](objectMapper, null, 200).buildList(response, addParent)
  }
}
