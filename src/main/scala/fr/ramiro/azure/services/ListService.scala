package fr.ramiro.azure.services

import java.lang.reflect.Type
import java.util

import com.microsoft.azure.{ Page, PagedList }
import com.microsoft.rest.ServiceResponse
import fr.ramiro.azure.model.PageImpl
import fr.ramiro.azure.rest.AzureServiceResponseBuilder
import okhttp3.ResponseBody
import retrofit2.{ Call, Response }

trait ListService[T] extends GetService[T] {
  val listedType: Type

  def listInternal: Call[ResponseBody]

  def list: ServiceResponse[PagedList[T]] = {
    val response = listDelegate(listInternal.execute)
    new ServiceResponse[PagedList[T]](
      new PagedList[T](response.getBody) {
        def nextPage(nextPageLink: String): Page[T] = new Page[T] {
          override def getNextPageLink: String = ""
          override def getItems: util.List[T] = new util.ArrayList[T]()
        }
      },
      response.getResponse
    )
  }

  private def listDelegate(response: Response[ResponseBody]): ServiceResponse[PageImpl[T]] = {
    new AzureServiceResponseBuilder[T](mapperAdapter, listedType, 200).buildList(response, addParent)
  }
}
