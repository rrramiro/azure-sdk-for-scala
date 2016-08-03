package fr.ramiro.azure.services

import com.microsoft.azure.{ Page, PagedList }
import com.microsoft.rest.ServiceResponse
import fr.ramiro.azure.model.PageImpl
import fr.ramiro.azure.rest.AzureServiceResponseBuilder
import okhttp3.ResponseBody
import retrofit2.{ Call, Response }
import scala.reflect.ClassTag

trait PagedService[T] extends GetService[T] {

  def listInternal: Call[ResponseBody]

  def listNextInternal(nextPageLink: String): Call[ResponseBody]

  def list(implicit classTag: ClassTag[T]): ServiceResponse[PagedList[T]] = {
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

  private def listNext(nextPageLink: String)(implicit classTag: ClassTag[T]): ServiceResponse[PageImpl[T]] = {
    listDelegate(listNextInternal(nextPageLink).execute)
  }

  private def listDelegate(response: Response[ResponseBody])(implicit classTag: ClassTag[T]): ServiceResponse[PageImpl[T]] = {
    new AzureServiceResponseBuilder[T](objectMapper, 200).buildPaged(response, addParent)
  }
}
