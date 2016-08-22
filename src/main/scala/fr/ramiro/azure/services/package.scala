package fr.ramiro.azure

import fr.ramiro.azure.model.{ ResourceGroup, Subscription }
import retrofit2.Retrofit

package object services {

  implicit class CdnServiceWrapper(resourceGroup: ResourceGroup) {
    def cdnProfiles = new CdnProfilesService(resourceGroup.retrofit, resourceGroup.subscriptionId, resourceGroup.name)
  }

  implicit class ResourceGroupsServiceWrapper(subscription: Subscription) {
    def resourceGroups = new ResourceGroupsService(subscription.retrofit, subscription.subscriptionId)
  }

  implicit class SubscriptionsServiceWrapper(retrofit: Retrofit) {
    def subscriptions = new SubscriptionsService(retrofit)
  }

}
