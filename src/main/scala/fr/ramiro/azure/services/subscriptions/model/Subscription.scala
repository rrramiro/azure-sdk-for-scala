package fr.ramiro.azure.services.subscriptions.model

import com.fasterxml.jackson.annotation.JsonIgnore
import fr.ramiro.azure.Azure

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