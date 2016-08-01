package fr.ramiro.azure.services.subscriptions

import com.fasterxml.jackson.annotation.JsonIgnore
import fr.ramiro.azure.Azure

package object model {

  case class Subscription(
      id: String,
      subscriptionId: String,
      displayName: String,
      state: String,
      subscriptionPolicies: SubscriptionPolicies
  ) {
    @JsonIgnore
    var azure: Azure = _
  }

  case class SubscriptionPolicies(
    locationPlacementId: String,
    quotaId: String
  )

}
