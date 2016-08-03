package fr.ramiro.azure.services

import com.microsoft.rest.ServiceResponse
import fr.ramiro.azure.rest.AzureServiceResponseBuilder
import okhttp3.ResponseBody
import retrofit2.{ Call, Response }
import scala.reflect.ClassTag

trait GetService[T] extends BaseService[T] {

  def getInternal(id: String): Call[ResponseBody]

  def get(id: String)(implicit classTag: ClassTag[T]) = {
    getDelegate(getInternal(id).execute())
  }

  private def getDelegate(response: Response[ResponseBody])(implicit classTag: ClassTag[T]): ServiceResponse[T] = {
    new AzureServiceResponseBuilder[T](objectMapper, 200).build(response, addParent)
  }
}
