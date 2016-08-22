package fr.ramiro.azure.model

import com.fasterxml.jackson.annotation.JsonIgnore
import retrofit2.Retrofit

case class CdnProfile(
    id: String,
    name: String,
    `type`: String,
    location: String,
    tags: Map[String, String],
    properties: ResourceGroupProperties
//sku : Map[String, String]
) {
  @JsonIgnore
  var subscriptionId: String = _
  @JsonIgnore
  var resourceGroupName: String = _
  @JsonIgnore
  var retrofit: Retrofit = _
}
