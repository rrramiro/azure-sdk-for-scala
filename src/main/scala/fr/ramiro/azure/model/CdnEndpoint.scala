package fr.ramiro.azure.model

import com.fasterxml.jackson.annotation.JsonIgnore
import retrofit2.Retrofit

class CdnEndpoint {
  @JsonIgnore
  var profileName: String = _
  @JsonIgnore
  var subscriptionId: String = _
  @JsonIgnore
  var resourceGroupName: String = _
  @JsonIgnore
  var retrofit: Retrofit = _
}
