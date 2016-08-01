package fr.ramiro.azure.services.subscriptions

import com.google.common.reflect.TypeToken
import com.microsoft.azure.{ CloudException, Page, PagedList }
import com.microsoft.rest.ServiceResponse
import fr.ramiro.azure.Azure
import fr.ramiro.azure.model.PageImpl1
import fr.ramiro.azure.rest.AzureServiceResponseBuilder
import fr.ramiro.azure.services.subscriptions.model.Subscription
import okhttp3.ResponseBody
import retrofit2.{ Call, Response }
import retrofit2.http.{ Url, _ }

class SubscriptionsService(azure: Azure) {
  val subscriptionsInternal = azure.retrofit.create(classOf[SubscriptionsInternal])
  val defaultApiVersion = "2015-11-01"
  val defaultAcceptLanguage = "en-US"
  val defaultUserAgent = s"Azure-SDK-For-Java/${getClass.getPackage.getImplementationVersion} (SubscriptionClient, $defaultApiVersion)"

  def get(subscriptionId: String) = {
    getDelegate(subscriptionsInternal.get(subscriptionId, defaultApiVersion, defaultAcceptLanguage, defaultUserAgent).execute())
  }

  def list = {
    val response = listDelegate(subscriptionsInternal.list(defaultApiVersion, defaultAcceptLanguage, defaultUserAgent).execute)
    new ServiceResponse[PagedList[Subscription]](
      new PagedList[Subscription](response.getBody) {
        def nextPage(nextPageLink: String): Page[Subscription] = {
          listNext(nextPageLink).getBody
        }
      },
      response.getResponse
    )
  }

  def listNext(nextPageLink: String) = {
    listNextDelegate(subscriptionsInternal.listNext(nextPageLink, defaultAcceptLanguage, defaultUserAgent).execute)
  }

  private def getDelegate(response: Response[ResponseBody]): ServiceResponse[Subscription] = {
    new AzureServiceResponseBuilder[Subscription](azure.mapperAdapter, new TypeToken[Subscription]() {}.getType, 200).build(response, addParent)
  }

  private def addParent(sub: Subscription): Subscription = {
    sub.azure = azure
    sub
  }

  private def listDelegate(response: Response[ResponseBody]): ServiceResponse[PageImpl1[Subscription]] = {
    new AzureServiceResponseBuilder[Subscription](azure.mapperAdapter, new TypeToken[PageImpl1[Subscription]]() {}.getType, 200).buildPaged(response, addParent)
  }

  private def listNextDelegate(response: Response[ResponseBody]): ServiceResponse[PageImpl1[Subscription]] = {
    new AzureServiceResponseBuilder[Subscription](azure.mapperAdapter, new TypeToken[PageImpl1[Subscription]]() {}.getType, 200).buildPaged(response, addParent)
  }

  trait SubscriptionsInternal {
    @Headers(Array("Content-Type: application/json; charset=utf-8"))
    @GET("subscriptions/{subscriptionId}/locations") def listLocations(@Path("subscriptionId") subscriptionId: String, @Query("api-version") apiVersion: String, @Header("accept-language") acceptLanguage: String, @Header("User-Agent") userAgent: String): Call[ResponseBody]

    @Headers(Array("Content-Type: application/json; charset=utf-8"))
    @GET("subscriptions/{subscriptionId}") def get(@Path("subscriptionId") subscriptionId: String, @Query("api-version") apiVersion: String, @Header("accept-language") acceptLanguage: String, @Header("User-Agent") userAgent: String): Call[ResponseBody]

    @Headers(Array("Content-Type: application/json; charset=utf-8"))
    @GET("subscriptions") def list(@Query("api-version") apiVersion: String, @Header("accept-language") acceptLanguage: String, @Header("User-Agent") userAgent: String): Call[ResponseBody]

    @Headers(Array("Content-Type: application/json; charset=utf-8"))
    @GET def listNext(@Url nextPageLink: String, @Header("accept-language") acceptLanguage: String, @Header("User-Agent") userAgent: String): Call[ResponseBody]
  }

}
