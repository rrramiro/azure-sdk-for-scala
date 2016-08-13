package fr.ramiro.azure.services

import fr.ramiro.azure.Azure
import fr.ramiro.azure.model.CdnProfile
import retrofit2.Response
import retrofit2.http._

import scala.concurrent.duration.Duration
import scala.concurrent.{ Await, Future }

class CdnProfilesService(azure: Azure, subscriptionId: String, resourceGroupName: String) extends BaseService {
  val internal = azure.retrofit.create(classOf[CdnProfileServiceInternal])
  override val defaultApiVersion = "2016-04-02"

  def addParent(child: CdnProfile): CdnProfile = {
    child.azure = azure
    child.subscriptionId = subscriptionId
    child.resourceGroupName = child.resourceGroupName
    child
  }

  def list: Seq[CdnProfile] = internal.list(subscriptionId, resourceGroupName, defaultApiVersion).body().map { addParent }

  def get(id: String): CdnProfile = addParent(Await.result(internal.get(subscriptionId, resourceGroupName, id, defaultApiVersion), Duration.Inf).body())

  trait CdnProfileServiceInternal {

    @Headers(Array("Content-Type: application/json; charset=utf-8"))
    @GET("/subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/Microsoft.Cdn/Profiles")
    def list(
      @Path("subscriptionId") subscriptionId: String,
      @Path("resourceGroupName") resourceGroupName: String,
      @Query("api-version") apiVersion: String
    ): Response[Seq[CdnProfile]]

    @Headers(Array("Content-Type: application/json; charset=utf-8"))
    @GET("/subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/Microsoft.Cdn/Profiles/{profileName}")
    def get(
      @Path("subscriptionId") subscriptionId: String,
      @Path("resourceGroupName") resourceGroupName: String,
      @Path("profileName") profileName: String,
      @Query("api-version") apiVersion: String
    ): Future[Response[CdnProfile]]
  }
}
