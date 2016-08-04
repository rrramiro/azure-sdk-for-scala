package fr.ramiro.azure

import fr.ramiro.azure.model.{ ResourceGroup, Subscription }

package object services {

  implicit class CdnServiceWrapper(resourceGroup: ResourceGroup) {
    def cdnProfiles = new CdnProfilesService(resourceGroup.azure, resourceGroup.subscriptionId, resourceGroup.name)
  }

  implicit class ResourceGroupsServiceWrapper(subscription: Subscription) {
    def resourceGroups = new ResourceGroupsService(subscription.azure, subscription.subscriptionId)
  }

  implicit class SubscriptionsServiceWrapper(azure: Azure) {
    def subscriptions = new SubscriptionsService(azure)
  }

}
