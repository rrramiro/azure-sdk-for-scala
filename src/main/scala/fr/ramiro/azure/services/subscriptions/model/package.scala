package fr.ramiro.azure.services.subscriptions

package object model {

  case class Subscription(
    id: String,
    subscriptionId: String,
    displayName: String,
    state: String,
    subscriptionPolicies: SubscriptionPolicies
  )

  case class SubscriptionPolicies(
    locationPlacementId: String,
    quotaId: String
  )

}
