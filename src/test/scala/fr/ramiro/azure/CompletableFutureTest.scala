package fr.ramiro.azure

import java.io.IOException
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ExecutionException

import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.http.GET
import okhttp3.mockwebserver.SocketPolicy.DISCONNECT_AFTER_REQUEST
import org.assertj.core.api.Assertions.assertThat
import org.junit.Assert.fail

class CompletableFutureTest {
  trait Service {
    @GET("/") def body: CompletableFuture[String]

    @GET("/") def response: CompletableFuture[Response[String]]
  }

  val _server = new MockWebServer

  @Rule def server: MockWebServer = _server

  private var service: Service = null

  @Before def setUp {
    val retrofit: Retrofit = new Retrofit.Builder().baseUrl(server.url("/")).addConverterFactory(new StringConverterFactory).addCallAdapterFactory(new AzureCallAdapterFactory).build
    service = retrofit.create(classOf[Service])
  }

  @Test
  def bodySuccess200 {
    server.enqueue(new MockResponse().setBody("Hi"))
    val future: CompletableFuture[String] = service.body
    assertThat(future.get).isEqualTo("Hi")
  }

  @Test
  def bodySuccess404 {
    server.enqueue(new MockResponse().setResponseCode(404))
    val future: CompletableFuture[String] = service.body
    try {
      future.get
      fail()
    } catch {
      case e: ExecutionException =>
        assert(e.getCause.isInstanceOf[HttpException])
        assert(e.getCause.getMessage == "HTTP 404 Client Error")
    }
  }

  @Test
  def bodyFailure {
    server.enqueue(new MockResponse().setSocketPolicy(DISCONNECT_AFTER_REQUEST))
    val future: CompletableFuture[String] = service.body
    try {
      future.get
      fail()
    } catch {
      case e: ExecutionException =>
        assertThat(e.getCause).isInstanceOf(classOf[IOException])
    }
  }

  @Test
  def responseSuccess200 {
    server.enqueue(new MockResponse().setBody("Hi"))
    val future: CompletableFuture[Response[String]] = service.response
    val response: Response[String] = future.get
    assertThat(response.isSuccessful).isTrue
    assertThat(response.body).isEqualTo("Hi")
  }

  @Test
  def responseSuccess404 {
    server.enqueue(new MockResponse().setResponseCode(404).setBody("Hi"))
    val future: CompletableFuture[Response[String]] = service.response
    val response: Response[String] = future.get
    assertThat(response.isSuccessful).isFalse
    assertThat(response.errorBody.string).isEqualTo("Hi")
  }

  @Test
  def responseFailure {
    server.enqueue(new MockResponse().setSocketPolicy(DISCONNECT_AFTER_REQUEST))
    val future: CompletableFuture[Response[String]] = service.response
    try {
      future.get
      fail()
    } catch {
      case e: ExecutionException =>
        assertThat(e.getCause).isInstanceOf(classOf[IOException])
    }
  }
}