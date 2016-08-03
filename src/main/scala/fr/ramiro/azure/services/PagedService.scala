package fr.ramiro.azure.services

import com.microsoft.azure.{ Page, PagedList }
import com.microsoft.rest.ServiceResponse
import fr.ramiro.azure.model.PageImpl
import okhttp3.ResponseBody
import retrofit2.Call

import scala.reflect.ClassTag

trait PagedService[T] extends GetService[T] {

  def listInternal: Call[ResponseBody]

  def listNextInternal(nextPageLink: String): Call[ResponseBody]

  def list(implicit classTag: ClassTag[T]): ServiceResponse[PagedList[T]] = {
    val response = createServiceResponse(listInternal.execute, buildBodyPaged)
    new ServiceResponse[PagedList[T]](
      new PagedList[T](response.getBody) {
        def nextPage(nextPageLink: String): Page[T] = {
          createServiceResponse(listNextInternal(nextPageLink).execute, buildBodyPaged).getBody
        }
      },
      response.getResponse
    )
  }

  private def buildBodyPaged(body: String)(implicit classTag: ClassTag[T]): PageImpl[T] = {
    val typeResult = objectMapper.getTypeFactory.constructParametricType(classOf[PageImpl[T]], classTag.runtimeClass)
    val result = objectMapper.readValue(body, typeResult).asInstanceOf[PageImpl[T]]
    result.updateItems { addParent }
  }
}
