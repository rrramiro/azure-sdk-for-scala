package fr.ramiro.azure.services

import fr.ramiro.azure.Azure
import fr.ramiro.azure.model.{ CdnProfile, ResourceGroup }
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http._

class CdnProfilesService(azure: Azure, subscriptionId: String, resourceGroupName: String) extends ListService[CdnProfile] {
  override val objectMapper = azure.objectMapper
  private val cdnProfileServiceInternal = azure.retrofit.create(classOf[CdnProfileServiceInternal])
  override val defaultApiVersion = "2016-04-02"

  override def addParent(child: CdnProfile): CdnProfile = {
    child.azure = azure
    child.subscriptionId = subscriptionId
    child.resourceGroupName = child.resourceGroupName
    child
  }

  override def listInternal: Call[ResponseBody] = cdnProfileServiceInternal.list(subscriptionId, resourceGroupName, defaultApiVersion)

  override def getInternal(id: String): Call[ResponseBody] = cdnProfileServiceInternal.get(subscriptionId, resourceGroupName, id, defaultApiVersion)

  trait CdnProfileServiceInternal {
    @Headers(Array("Content-Type: application/json; charset=utf-8"))
    @GET("/subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/Microsoft.Cdn/Profiles")
    def list(
      @Path("subscriptionId") subscriptionId: String,
      @Path("resourceGroupName") resourceGroupName: String,
      @Query("api-version") apiVersion: String
    ): Call[ResponseBody]

    @Headers(Array("Content-Type: application/json; charset=utf-8"))
    @GET("/subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/Microsoft.Cdn/Profiles/{profileName}")
    def get(
      @Path("subscriptionId") subscriptionId: String,
      @Path("resourceGroupName") resourceGroupName: String,
      @Path("profileName") profileName: String,
      @Query("api-version") apiVersion: String
    ): Call[ResponseBody]
  }
}
