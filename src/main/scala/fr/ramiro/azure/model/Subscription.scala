package fr.ramiro.azure.model

import com.fasterxml.jackson.annotation.JsonIgnore
import retrofit2.Retrofit

class Subscription(
    val id: String,
    val subscriptionId: String,
    val displayName: String,
    val state: String,
    val subscriptionPolicies: SubscriptionPolicies
) {
  @JsonIgnore
  var retrofit: Retrofit = _
}

object Subscription {
  def apply(retrofitParam: Retrofit, subscriptionId: String): Subscription = {
    new Subscription(null, subscriptionId, null, null, SubscriptionPolicies(null, null)) {
      this.retrofit = retrofitParam
    }
  }
}
