package fr.ramiro.azure

import com.google.common.reflect.TypeToken
import java.lang.annotation.Annotation
import java.lang.reflect.Type
import fr.ramiro.azure.model.CollectionResponse
import okhttp3.mockwebserver.MockWebServer
import retrofit2.{ CallAdapter, Response, Retrofit }
import org.scalatest.{ BeforeAndAfterEach, FunSuite }
import scala.concurrent.Future

class AzureCallAdapterFactoryTest extends FunSuite with BeforeAndAfterEach {
  private val NO_ANNOTATIONS: Array[Annotation] = new Array[Annotation](0)
  private val factory: CallAdapter.Factory = new AzureCallAdapterFactory
  private var server: MockWebServer = _
  private var retrofit: Retrofit = _

  override def beforeEach {
    server = new MockWebServer
    retrofit = RetrofitAzure.retrofit(server.url("/"))
  }

  test("responseType") {
    val bodyClass: Type = new TypeToken[Future[String]]() {}.getType
    assert(factory.get(bodyClass, NO_ANNOTATIONS, retrofit).responseType eq classOf[String])
    val responseClass: Type = new TypeToken[Future[Response[String]]]() {}.getType
    assert(factory.get(responseClass, NO_ANNOTATIONS, retrofit).responseType eq classOf[String])
    val resultClass: Type = new TypeToken[Future[Response[String]]]() {}.getType
    assert(factory.get(resultClass, NO_ANNOTATIONS, retrofit).responseType eq classOf[String])
    val bodyGeneric: Type = new TypeToken[Future[Seq[String]]]() {}.getType
    assert(factory.get(bodyGeneric, NO_ANNOTATIONS, retrofit).responseType === new TypeToken[Seq[String]]() {}.getType)
  }

  test("responseWildcard with base type") {
    val bodyWildcard: Type = new TypeToken[Future[_ <: String]]() {}.getType
    assert(factory.get(bodyWildcard, NO_ANNOTATIONS, retrofit).responseType eq classOf[String])
    val resultWildcard: Type = new TypeToken[Future[Response[_ <: String]]]() {}.getType
    assert(factory.get(resultWildcard, NO_ANNOTATIONS, retrofit).responseType eq classOf[String])
  }

  test("list") {
    val bodyGeneric: Type = new TypeToken[Response[Seq[String]]]() {}.getType
    val adapter = factory.get(bodyGeneric, NO_ANNOTATIONS, retrofit)
    assert(adapter.responseType === new TypeToken[CollectionResponse[String]]() {}.getType)
  }

  test("nonListenableFutureReturnsNull") {
    val adapter = factory.get(classOf[String], NO_ANNOTATIONS, retrofit)
    assert(adapter === null)
  }

  test("rawTypeThrows") {
    val observableType: Type = new TypeToken[Future[_]]() {}.getType
    val caught = intercept[IllegalStateException] {
      factory.get(observableType, NO_ANNOTATIONS, retrofit)
    }
    assert(caught.getMessage === "Future return type must be parameterized as Future[Foo] or Future[_ <: Foo]")
  }

  test("rawResponseTypeThrows") {
    val observableType: Type = new TypeToken[Future[Response[_]]]() {}.getType
    val caught = intercept[IllegalStateException] {
      factory.get(observableType, NO_ANNOTATIONS, retrofit)
    }
    assert(caught.getMessage === "Response must be parameterized as Response[Foo] or Response[_ <: Foo]")
  }
}
