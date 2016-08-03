package fr.ramiro.azure.services

import okhttp3.ResponseBody
import retrofit2.Call

import scala.reflect.ClassTag

trait GetService[T] extends BaseService[T] {

  def getInternal(id: String): Call[ResponseBody]

  def get(id: String)(implicit classTag: ClassTag[T]) = {
    createServiceResponse(getInternal(id).execute(), buildBody)
  }

  //TODO handle parsing errors
  private def buildBody(body: String)(implicit classTag: ClassTag[T]): T = {
    addParent(objectMapper.readValue[T](body, classTag.runtimeClass.asInstanceOf[Class[T]]))
  }
}
