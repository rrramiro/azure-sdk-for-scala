package fr.ramiro.azure

import java.net.{ CookieManager, CookiePolicy }

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.microsoft.azure.serializer.CloudErrorDeserializer
import com.microsoft.azure.{ RequestIdHeaderInterceptor, ResourceGetExponentialBackoffRetryStrategy }
import com.microsoft.rest.{ BaseUrlHandler, UserAgentInterceptor }
import com.microsoft.rest.credentials.TokenCredentialsInterceptor
import com.microsoft.rest.retry.RetryHandler
import com.microsoft.rest.serializer.{ FlatteningDeserializer, FlatteningSerializer, JacksonMapperAdapter }
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.{ JavaNetCookieJar, OkHttpClient }
import retrofit2.Retrofit

object Azure {

  private class AzureJacksonMapperAdapter extends JacksonMapperAdapter {
    override lazy val getObjectMapper: ObjectMapper = new ObjectMapper {
      initializeObjectMapper(this)
      registerModule(FlatteningSerializer.getModule(getSimpleMapper))
        .registerModule(FlatteningDeserializer.getModule(getSimpleMapper))
        .registerModule(CloudErrorDeserializer.getModule(getSimpleMapper))
        .registerModule(DefaultScalaModule)
    }
  }

  def apply(credential: AzureTokenCredentials, logLevel: HttpLoggingInterceptor.Level = HttpLoggingInterceptor.Level.NONE) = {

    val httpClient = (new OkHttpClient.Builder).cookieJar(new JavaNetCookieJar(new CookieManager {
      setCookiePolicy(CookiePolicy.ACCEPT_ALL)
    })).addInterceptor(new RequestIdHeaderInterceptor)
      .addInterceptor(new TokenCredentialsInterceptor(credential))
      .addInterceptor(new UserAgentInterceptor)
      .addInterceptor(new RetryHandler(new ResourceGetExponentialBackoffRetryStrategy))
      .addInterceptor(new RetryHandler())
      .addInterceptor(new BaseUrlHandler)
      .addInterceptor(new HttpLoggingInterceptor().setLevel(logLevel)).build()

    val azureJacksonMapperAdapter = new AzureJacksonMapperAdapter

    val retrofit: Retrofit = (new Retrofit.Builder).baseUrl(credential.baseUrl)
      .client(httpClient)
      .addConverterFactory(azureJacksonMapperAdapter.getConverterFactory)
      .build()

    new Azure(retrofit, credential.subscriptionId, azureJacksonMapperAdapter)
  }
}

class Azure(val retrofit: Retrofit, val subscriptionId: String, val mapperAdapter: JacksonMapperAdapter)

