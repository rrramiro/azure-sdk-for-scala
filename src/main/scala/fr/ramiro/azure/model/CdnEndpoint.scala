package fr.ramiro.azure.model

import com.fasterxml.jackson.annotation.JsonIgnore
import fr.ramiro.azure.Azure

class CdnEndpoint {
  @JsonIgnore
  var profileName: String = _
  @JsonIgnore
  var subscriptionId: String = _
  @JsonIgnore
  var resourceGroupName: String = _
  @JsonIgnore
  var azure: Azure = _
}
