package fr.ramiro.azure.services.cdn

import com.microsoft.rest.ServiceResponse
import fr.ramiro.azure.Azure
import fr.ramiro.azure.services.BaseService
import fr.ramiro.azure.services.cdn.model.CdnEndpoint
import okhttp3.ResponseBody
import retrofit2.{ Call, Response }
import retrofit2.http._

class CdnEndpointsService(val azure: Azure, resourceGroupName: String, profileName: String) extends BaseService[CdnEndpoint] {
  override val objectMapper = azure.objectMapper
  override def addParent(child: CdnEndpoint): CdnEndpoint = child

  private case class PurgeRequest(ContentPaths: Seq[String])

  val cdnInternal = azure.retrofit.create(classOf[CdnServiceInternal])

  private def purgeDelegate(response: Response[ResponseBody]) = {
    createServiceResponse(response, _ => response.code() == 202)
  }

  def cdnPurge(endpointName: String, contentPaths: String*): ServiceResponse[Boolean] = purgeDelegate(
    cdnInternal.purge(
      azure.subscriptionId,
      resourceGroupName,
      profileName,
      endpointName,
      defaultApiVersion,
      new PurgeRequest(contentPaths)
    ).execute()
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
