package fr.ramiro.azure.services

import fr.ramiro.azure.Azure

trait BaseService[T] {
  val azure: Azure
  val defaultApiVersion = "2015-11-01"
  val defaultAcceptLanguage = "en-US"
  val defaultUserAgent = s"Azure-SDK-For-Java/${getClass.getPackage.getImplementationVersion} (SubscriptionClient, $defaultApiVersion)"

  def addParent(child: T): T
}
