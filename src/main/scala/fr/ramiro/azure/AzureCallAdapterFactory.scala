package fr.ramiro.azure

import java.lang.annotation.Annotation
import java.lang.reflect.{ ParameterizedType, Type, WildcardType }
import com.google.common.reflect.{ TypeParameter, TypeToken }
import com.microsoft.azure.CloudError
import fr.ramiro.azure.model.{ CloudException, CollectionResponse }
import okhttp3.ResponseBody
import retrofit2._
import scala.concurrent.{ Future, Promise }

//TODO Handle cancellation
class AzureCallAdapterFactory extends CallAdapter.Factory {
  def get(retType: Type, annotations: Array[Annotation], retrofit: Retrofit): CallAdapter[_] = {
    val rawType = CallAdapter.Factory.getRawType(retType)
    if (rawType eq classOf[Future[_]]) {
      retType match {
        case returnType: ParameterizedType if isNotOpenWildCard(returnType) =>
          val inType: Type = CallAdapter.Factory.getParameterUpperBound(0, returnType.asInstanceOf[ParameterizedType])
          if (CallAdapter.Factory.getRawType(inType) eq classOf[Response[_]]) {
            inType match {
              case innerType: ParameterizedType if isNotOpenWildCard(innerType) =>
                new FutureResponseCallAdapter(CallAdapter.Factory.getParameterUpperBound(0, innerType.asInstanceOf[ParameterizedType]))
              case _ =>
                throw new IllegalStateException("Response must be parameterized as Response[Foo] or Response[_ <: Foo]")
            }
          } else {
            new BodyCallAdapter(inType, retrofit)
          }
        case _ =>
          throw new IllegalStateException("Future return type must be parameterized as Future[Foo] or Future[_ <: Foo]")
      }
    } else if (rawType eq classOf[Response[_]]) {
      retType match {
        case returnType: ParameterizedType if isNotOpenWildCard(returnType) =>
          val inType: Type = CallAdapter.Factory.getParameterUpperBound(0, returnType.asInstanceOf[ParameterizedType])
          if (CallAdapter.Factory.getRawType(inType) eq classOf[Seq[_]]) {
            inType match {
              case innerType: ParameterizedType if isNotOpenWildCard(innerType) =>
                new ListCallAdapter(constructListResponseType(TypeToken.of(CallAdapter.Factory.getRawType {
                  CallAdapter.Factory.getParameterUpperBound(0, innerType.asInstanceOf[ParameterizedType])
                })), retrofit)
              case _ =>
                throw new IllegalStateException("Response must be parameterized as Response[Seq[Foo]] or Response[Seq[_ <: Foo]]")
            }
          } else if (CallAdapter.Factory.getRawType(inType) eq classOf[Stream[_]]) {
            inType match {
              case innerType: ParameterizedType if isNotOpenWildCard(innerType) =>
                new StreamCallAdapter(constructListResponseType(TypeToken.of(CallAdapter.Factory.getRawType {
                  CallAdapter.Factory.getParameterUpperBound(0, innerType.asInstanceOf[ParameterizedType])
                })), retrofit)
              case _ =>
                throw new IllegalStateException("Response must be parameterized as Response[Stream[Foo]] or Response[Stream[_ <: Foo]]")
            }
          } else {
            null
          }
        case _ =>
          throw new IllegalStateException("Response must be parameterized as Response[Foo] or Response[_ <: Foo]")
      }
    } else {
      null
    }
  }

  private def constructListResponseType[K](keyToken: TypeToken[K]) = {
    new TypeToken[CollectionResponse[K]]() {}.where(new TypeParameter[K]() {}, keyToken).getType
  }

  private def isNotOpenWildCard(parameterizedType: ParameterizedType): Boolean = {
    parameterizedType.getActualTypeArguments.exists {
      case wildcardType: WildcardType => !wildcardType.getUpperBounds.contains(classOf[Object])
      case _ => true
    }
  }

  //TODO Add header accept-language, User-Agent
  trait NextService {
    import retrofit2.http._
    @Headers(Array("Content-Type: application/json; charset=utf-8"))
    @GET
    def listNext(@Url nextPageLink: String): Call[ResponseBody]
  }

  private def buildCloudError(retrofit: Retrofit, responseBody: ResponseBody): CloudError = {
    try {
      retrofit.responseBodyConverter[CloudError](classOf[CloudError], Array()).convert(responseBody)
    } catch {
      case e: Throwable => new CloudError {
        setMessage(e.getMessage + " - " + responseBody.string)
      }
    } finally {
      responseBody.close()
    }
  }

  class StreamCallAdapter(val responseType: Type, retrofit: Retrofit) extends CallAdapter[Response[Stream[Any]]] {
    val nextService = retrofit.create(classOf[NextService])

    def adapt[R](call: Call[R]): Response[Stream[Any]] = {
      val response = call.execute()
      if (response.isSuccessful) {
        Response.success(pageStream(Some(response.body().asInstanceOf[CollectionResponse[_]])), response.raw())
      } else {
        throw new CloudException("Invalid status code " + response.code, buildCloudError(retrofit, response.errorBody()), response)
      }
    }

    def pageStream[T](page: Option[CollectionResponse[T]]): Stream[T] = page match {
      case Some(aPage) => aPage.value.toStream #::: pageStream(
        if (aPage.hasNextPage) {
          val responseNext = nextService.listNext(aPage.nextLink).execute()
          if (responseNext.isSuccessful) {
            Some(retrofit.responseBodyConverter[CollectionResponse[T]](responseType, Array()).convert({
              responseNext.body()
            }))
          } else {
            throw new CloudException("Error URL next " + responseNext.code, buildCloudError(retrofit, responseNext.errorBody()), responseNext)
          }
        } else {
          None
        }
      )
      case _ => Stream.empty
    }
  }

  class ListCallAdapter(val responseType: Type, retrofit: Retrofit) extends CallAdapter[Response[Seq[Any]]] {
    def adapt[R](call: Call[R]): Response[Seq[Any]] = {
      val response = call.execute()
      if (response.isSuccessful) {
        Response.success(response.body().asInstanceOf[CollectionResponse[_]].value, response.raw())
      } else {
        throw new CloudException("Invalid status code " + response.code, buildCloudError(retrofit, response.errorBody()), response)
      }
    }
  }

  class BodyCallAdapter(val responseType: Type, retrofit: Retrofit) extends CallAdapter[Future[_]] {
    def adapt[R](call: Call[R]): Future[R] = {
      val promise = Promise[R]
      call.enqueue(new Callback[R]() {
        def onResponse(call: Call[R], response: Response[R]) {
          if (response.isSuccessful) {
            promise.success(response.body)
          } else {
            promise.failure(new CloudException("Invalid status code " + response.code, buildCloudError(retrofit, response.errorBody()), response))
          }
        }
        def onFailure(call: Call[R], t: Throwable) {
          promise.failure(t)
        }
      })
      promise.future
    }
  }

  class FutureResponseCallAdapter(val responseType: Type) extends CallAdapter[Future[Response[_]]] {
    def adapt[R](call: Call[R]): Future[Response[R]] = {
      val promise = Promise[Response[R]]
      call.enqueue(new Callback[R]() {
        def onResponse(call: Call[R], response: Response[R]) {
          promise.success(response)
        }
        def onFailure(call: Call[R], t: Throwable) {
          promise.failure(t)
        }
      })
      promise.future
    }
  }
}