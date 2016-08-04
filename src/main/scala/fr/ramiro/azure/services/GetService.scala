package fr.ramiro.azure.services

import com.microsoft.rest.ServiceResponse
import okhttp3.ResponseBody
import retrofit2.Call

import scala.reflect.ClassTag

trait GetService[T] extends BaseService[T] {

  def getInternal(id: String): Call[ResponseBody]

  def get(id: String)(implicit classTag: ClassTag[T]) = {
    val response = createServiceResponse(getInternal(id).execute(), buildBody)
    new ServiceResponse[T](
      addParent(response.getBody),
      response.getResponse
    )
  }

  //TODO handle parsing errors
  private def buildBody(body: String)(implicit classTag: ClassTag[T]): T = {
    objectMapper.readValue[T](body, classTag.runtimeClass.asInstanceOf[Class[T]])
  }
}
