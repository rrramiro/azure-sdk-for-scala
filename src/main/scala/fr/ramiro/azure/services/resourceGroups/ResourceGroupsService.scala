package fr.ramiro.azure.services.resourceGroups

import com.google.common.reflect.TypeToken
import com.microsoft.azure.{Page, PagedList}
import com.microsoft.rest.ServiceResponse
import fr.ramiro.azure.model.PageImpl1
import fr.ramiro.azure.rest.AzureServiceResponseBuilder
import fr.ramiro.azure.services.subscriptions.model.Subscription
import okhttp3.ResponseBody
import retrofit2.{Call, Response}
import retrofit2.http.{POST, Url, _}
import fr.ramiro.azure.services.resourceGroups.model._

class ResourceGroupsService(subscription: Subscription) {
  val azure = subscription.azure
  val resourceGroupsInternal = azure.retrofit.create(classOf[ResourceGroupsInternal])
  val defaultApiVersion = "2015-11-01"
  val defaultAcceptLanguage = "en-US"
  val defaultUserAgent = s"Azure-SDK-For-Java/${getClass.getPackage.getImplementationVersion} (SubscriptionClient, $defaultApiVersion)"


  def list = {
    val response = listDelegate(resourceGroupsInternal.list(subscription.subscriptionId, null, null, defaultApiVersion, defaultAcceptLanguage, defaultUserAgent).execute())
    new ServiceResponse[PagedList[ResourceGroupInner]](
      new PagedList[ResourceGroupInner](response.getBody) {
        def nextPage(nextPageLink: String): Page[ResourceGroupInner] = {
          listNext(nextPageLink).getBody
        }
      },
      response.getResponse
    )
  }

  def listNext(nextPageLink: String) = {
    listNextDelegate(resourceGroupsInternal.listNext(nextPageLink, defaultAcceptLanguage, defaultUserAgent).execute)
  }

  def addParent(a: ResourceGroupInner): ResourceGroupInner = {
    a.subscription = subscription
    a
  }

  private def listDelegate(response: Response[ResponseBody]): ServiceResponse[PageImpl1[ResourceGroupInner]] = {
    new AzureServiceResponseBuilder[ResourceGroupInner](azure.mapperAdapter, new TypeToken[PageImpl1[ResourceGroupInner]]() {}.getType, 200).buildPaged(response, addParent)
  }

  private def listNextDelegate(response: Response[ResponseBody]): ServiceResponse[PageImpl1[ResourceGroupInner]] = {
    new AzureServiceResponseBuilder[ResourceGroupInner](azure.mapperAdapter, new TypeToken[PageImpl1[ResourceGroupInner]]() {}.getType, 200).buildPaged(response, addParent)
  }


  trait ResourceGroupsInternal {
    @Headers(Array("Content-Type: application/json; charset=utf-8"))
    @GET("subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/resources")
    def listResources(@Path("resourceGroupName") resourceGroupName: String, @Path("subscriptionId") subscriptionId: String, @Query("$filter") filter: String, @Query("$top") top: Integer, @Query("api-version") apiVersion: String, @Header("accept-language") acceptLanguage: String, @Header("User-Agent") userAgent: String): Call[ResponseBody]

    @Headers(Array("Content-Type: application/json; charset=utf-8"))
    @HEAD("subscriptions/{subscriptionId}/resourcegroups/{resourceGroupName}")
    def checkExistence(@Path("resourceGroupName") resourceGroupName: String, @Path("subscriptionId") subscriptionId: String, @Query("api-version") apiVersion: String, @Header("accept-language") acceptLanguage: String, @Header("User-Agent") userAgent: String): Call[Void]

    @Headers(Array("Content-Type: application/json; charset=utf-8"))
    @PUT("subscriptions/{subscriptionId}/resourcegroups/{resourceGroupName}")
    def createOrUpdate(
      @Path("resourceGroupName") resourceGroupName: String,
      @Path("subscriptionId") subscriptionId: String,
      @Body parameters: ResourceGroupInner,
      @Query("api-version") apiVersion: String,
      @Header("accept-language") acceptLanguage: String,
      @Header("User-Agent") userAgent: String
    ): Call[ResponseBody]

    @Headers(Array("Content-Type: application/json; charset=utf-8"))
    @HTTP(path = "subscriptions/{subscriptionId}/resourcegroups/{resourceGroupName}", method = "DELETE", hasBody = true)
    def delete(@Path("resourceGroupName") resourceGroupName: String, @Path("subscriptionId") subscriptionId: String, @Query("api-version") apiVersion: String, @Header("accept-language") acceptLanguage: String, @Header("User-Agent") userAgent: String): Call[ResponseBody]

    @Headers(Array("Content-Type: application/json; charset=utf-8"))
    @HTTP(path = "subscriptions/{subscriptionId}/resourcegroups/{resourceGroupName}", method = "DELETE", hasBody = true)
    def beginDelete(@Path("resourceGroupName") resourceGroupName: String, @Path("subscriptionId") subscriptionId: String, @Query("api-version") apiVersion: String, @Header("accept-language") acceptLanguage: String, @Header("User-Agent") userAgent: String): Call[ResponseBody]

    @Headers(Array("Content-Type: application/json; charset=utf-8"))
    @GET("subscriptions/{subscriptionId}/resourcegroups/{resourceGroupName}")
    def get(@Path("resourceGroupName") resourceGroupName: String, @Path("subscriptionId") subscriptionId: String, @Query("api-version") apiVersion: String, @Header("accept-language") acceptLanguage: String, @Header("User-Agent") userAgent: String): Call[ResponseBody]

    @Headers(Array("Content-Type: application/json; charset=utf-8"))
    @PATCH("subscriptions/{subscriptionId}/resourcegroups/{resourceGroupName}")
    def patch(
      @Path("resourceGroupName") resourceGroupName: String,
      @Path("subscriptionId") subscriptionId: String,
      @Body parameters: ResourceGroupInner,
      @Query("api-version") apiVersion: String,
      @Header("accept-language") acceptLanguage: String,
      @Header("User-Agent") userAgent: String
    ): Call[ResponseBody]

    @Headers(Array("Content-Type: application/json; charset=utf-8"))
    @POST("subscriptions/{subscriptionId}/resourcegroups/{resourceGroupName}/exportTemplate")
    def exportTemplate(
      @Path("resourceGroupName") resourceGroupName: String,
      @Path("subscriptionId") subscriptionId: String,
      @Body parameters: ExportTemplateRequestInner,
      @Query("api-version") apiVersion: String,
      @Header("accept-language") acceptLanguage: String,
      @Header("User-Agent") userAgent: String
    ): Call[ResponseBody]

    @Headers(Array("Content-Type: application/json; charset=utf-8"))
    @GET("subscriptions/{subscriptionId}/resourcegroups")
    def list(
      @Path("subscriptionId") subscriptionId: String,
      @Query("$filter") filter: String,
      @Query("$top") top: Integer,
      @Query("api-version") apiVersion: String,
      @Header("accept-language") acceptLanguage: String,
      @Header("User-Agent") userAgent: String
    ): Call[ResponseBody]

    @Headers(Array("Content-Type: application/json; charset=utf-8"))
    @GET
    def listResourcesNext(@Url nextPageLink: String, @Header("accept-language") acceptLanguage: String, @Header("User-Agent") userAgent: String): Call[ResponseBody]

    @Headers(Array("Content-Type: application/json; charset=utf-8"))
    @GET
    def listNext(@Url nextPageLink: String, @Header("accept-language") acceptLanguage: String, @Header("User-Agent") userAgent: String): Call[ResponseBody]
  }
}
