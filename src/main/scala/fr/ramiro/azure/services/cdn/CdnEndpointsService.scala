package fr.ramiro.azure.services.cdn

import com.google.common.reflect.TypeToken
import com.microsoft.azure.CloudException
import com.microsoft.rest.ServiceResponse
import fr.ramiro.azure.Azure
import fr.ramiro.azure.rest.AzureServiceResponseBuilder
import fr.ramiro.azure.services.BaseService
import fr.ramiro.azure.services.cdn.model.{ CdnEndpoint, CdnProfile }
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http._

class CdnEndpointsService(cdnProfile: CdnProfile) extends BaseService[CdnEndpoint] {
  val azure: Azure = cdnProfile.resourceGroup.subscription.azure
  override val mapperAdapter = azure.mapperAdapter
  override def addParent(child: CdnEndpoint): CdnEndpoint = child

  private case class PurgeRequest(ContentPaths: Seq[String])

  val cdnInternal = azure.retrofit.create(classOf[CdnServiceInternal])

  private def purgeDelegate(call: Call[ResponseBody]) = {
    new AzureServiceResponseBuilder[Void](
      azure.mapperAdapter,
      new TypeToken[Void]() {}.getType,
      202
    ).build(call.execute(), identity) //TODO replace identity
  }

  @throws(classOf[CloudException])
  def cdnPurge(resourceGroupName: String, profileName: String, endpointName: String, contentPaths: String*): ServiceResponse[Void] = purgeDelegate(
    cdnInternal.purge(
      azure.subscriptionId,
      resourceGroupName,
      profileName,
      endpointName,
      defaultApiVersion,
      new PurgeRequest(contentPaths)
    )
  )

  trait CdnServiceInternal {
    /**
     * @param subscriptionId	Azure Subscription ID.
     * @param resourceGroupName	Name of the resource group within the Azure subscription.
     * @param profileName	The name of the profile within the specified resource group.
     * @param endpointName	The name of the endpoint.
     * @param apiVersion	Version of the API to be used with this request. Currently 2016-04-02.
     */
    @Headers(Array("Content-Type: application/json; charset=utf-8"))
    @POST("/subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/Microsoft.Cdn/Profiles/{profileName}/endpoints/{endpointName}/purge")
    def purge(
      @Path("subscriptionId") subscriptionId: String,
      @Path("resourceGroupName") resourceGroupName: String,
      @Path("profileName") profileName: String,
      @Path("endpointName") endpointName: String,
      @Query("api-version") apiVersion: String,
      @Body purgeParam: PurgeRequest
    ): Call[ResponseBody]
  }

}
