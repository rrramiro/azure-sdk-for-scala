package fr.ramiro.azure.model

import com.fasterxml.jackson.annotation.JsonIgnore
import retrofit2.Retrofit

case class ResourceGroup(
    id: String,
    name: String,
    properties: ResourceGroupProperties,
    location: String,
    tags: Map[String, String]
) {
  @JsonIgnore
  var subscriptionId: String = _
  @JsonIgnore
  var retrofit: Retrofit = _
}

