package fr.ramiro.azure

import com.google.common.reflect.TypeToken
import java.lang.annotation.Annotation
import java.lang.reflect.Type
import java.util
import java.util.concurrent.CompletableFuture

import okhttp3.{ MediaType, RequestBody, ResponseBody }
import okhttp3.mockwebserver.MockWebServer
import org.junit.{ Before, Ignore, Rule, Test }
import retrofit2.{ CallAdapter, Converter, Response, Retrofit }
import org.assertj.core.api.Assertions.assertThat
import org.junit.Assert.fail

class AzureCallAdapterFactoryTest {
  private val NO_ANNOTATIONS: Array[Annotation] = new Array[Annotation](0)
  val _server: MockWebServer = new MockWebServer
  @Rule def server: MockWebServer = _server

  private val factory: CallAdapter.Factory = new AzureCallAdapterFactory
  private var retrofit: Retrofit = null

  @Before def setUp {
    retrofit = new Retrofit.Builder().baseUrl(server.url("/")).addConverterFactory(new StringConverterFactory).addCallAdapterFactory(factory).build
  }

  @Test def responseType {
    val bodyClass: Type = new TypeToken[CompletableFuture[String]]() {}.getType
    assert(factory.get(bodyClass, NO_ANNOTATIONS, retrofit).responseType eq classOf[String])
    val bodyWildcard: Type = new TypeToken[CompletableFuture[_ <: String]]() {}.getType
    assert(factory.get(bodyWildcard, NO_ANNOTATIONS, retrofit).responseType eq classOf[String])
    val responseClass: Type = new TypeToken[CompletableFuture[Response[String]]]() {}.getType
    assert(factory.get(responseClass, NO_ANNOTATIONS, retrofit).responseType eq classOf[String])
    val responseWildcard: Type = new TypeToken[CompletableFuture[Response[_ <: String]]]() {}.getType
    assert(factory.get(responseWildcard, NO_ANNOTATIONS, retrofit).responseType eq classOf[String])
    val resultClass: Type = new TypeToken[CompletableFuture[Response[String]]]() {}.getType
    assert(factory.get(resultClass, NO_ANNOTATIONS, retrofit).responseType eq classOf[String])
    val resultWildcard: Type = new TypeToken[CompletableFuture[Response[_ <: String]]]() {}.getType
    assert(factory.get(resultWildcard, NO_ANNOTATIONS, retrofit).responseType eq classOf[String])
  }

  @Ignore
  @Test def responseTypeGeneric: Unit = {
    val bodyGeneric: Type = new TypeToken[CompletableFuture[util.List[String]]]() {}.getType
    assert(factory.get(bodyGeneric, NO_ANNOTATIONS, retrofit).responseType eq new TypeToken[util.List[String]]() {}.getType)
  }

  @Test def nonListenableFutureReturnsNull {
    val adapter: CallAdapter[_] = factory.get(classOf[String], NO_ANNOTATIONS, retrofit)
    assert(adapter == null)
  }

  @Ignore
  @Test def rawTypeThrows {
    val observableType: Type = new TypeToken[CompletableFuture[_]]() {}.getType
    try {
      factory.get(observableType, NO_ANNOTATIONS, retrofit)
      fail()
    } catch {
      case e: IllegalStateException =>
        assertThat(e).hasMessage("CompletableFuture return type must be parameterized as CompletableFuture<Foo> or CompletableFuture<? extends Foo>")
    }
  }

  @Ignore
  @Test def rawResponseTypeThrows {
    val observableType: Type = new TypeToken[CompletableFuture[Response[_]]]() {}.getType
    try {
      factory.get(observableType, NO_ANNOTATIONS, retrofit)
      fail()
    } catch {
      case e: IllegalStateException =>
        assertThat(e).hasMessage("Response must be parameterized as Response<Foo> or Response<? extends Foo>")
    }
  }
}

final class StringConverterFactory extends Converter.Factory {
  override def responseBodyConverter(`type`: Type, annotations: Array[Annotation], retrofit: Retrofit): Converter[ResponseBody, _] = {
    new Converter[ResponseBody, String]() {
      def convert(value: ResponseBody): String = value.string
    }
  }

  override def requestBodyConverter(`type`: Type, parameterAnnotations: Array[Annotation], methodAnnotations: Array[Annotation], retrofit: Retrofit): Converter[_, RequestBody] = {
    new Converter[String, RequestBody]() {
      def convert(value: String): RequestBody = RequestBody.create(MediaType.parse("text/plain"), value)
    }
  }
}