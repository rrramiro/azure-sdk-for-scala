package fr.ramiro.azure.services

import java.lang.reflect.Type

import com.microsoft.azure.{ Page, PagedList }
import com.microsoft.rest.ServiceResponse
import fr.ramiro.azure.model.PageImpl
import fr.ramiro.azure.rest.AzureServiceResponseBuilder
import okhttp3.ResponseBody
import retrofit2.{ Call, Response }

trait PagedService[T] extends GetService[T] {
  val pagedType: Type

  def listInternal: Call[ResponseBody]

  def listNextInternal(nextPageLink: String): Call[ResponseBody]

  def list: ServiceResponse[PagedList[T]] = {
    val response = listDelegate(listInternal.execute)
    new ServiceResponse[PagedList[T]](
      new PagedList[T](response.getBody) {
        def nextPage(nextPageLink: String): Page[T] = {
          listNext(nextPageLink).getBody
        }
      },
      response.getResponse
    )
  }

  private def listNext(nextPageLink: String): ServiceResponse[PageImpl[T]] = {
    listDelegate(listNextInternal(nextPageLink).execute)
  }

  private def listDelegate(response: Response[ResponseBody]): ServiceResponse[PageImpl[T]] = {
    new AzureServiceResponseBuilder[T](objectMapper, pagedType, 200).buildPaged(response, addParent)
  }
}
