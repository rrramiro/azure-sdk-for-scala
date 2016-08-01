package fr.ramiro.azure.services.cdn

import com.google.common.reflect.TypeToken
import com.microsoft.azure.{ AzureServiceResponseBuilder, CloudException }
import com.microsoft.rest.ServiceResponse
import fr.ramiro.azure.Azure
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http._

class CdnService(azure: Azure) {
  val defaultApiVersion = "2016-04-02"
  val cdnInternal = azure.retrofit.create(classOf[CdnInternal])
  val purgeStatusCode = 202

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

  private def purgeDelegate(call: Call[ResponseBody]) = {
    new AzureServiceResponseBuilder[Void, CloudException](azure.mapperAdapter)
      .register(purgeStatusCode, new TypeToken[Void] {}.getType)
      .registerError(classOf[CloudException])
      .build(call.execute())
  }

  private case class PurgeRequest(ContentPaths: Seq[String])

  trait CdnInternal {
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
