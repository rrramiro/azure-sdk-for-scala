package fr.ramiro.azure.services

import fr.ramiro.azure.model.Subscription
import retrofit2.{ Response, Retrofit }
import retrofit2.http._

class SubscriptionsService(val retrofit: Retrofit) extends BaseService {
  val internal = retrofit.create(classOf[SubscriptionsInternal])

  def addParent(child: Subscription): Subscription = {
    child.retrofit = retrofit
    child
  }

  def list: Seq[Subscription] = internal.list(defaultApiVersion, defaultAcceptLanguage, defaultUserAgent).body().map { addParent }

  def get(id: String): Subscription = addParent(internal.get(id, defaultApiVersion, defaultAcceptLanguage, defaultUserAgent).body())

  trait SubscriptionsInternal {

    @Headers(Array("Content-Type: application/json; charset=utf-8"))
    @GET("subscriptions/{subscriptionId}")
    def get(
      @Path("subscriptionId") subscriptionId: String,
      @Query("api-version") apiVersion: String,
      @Header("accept-language") acceptLanguage: String,
      @Header("User-Agent") userAgent: String
    ): Response[Subscription]

    @Headers(Array("Content-Type: application/json; charset=utf-8"))
    @GET("subscriptions")
    def list(@Query("api-version") apiVersion: String, @Header("accept-language") acceptLanguage: String, @Header("User-Agent") userAgent: String): Response[Seq[Subscription]]
  }
}
