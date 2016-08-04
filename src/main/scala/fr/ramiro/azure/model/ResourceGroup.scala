package fr.ramiro.azure.model

import com.fasterxml.jackson.annotation.JsonIgnore
import fr.ramiro.azure.Azure

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
  var azure: Azure = _
}

