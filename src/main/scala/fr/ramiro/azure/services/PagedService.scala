package fr.ramiro.azure.services

import com.microsoft.rest.ServiceResponse
import fr.ramiro.azure.model.{ PageResponse, PagedIterator }
import okhttp3.ResponseBody
import retrofit2.Call
import scala.reflect.ClassTag

trait PagedService[T] extends GetService[T] {

  def listInternal: Call[ResponseBody]

  def listNextInternal(nextPageLink: String): Call[ResponseBody]

  def list(implicit classTag: ClassTag[T]): ServiceResponse[Stream[T]] = {
    val response: ServiceResponse[PageResponse[T]] = createServiceResponse(listInternal.execute, buildBodyPaged)

    val pageList: Stream[T] = new PagedIterator[T](response.getBody) {
      def nextPage(nextPageLink: String): PageResponse[T] = {
        createServiceResponse(listNextInternal(nextPageLink).execute, buildBodyPaged).getBody
      }
    }.toStream

    new ServiceResponse[Stream[T]](
      pageList,
      response.getResponse
    )

  }

  private def buildBodyPaged(body: String)(implicit classTag: ClassTag[T]): PageResponse[T] = {
    val typeResult = objectMapper.getTypeFactory.constructParametricType(classOf[PageResponse[T]], classTag.runtimeClass)
    val result = objectMapper.readValue(body, typeResult).asInstanceOf[PageResponse[T]]
    result.updateItems { addParent }
  }
}
