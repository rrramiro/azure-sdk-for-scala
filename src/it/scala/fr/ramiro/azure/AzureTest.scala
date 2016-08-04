package fr.ramiro.azure

import fr.ramiro.azure.services._
import okhttp3.logging.HttpLoggingInterceptor
import org.scalatest.FunSuite

class AzureTest extends FunSuite {
  test("subscriptions") {
    Azure(AzureTokenCredentials(), HttpLoggingInterceptor.Level.BODY).subscriptions.list.getBody.foreach { subscription =>
      println(subscription)
      subscription.resourceGroups.list.getBody.foreach { resourceGroup =>
        println(resourceGroup)
        resourceGroup.cdnProfiles.list.getBody.foreach { cdnProfile =>
          println(cdnProfile)
        }
      }
    }
  }
}
