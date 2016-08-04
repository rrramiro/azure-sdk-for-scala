package fr.ramiro.azure.model

import com.fasterxml.jackson.annotation.JsonIgnore
import fr.ramiro.azure.Azure

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
  var azure: Azure = _
}
