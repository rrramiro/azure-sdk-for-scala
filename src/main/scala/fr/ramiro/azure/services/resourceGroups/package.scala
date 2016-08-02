package fr.ramiro.azure.services

import fr.ramiro.azure.services.subscriptions.model.Subscription

package object resourceGroups {

  implicit class ResourceGroupsServiceWrapper(subscription: Subscription) {
    def resourceGroups = new ResourceGroupsService(subscription.azure, subscription.subscriptionId)
  }

}
