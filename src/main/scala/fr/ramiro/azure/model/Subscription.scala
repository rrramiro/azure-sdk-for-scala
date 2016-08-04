package fr.ramiro.azure.model

import com.fasterxml.jackson.annotation.JsonIgnore
import fr.ramiro.azure.Azure

class Subscription(
    val id: String,
    val subscriptionId: String,
    val displayName: String,
    val state: String,
    val subscriptionPolicies: SubscriptionPolicies
) {
  @JsonIgnore
  var azure: Azure = _
}
