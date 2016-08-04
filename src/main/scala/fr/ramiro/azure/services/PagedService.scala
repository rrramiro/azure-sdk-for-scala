package fr.ramiro.azure.services

import com.microsoft.rest.ServiceResponse
import fr.ramiro.azure.model.PageResponse
import okhttp3.ResponseBody
import retrofit2.Call
import scala.reflect.ClassTag

trait PagedService[T] extends GetService[T] {

  def listInternal: Call[ResponseBody]

  def listNextInternal(nextPageLink: String): Call[ResponseBody]

  def list(implicit classTag: ClassTag[T]): ServiceResponse[Stream[T]] = {
    val response: ServiceResponse[PageResponse[T]] = createServiceResponse(listInternal.execute, buildBodyPaged)

    def pageStream(page: Option[PageResponse[T]]): Stream[T] = page match {
      case Some(aPage) => aPage.value.map { addParent }.toStream #::: pageStream(
        if (aPage.hasNextPage) {
          Some(createServiceResponse(listNextInternal(aPage.nextLink).execute, buildBodyPaged).getBody)
        } else
          None
      )
      case _ => Stream.empty
    }

    new ServiceResponse[Stream[T]](
      pageStream(Some(response.getBody)),
      response.getResponse
    )
  }

  private def buildBodyPaged(body: String)(implicit classTag: ClassTag[T]): PageResponse[T] = {
    val typeResult = objectMapper.getTypeFactory.constructParametricType(classOf[PageResponse[T]], classTag.runtimeClass)
    objectMapper.readValue(body, typeResult).asInstanceOf[PageResponse[T]]
  }
}
