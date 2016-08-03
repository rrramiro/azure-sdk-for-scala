package fr.ramiro.azure.services.resourceGroups

import java.lang.reflect.Type

import com.google.common.reflect.TypeToken
import fr.ramiro.azure.Azure
import fr.ramiro.azure.model.PageResponse
import fr.ramiro.azure.services.PagedService
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http._
import fr.ramiro.azure.services.resourceGroups.model._

class ResourceGroupsService(azure: Azure, subscriptionId: String) extends PagedService[ResourceGroup] {
  override val objectMapper = azure.objectMapper
  val resourceGroupsInternal = azure.retrofit.create(classOf[ResourceGroupsInternal])
  val pagedType: Type = new TypeToken[PageResponse[ResourceGroup]]() {}.getType
  val getType: Type = new TypeToken[ResourceGroup]() {}.getType

  override def listInternal: Call[ResponseBody] = resourceGroupsInternal.list(subscriptionId, null, null, defaultApiVersion, defaultAcceptLanguage, defaultUserAgent)

  override def listNextInternal(nextPageLink: String): Call[ResponseBody] = resourceGroupsInternal.listNext(nextPageLink, defaultAcceptLanguage, defaultUserAgent)

  override def getInternal(id: String): Call[ResponseBody] = resourceGroupsInternal.get(id, subscriptionId, defaultApiVersion, defaultAcceptLanguage, defaultUserAgent)

  def addParent(child: ResourceGroup): ResourceGroup = {
    child.subscriptionId = subscriptionId
    child.azure = azure
    child
  }

  trait ResourceGroupsInternal {
    @Headers(Array("Content-Type: application/json; charset=utf-8"))
    @GET("subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/resources")
    def listResources(
      @Path("resourceGroupName") resourceGroupName: String,
      @Path("subscriptionId") subscriptionId: String,
      @Query("$filter") filter: String,
      @Query("$top") top: Integer,
      @Query("api-version") apiVersion: String,
      @Header("accept-language") acceptLanguage: String,
      @Header("User-Agent") userAgent: String
    ): Call[ResponseBody]

    @Headers(Array("Content-Type: application/json; charset=utf-8"))
    @HEAD("subscriptions/{subscriptionId}/resourcegroups/{resourceGroupName}")
    def checkExistence(@Path("resourceGroupName") resourceGroupName: String, @Path("subscriptionId") subscriptionId: String, @Query("api-version") apiVersion: String, @Header("accept-language") acceptLanguage: String, @Header("User-Agent") userAgent: String): Call[Void]

    @Headers(Array("Content-Type: application/json; charset=utf-8"))
    @PUT("subscriptions/{subscriptionId}/resourcegroups/{resourceGroupName}")
    def createOrUpdate(
      @Path("resourceGroupName") resourceGroupName: String,
      @Path("subscriptionId") subscriptionId: String,
      @Body parameters: ResourceGroup,
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
      @Body parameters: ResourceGroup,
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
