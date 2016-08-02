package fr.ramiro.azure.services

import fr.ramiro.azure.services.resourceGroups.model.ResourceGroup

package object cdn {
  implicit class CdnServiceWrapper(resourceGroup: ResourceGroup) {
    def cdnProfiles = new CdnProfileService(resourceGroup)
  }
}
