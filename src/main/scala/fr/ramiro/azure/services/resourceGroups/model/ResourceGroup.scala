package fr.ramiro.azure.services.resourceGroups.model

import com.fasterxml.jackson.annotation.JsonIgnore
import fr.ramiro.azure.services.subscriptions.model.Subscription

case class ResourceGroup(
    id: String,
    name: String,
    properties: ResourceGroupProperties,
    location: String,
    tags: Map[String, String]
) {
  @JsonIgnore
  var subscription: Subscription = _
}

case class ResourceGroupProperties(
  provisioningState: String,
  resourceState: String
)

case class ExportTemplateRequestInner(
  resources: Seq[String],
  options: String
)
