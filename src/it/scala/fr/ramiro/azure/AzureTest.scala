package fr.ramiro.azure

import com.typesafe.config.ConfigFactory
import fr.ramiro.azure.model.Subscription
import fr.ramiro.azure.services._
import okhttp3.logging.HttpLoggingInterceptor
import org.scalatest.FunSuite

class AzureTest extends FunSuite {
  test("lists and pages") {
    RetrofitAzure(AzureTokenCredentials(), HttpLoggingInterceptor.Level.BODY).subscriptions.list.foreach { subscription =>
      println(subscription)
      subscription.resourceGroups.list.foreach { resourceGroup =>
        println(resourceGroup)
        resourceGroup.cdnProfiles.list.foreach { cdnProfile =>
          println(cdnProfile)
        }
      }
    }
  }

  test("From existing subscription"){
    val config = ConfigFactory.load()
    val retrofit = RetrofitAzure(AzureTokenCredentials(), HttpLoggingInterceptor.Level.BODY)
    val subscription = Subscription(retrofit, config.getString("subscription"))

    subscription.resourceGroups.list.foreach { resourceGroup =>
      println(resourceGroup)
    }
  }
}
