package fr.ramiro.azure

import fr.ramiro.azure.services.subscriptions._
import okhttp3.logging.HttpLoggingInterceptor
import org.scalatest.FunSuite

import scala.collection.JavaConverters.collectionAsScalaIterableConverter

class AzureTest extends FunSuite {
  //  test("purge") {
  //    Azure(AzureTokenCredentials()).cdn.cdnPurge(getenv("resourceGroupName"), getenv("profileName"), getenv("endpointName"), "/*")
  //  }

  test("subscriptions") {
    Azure(AzureTokenCredentials(), HttpLoggingInterceptor.Level.BODY).subscriptions.list.getBody.asScala.foreach { subscription =>
      println(subscription)
    }
  }
}
