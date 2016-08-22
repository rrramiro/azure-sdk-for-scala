package fr.ramiro.azure.services

import fr.ramiro.azure.model.CdnEndpoint
import retrofit2.http._
import retrofit2.{ Response, Retrofit }

class CdnEndpointsService(retrofit: Retrofit, subscriptionId: String, resourceGroupName: String, profileName: String) extends BaseService {
  val internal = retrofit.create(classOf[CdnServiceInternal])

  def addParent(child: CdnEndpoint): CdnEndpoint = {
    child.retrofit = retrofit
    child.subscriptionId = subscriptionId
    child.resourceGroupName = resourceGroupName
    child.profileName = profileName
    child
  }

  def purge(endpointName: String, contentPaths: String*): Boolean = internal.purge(
    subscriptionId,
    resourceGroupName,
    profileName,
    endpointName,
    defaultApiVersion,
    PurgeRequest(contentPaths)
  ).code() == 202

  private case class PurgeRequest(ContentPaths: Seq[String])

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
    ): Response[Void]
  }

}
