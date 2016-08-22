package fr.ramiro.azure

import java.net.{ CookieManager, CookiePolicy }
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.microsoft.azure.serializer.CloudErrorDeserializer
import com.microsoft.azure.{ RequestIdHeaderInterceptor, ResourceGetExponentialBackoffRetryStrategy }
import com.microsoft.rest.{ BaseUrlHandler, UserAgentInterceptor }
import com.microsoft.rest.credentials.TokenCredentialsInterceptor
import com.microsoft.rest.retry.RetryHandler
import com.microsoft.rest.serializer.{ FlatteningDeserializer, FlatteningSerializer, JacksonConverterFactory, JacksonMapperAdapter }
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.{ HttpUrl, JavaNetCookieJar, OkHttpClient }
import retrofit2.Retrofit

object RetrofitAzure {

  private lazy val objectMapper: ObjectMapper = {
    new JacksonMapperAdapter {
      override lazy val getObjectMapper: ObjectMapper = new ObjectMapper {
        initializeObjectMapper(this)
        registerModule(FlatteningSerializer.getModule(getSimpleMapper))
          .registerModule(FlatteningDeserializer.getModule(getSimpleMapper))
          .registerModule(CloudErrorDeserializer.getModule(getSimpleMapper))
          .registerModule(DefaultScalaModule)
      }
      def exportObjectMapper = getObjectMapper
    }.exportObjectMapper
  }

  def okHttpClient(credential: AzureTokenCredentials, logLevel: HttpLoggingInterceptor.Level) = new OkHttpClient.Builder().cookieJar(new JavaNetCookieJar(new CookieManager {
    setCookiePolicy(CookiePolicy.ACCEPT_ALL)
  })).addInterceptor(new RequestIdHeaderInterceptor)
    .addInterceptor(new TokenCredentialsInterceptor(credential))
    .addInterceptor(new UserAgentInterceptor)
    .addInterceptor(new RetryHandler(new ResourceGetExponentialBackoffRetryStrategy))
    .addInterceptor(new RetryHandler())
    .addInterceptor(new BaseUrlHandler)
    .addInterceptor(new HttpLoggingInterceptor().setLevel(logLevel))
    .build()

  def retrofit(baseUrl: HttpUrl, httpClient: Option[OkHttpClient] = None): Retrofit = {
    val builder = new Retrofit.Builder().baseUrl(baseUrl)
    httpClient.foreach { client => builder.client(client) }
    builder.addConverterFactory(JacksonConverterFactory.create(objectMapper))
      .addCallAdapterFactory(new AzureCallAdapterFactory)
      .build()
  }

  def apply(credential: AzureTokenCredentials, logLevel: HttpLoggingInterceptor.Level = HttpLoggingInterceptor.Level.NONE) = {
    retrofit(HttpUrl.parse(credential.baseUrl), Some(okHttpClient(credential, logLevel)))
  }
}

