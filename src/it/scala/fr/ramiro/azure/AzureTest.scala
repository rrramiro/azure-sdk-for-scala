package fr.ramiro.azure

import fr.ramiro.azure.services._
import okhttp3.logging.HttpLoggingInterceptor
import org.scalatest.FunSuite

class AzureTest extends FunSuite {
  test("subscriptions") {
    Azure(AzureTokenCredentials(), HttpLoggingInterceptor.Level.BODY).subscriptions.list.foreach { subscription =>
      println(subscription)
      subscription.resourceGroups.list.foreach { resourceGroup =>
        println(resourceGroup)
        resourceGroup.cdnProfiles.list.foreach { cdnProfile =>
          println(cdnProfile)
        }
      }
    }
  }
}
