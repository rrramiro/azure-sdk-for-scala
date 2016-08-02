package fr.ramiro.azure.services.subscriptions

import java.lang.reflect.Type

import com.google.common.reflect.TypeToken
import fr.ramiro.azure.Azure
import fr.ramiro.azure.model.PageImpl
import fr.ramiro.azure.services.PagedService
import fr.ramiro.azure.services.subscriptions.model.Subscription
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.{ Url, _ }

class SubscriptionsService(val azure: Azure) extends PagedService[Subscription] {
  val subscriptionsInternal = azure.retrofit.create(classOf[SubscriptionsInternal])
  val pagedType: Type = new TypeToken[PageImpl[Subscription]]() {}.getType
  val getType: Type = new TypeToken[Subscription]() {}.getType

  override def getInternal(id: String) = subscriptionsInternal.get(id, defaultApiVersion, defaultAcceptLanguage, defaultUserAgent)

  override def listInternal: Call[ResponseBody] = subscriptionsInternal.list(defaultApiVersion, defaultAcceptLanguage, defaultUserAgent)

  override def listNextInternal(nextPageLink: String): Call[ResponseBody] = subscriptionsInternal.listNext(nextPageLink, defaultAcceptLanguage, defaultUserAgent)

  def addParent(sub: Subscription): Subscription = { sub.azure = azure; sub }

  trait SubscriptionsInternal {
    @Headers(Array("Content-Type: application/json; charset=utf-8"))
    @GET("subscriptions/{subscriptionId}/locations")
    def listLocations(@Path("subscriptionId") subscriptionId: String, @Query("api-version") apiVersion: String, @Header("accept-language") acceptLanguage: String, @Header("User-Agent") userAgent: String): Call[ResponseBody]

    @Headers(Array("Content-Type: application/json; charset=utf-8"))
    @GET("subscriptions/{subscriptionId}")
    def get(@Path("subscriptionId") subscriptionId: String, @Query("api-version") apiVersion: String, @Header("accept-language") acceptLanguage: String, @Header("User-Agent") userAgent: String): Call[ResponseBody]

    @Headers(Array("Content-Type: application/json; charset=utf-8"))
    @GET("subscriptions")
    def list(@Query("api-version") apiVersion: String, @Header("accept-language") acceptLanguage: String, @Header("User-Agent") userAgent: String): Call[ResponseBody]

    @Headers(Array("Content-Type: application/json; charset=utf-8"))
    @GET
    def listNext(@Url nextPageLink: String, @Header("accept-language") acceptLanguage: String, @Header("User-Agent") userAgent: String): Call[ResponseBody]
  }
}
