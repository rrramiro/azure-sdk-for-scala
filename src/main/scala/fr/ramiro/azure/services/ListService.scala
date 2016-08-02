package fr.ramiro.azure.services

import java.lang.reflect.Type

import com.microsoft.rest.ServiceResponse
import fr.ramiro.azure.model.ListResponse
import fr.ramiro.azure.rest.AzureServiceResponseBuilder
import okhttp3.ResponseBody
import retrofit2.{ Call, Response }

trait ListService[T] extends GetService[T] {
  val listedType: Type

  def listInternal: Call[ResponseBody]

  def list: ServiceResponse[Seq[T]] = {
    val response = listDelegate(listInternal.execute)
    new ServiceResponse[Seq[T]](
      response.getBody.value,
      response.getResponse
    )
  }

  private def listDelegate(response: Response[ResponseBody]): ServiceResponse[ListResponse[T]] = {
    new AzureServiceResponseBuilder[T](mapperAdapter, listedType, 200).buildList(response, addParent)
  }
}
