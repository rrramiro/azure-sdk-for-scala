package fr.ramiro.azure.services.cdn.model

import com.fasterxml.jackson.annotation.JsonIgnore
import fr.ramiro.azure.services.resourceGroups.model.{ ResourceGroup, ResourceGroupProperties }

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
  var resourceGroup: ResourceGroup = _
}

class CdnEndpoint {

}