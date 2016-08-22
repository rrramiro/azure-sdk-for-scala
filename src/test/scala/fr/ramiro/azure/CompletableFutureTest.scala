package fr.ramiro.azure

import java.io.IOException
import fr.ramiro.azure.model.CloudException
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import retrofit2.Response
import retrofit2.http.GET
import okhttp3.mockwebserver.SocketPolicy.DISCONNECT_AFTER_REQUEST
import org.scalatest.{ BeforeAndAfterEach, FunSuite }
import scala.concurrent.duration.Duration.Inf
import scala.concurrent.{ Await, Future }

class CompletableFutureTest extends FunSuite with BeforeAndAfterEach {

  trait Service {
    @GET("/") def body: Future[String]

    @GET("/") def response: Future[Response[String]]

    @GET("/") def list: Response[Seq[String]]

    @GET("/") def page: Response[Stream[String]]
  }

  private var server: MockWebServer = _
  private var service: Service = _

  override def beforeEach() {
    server = new MockWebServer
    val retrofit = RetrofitAzure.retrofit(server.url("/"))
    service = retrofit.create(classOf[Service])
  }

  test("list") {
    server.enqueue(new MockResponse().setBody(
      """
        |{
        |  "value": [ "Hi" ]
        |}
      """.stripMargin
    ))
    assert(service.list.body() === Seq("Hi"))
  }

  test("page") {
    server.enqueue(new MockResponse().setBody(
      """
        |{
        |  "value": [ "Hi" ],
        |  "nextLink": "/"
        |}
      """.stripMargin
    ))
    server.enqueue(new MockResponse().setBody(
      """
        |{
        |  "value": [ "Hola" ]
        |}
      """.stripMargin
    ))
    assert(service.page.body().toList === List("Hi", "Hola"))
  }

  test("bodySuccess200") {
    server.enqueue(new MockResponse().setBody(""""Hi""""))
    assert(Await.result(service.body, Inf) === "Hi")
  }

  test("bodySuccess404") {
    server.enqueue(new MockResponse().setResponseCode(404))
    val caught = intercept[CloudException] {
      Await.result(service.body, Inf)
    }
    assert(caught.getMessage == "Invalid status code 404")
  }

  test("bodyFailure") {
    server.enqueue(new MockResponse().setSocketPolicy(DISCONNECT_AFTER_REQUEST))
    intercept[IOException] {
      Await.result(service.body, Inf)
    }
  }

  test("responseSuccess200") {
    server.enqueue(new MockResponse().setBody(""""Hi""""))
    val response = Await.result(service.response, Inf)
    assert(response.isSuccessful)
    assert(response.body === "Hi")
  }

  test("responseSuccess404") {
    server.enqueue(new MockResponse().setResponseCode(404).setBody("Hi"))
    val response = Await.result(service.response, Inf)
    assert(!response.isSuccessful)
    assert(response.errorBody.string === "Hi")
  }

  test("responseFailure") {
    server.enqueue(new MockResponse().setSocketPolicy(DISCONNECT_AFTER_REQUEST))
    intercept[IOException] {
      Await.result(service.response, Inf)
    }
  }
}
