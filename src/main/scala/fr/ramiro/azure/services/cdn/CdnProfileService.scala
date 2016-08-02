package fr.ramiro.azure.services.cdn

import java.lang.reflect.Type

import com.google.common.reflect.TypeToken
import fr.ramiro.azure.Azure
import fr.ramiro.azure.model.PageImpl
import fr.ramiro.azure.services.ListService
import fr.ramiro.azure.services.cdn.model.CdnProfile
import fr.ramiro.azure.services.resourceGroups.model.ResourceGroup
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http._

class CdnProfileService(resourceGroup: ResourceGroup) extends ListService[CdnProfile] {
  val azure: Azure = resourceGroup.subscription.azure
  override val mapperAdapter = azure.mapperAdapter
  private val cdnProfileServiceInternal = azure.retrofit.create(classOf[CdnProfileServiceInternal])
  override val defaultApiVersion = "2016-04-02"
  override val getType: Type = new TypeToken[CdnProfile]() {}.getType
  override val listedType: Type = new TypeToken[PageImpl[CdnProfile]]() {}.getType

  override def addParent(child: CdnProfile): CdnProfile = child

  override def listInternal: Call[ResponseBody] = cdnProfileServiceInternal.list(resourceGroup.subscription.subscriptionId, resourceGroup.name, defaultApiVersion)

  override def getInternal(id: String): Call[ResponseBody] = cdnProfileServiceInternal.get(resourceGroup.subscription.subscriptionId, resourceGroup.name, id, defaultApiVersion)

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
