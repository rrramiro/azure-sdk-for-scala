package fr.ramiro.azure.services

import fr.ramiro.azure.model._
import okhttp3.ResponseBody
import retrofit2.{ Response, Retrofit }
import retrofit2.http._
import scala.concurrent.duration.Duration
import scala.concurrent.{ Await, Future }

class ResourceGroupsService(retrofit: Retrofit, subscriptionId: String) extends BaseService {
  val internal = retrofit.create(classOf[ResourceGroupsInternal])

  def list: Seq[ResourceGroup] = internal.list(subscriptionId, null, null, defaultApiVersion, defaultAcceptLanguage, defaultUserAgent).body().map { addParent }

  def get(id: String): ResourceGroup = addParent(Await.result(internal.get(id, subscriptionId, defaultApiVersion, defaultAcceptLanguage, defaultUserAgent), Duration.Inf))

  def addParent(child: ResourceGroup): ResourceGroup = {
    child.subscriptionId = subscriptionId
    child.retrofit = retrofit
    child
  }

  trait ResourceGroupsInternal {

    @Headers(Array("Content-Type: application/json; charset=utf-8"))
    @HEAD("subscriptions/{subscriptionId}/resourcegroups/{resourceGroupName}")
    def checkExistence(
      @Path("resourceGroupName") resourceGroupName: String,
      @Path("subscriptionId") subscriptionId: String,
      @Query("api-version") apiVersion: String,
      @Header("accept-language") acceptLanguage: String,
      @Header("User-Agent") userAgent: String
    ): Response[Void]

    @Headers(Array("Content-Type: application/json; charset=utf-8"))
    @PUT("subscriptions/{subscriptionId}/resourcegroups/{resourceGroupName}")
    def createOrUpdate(
      @Path("resourceGroupName") resourceGroupName: String,
      @Path("subscriptionId") subscriptionId: String,
      @Body parameters: ResourceGroup,
      @Query("api-version") apiVersion: String,
      @Header("accept-language") acceptLanguage: String,
      @Header("User-Agent") userAgent: String
    ): Response[Void]

    @Headers(Array("Content-Type: application/json; charset=utf-8"))
    @HTTP(path = "subscriptions/{subscriptionId}/resourcegroups/{resourceGroupName}", method = "DELETE", hasBody = true)
    def delete(
      @Path("resourceGroupName") resourceGroupName: String,
      @Path("subscriptionId") subscriptionId: String,
      @Query("api-version") apiVersion: String,
      @Header("accept-language") acceptLanguage: String,
      @Header("User-Agent") userAgent: String
    ): Response[Void]

    @Headers(Array("Content-Type: application/json; charset=utf-8"))
    @GET("subscriptions/{subscriptionId}/resourcegroups/{resourceGroupName}")
    def get(
      @Path("resourceGroupName") resourceGroupName: String,
      @Path("subscriptionId") subscriptionId: String,
      @Query("api-version") apiVersion: String,
      @Header("accept-language") acceptLanguage: String,
      @Header("User-Agent") userAgent: String
    ): Future[ResourceGroup]

    @Headers(Array("Content-Type: application/json; charset=utf-8"))
    @PATCH("subscriptions/{subscriptionId}/resourcegroups/{resourceGroupName}")
    def patch(
      @Path("resourceGroupName") resourceGroupName: String,
      @Path("subscriptionId") subscriptionId: String,
      @Body parameters: ResourceGroup,
      @Query("api-version") apiVersion: String,
      @Header("accept-language") acceptLanguage: String,
      @Header("User-Agent") userAgent: String
    ): Response[ResponseBody]

    @Headers(Array("Content-Type: application/json; charset=utf-8"))
    @GET("subscriptions/{subscriptionId}/resourcegroups")
    def list(
      @Path("subscriptionId") subscriptionId: String,
      @Query("$filter") filter: String,
      @Query("$top") top: Integer,
      @Query("api-version") apiVersion: String,
      @Header("accept-language") acceptLanguage: String,
      @Header("User-Agent") userAgent: String
    ): Response[Stream[ResourceGroup]]
  }
}
