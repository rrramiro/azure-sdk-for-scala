package fr.ramiro.azure.services

import java.lang.reflect.Type

import com.microsoft.rest.ServiceResponse
import fr.ramiro.azure.rest.AzureServiceResponseBuilder
import okhttp3.ResponseBody
import retrofit2.{ Call, Response }

trait GetService[T] extends BaseService[T] {
  val getType: Type

  def getInternal(id: String): Call[ResponseBody]

  def get(id: String) = {
    getDelegate(getInternal(id).execute())
  }

  private def getDelegate(response: Response[ResponseBody]): ServiceResponse[T] = {
    new AzureServiceResponseBuilder[T](azure.mapperAdapter, getType, 200).build(response, addParent)
  }
}
