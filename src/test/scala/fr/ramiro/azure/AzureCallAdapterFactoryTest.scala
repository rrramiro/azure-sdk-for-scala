package fr.ramiro.azure

import com.google.common.reflect.TypeToken
import java.lang.annotation.Annotation
import java.lang.reflect.Type
import java.util
import java.util.concurrent.CompletableFuture
import okhttp3.mockwebserver.MockWebServer
import retrofit2.{ CallAdapter, Response, Retrofit }
import org.scalatest.{ BeforeAndAfterEach, FunSuite }

class AzureCallAdapterFactoryTest extends FunSuite with BeforeAndAfterEach {
  private val NO_ANNOTATIONS: Array[Annotation] = new Array[Annotation](0)
  private val factory: CallAdapter.Factory = new AzureCallAdapterFactory
  private var server: MockWebServer = _
  private var retrofit: Retrofit = _

  override def beforeEach {
    server = new MockWebServer
    retrofit = new Retrofit.Builder()
      .baseUrl(server.url("/"))
      .addConverterFactory(new StringConverterFactory)
      .addCallAdapterFactory(factory).build
  }

  test("responseType") {
    val bodyClass: Type = new TypeToken[CompletableFuture[String]]() {}.getType
    assert(factory.get(bodyClass, NO_ANNOTATIONS, retrofit).responseType eq classOf[String])
    val responseClass: Type = new TypeToken[CompletableFuture[Response[String]]]() {}.getType
    assert(factory.get(responseClass, NO_ANNOTATIONS, retrofit).responseType eq classOf[String])
    val resultClass: Type = new TypeToken[CompletableFuture[Response[String]]]() {}.getType
    assert(factory.get(resultClass, NO_ANNOTATIONS, retrofit).responseType eq classOf[String])
    val bodyGeneric: Type = new TypeToken[CompletableFuture[util.List[String]]]() {}.getType
    assert(factory.get(bodyGeneric, NO_ANNOTATIONS, retrofit).responseType === new TypeToken[util.List[String]]() {}.getType)
  }

  test("responseWildcard with base type") {
    val bodyWildcard: Type = new TypeToken[CompletableFuture[_ <: String]]() {}.getType
    assert(factory.get(bodyWildcard, NO_ANNOTATIONS, retrofit).responseType eq classOf[String])
    val resultWildcard: Type = new TypeToken[CompletableFuture[Response[_ <: String]]]() {}.getType
    assert(factory.get(resultWildcard, NO_ANNOTATIONS, retrofit).responseType eq classOf[String])
  }

  test("nonListenableFutureReturnsNull") {
    val adapter: CallAdapter[_] = factory.get(classOf[String], NO_ANNOTATIONS, retrofit)
    assert(adapter === null)
  }

  test("rawTypeThrows") {
    val observableType: Type = new TypeToken[CompletableFuture[_]]() {}.getType
    val caught = intercept[IllegalStateException] {
      factory.get(observableType, NO_ANNOTATIONS, retrofit)
    }
    assert(caught.getMessage === "CompletableFuture return type must be parameterized as CompletableFuture<Foo> or CompletableFuture<? extends Foo>")
  }

  test("rawResponseTypeThrows") {
    val observableType: Type = new TypeToken[CompletableFuture[Response[_]]]() {}.getType
    val caught = intercept[IllegalStateException] {
      factory.get(observableType, NO_ANNOTATIONS, retrofit)
    }
    assert(caught.getMessage === "Response must be parameterized as Response<Foo> or Response<? extends Foo>")
  }
}
