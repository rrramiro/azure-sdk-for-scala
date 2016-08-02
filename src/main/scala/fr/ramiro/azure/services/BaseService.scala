package fr.ramiro.azure.services

import com.microsoft.rest.serializer.JacksonMapperAdapter

trait BaseService[T] {
  val mapperAdapter: JacksonMapperAdapter
  val defaultApiVersion = "2015-11-01"
  val defaultAcceptLanguage = "en-US"
  val defaultUserAgent = s"Azure-SDK-For-Java/${getClass.getPackage.getImplementationVersion} (SubscriptionClient, $defaultApiVersion)"

  def addParent(child: T): T
}
