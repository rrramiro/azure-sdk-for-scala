package fr.ramiro.azure.services

import com.fasterxml.jackson.databind.ObjectMapper

trait BaseService[T] {
  val objectMapper: ObjectMapper
  val defaultApiVersion = "2015-11-01"
  val defaultAcceptLanguage = "en-US"
  val defaultUserAgent = s"Azure-SDK-For-Java/${getClass.getPackage.getImplementationVersion} (SubscriptionClient, $defaultApiVersion)"

  def addParent(child: T): T
}
