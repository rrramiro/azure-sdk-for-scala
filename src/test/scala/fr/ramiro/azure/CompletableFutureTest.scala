package fr.ramiro.azure

import java.io.IOException
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ExecutionException

import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.http.GET
import okhttp3.mockwebserver.SocketPolicy.DISCONNECT_AFTER_REQUEST
import org.scalatest.{ BeforeAndAfterEach, FunSuite }

class CompletableFutureTest extends FunSuite with BeforeAndAfterEach {

  trait Service {
    @GET("/") def body: CompletableFuture[String]

    @GET("/") def response: CompletableFuture[Response[String]]
  }

  private var server: MockWebServer = _
  private var service: Service = _

  override def beforeEach() {
    server = new MockWebServer
    val retrofit: Retrofit = new Retrofit.Builder()
      .baseUrl(server.url("/"))
      .addConverterFactory(new StringConverterFactory)
      .addCallAdapterFactory(new AzureCallAdapterFactory)
      .build
    service = retrofit.create(classOf[Service])
  }

  test("bodySuccess200") {
    server.enqueue(new MockResponse().setBody("Hi"))
    val future: CompletableFuture[String] = service.body
    assert(future.get === "Hi")
  }

  test("bodySuccess404") {
    server.enqueue(new MockResponse().setResponseCode(404))
    val future: CompletableFuture[String] = service.body
    val caught = intercept[ExecutionException] {
      future.get
    }
    assert(caught.getCause.isInstanceOf[HttpException])
    assert(caught.getCause.getMessage == "HTTP 404 Client Error")
  }

  test("bodyFailure") {
    server.enqueue(new MockResponse().setSocketPolicy(DISCONNECT_AFTER_REQUEST))
    val future: CompletableFuture[String] = service.body
    val caught = intercept[ExecutionException] {
      future.get
    }
    assert(caught.getCause.isInstanceOf[IOException])
  }

  test("responseSuccess200") {
    server.enqueue(new MockResponse().setBody("Hi"))
    val future: CompletableFuture[Response[String]] = service.response
    val response: Response[String] = future.get
    assert(response.isSuccessful)
    assert(response.body === "Hi")
  }

  test("responseSuccess404") {
    server.enqueue(new MockResponse().setResponseCode(404).setBody("Hi"))
    val future: CompletableFuture[Response[String]] = service.response
    val response: Response[String] = future.get
    assert(!response.isSuccessful)
    assert(response.errorBody.string === "Hi")
  }

  test("responseFailure") {
    server.enqueue(new MockResponse().setSocketPolicy(DISCONNECT_AFTER_REQUEST))
    val future: CompletableFuture[Response[String]] = service.response
    val caught = intercept[ExecutionException] {
      future.get
    }
    assert(caught.getCause.isInstanceOf[IOException])
  }
}
