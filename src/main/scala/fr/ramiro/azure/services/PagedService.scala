package fr.ramiro.azure.services

import com.microsoft.azure.{ CloudException, Page, PagedList }
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
    if (response.isSuccessful) {
      new ServiceResponse[PageImpl[T]](buildBodyPaged(response.body), response)
    } else {
      throw createCloudException(response)
    }
  }

  private def buildBodyPaged(responseBody: ResponseBody)(implicit classTag: ClassTag[T]): PageImpl[T] = {
    try {
      val typeResult = objectMapper.getTypeFactory.constructParametricType(classOf[PageImpl[T]], classTag.runtimeClass)
      val result = objectMapper.readValue(responseBody.string, typeResult).asInstanceOf[PageImpl[T]]
      result.updateItems { addParent }
    } finally {
      responseBody.close()
    }
  }
}
