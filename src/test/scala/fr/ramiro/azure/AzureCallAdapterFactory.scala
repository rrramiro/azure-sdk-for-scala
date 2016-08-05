package fr.ramiro.azure

import java.lang.annotation.Annotation
import java.lang.reflect.{ ParameterizedType, Type, WildcardType }
import java.util.concurrent.CompletableFuture

import retrofit2._

class AzureCallAdapterFactory extends CallAdapter.Factory {

  def isNotOpenWildCard(parameterizedType: ParameterizedType): Boolean = {
    parameterizedType.getActualTypeArguments.exists {
      case wildcardType: WildcardType => !wildcardType.getUpperBounds.contains(classOf[Object])
      case _ => true
    }
  }

  def get(retType: Type, annotations: Array[Annotation], retrofit: Retrofit): CallAdapter[_] = {
    if (CallAdapter.Factory.getRawType(retType) ne classOf[CompletableFuture[_]]) {
      return null
    }
    retType match {
      case returnType: ParameterizedType if isNotOpenWildCard(returnType) =>
        val inType: Type = CallAdapter.Factory.getParameterUpperBound(0, returnType.asInstanceOf[ParameterizedType])
        if (CallAdapter.Factory.getRawType(inType) ne classOf[Response[_]]) {
          return new BodyCallAdapter(inType)
        }
        inType match {
          case innerType: ParameterizedType if isNotOpenWildCard(innerType) =>
            val responseType: Type = CallAdapter.Factory.getParameterUpperBound(0, innerType.asInstanceOf[ParameterizedType])
            new ResponseCallAdapter(responseType)
          case _ =>
            throw new IllegalStateException("Response must be parameterized as Response<Foo> or Response<? extends Foo>")
        }
      case _ =>
        throw new IllegalStateException("CompletableFuture return type must be parameterized as CompletableFuture<Foo> or CompletableFuture<? extends Foo>")
    }
  }
}

class BodyCallAdapter(val responseType: Type) extends CallAdapter[CompletableFuture[_]] {

  def adapt[R](call: Call[R]): CompletableFuture[R] = {
    val future: CompletableFuture[R] = new CompletableFuture[R]() {
      override def cancel(mayInterruptIfRunning: Boolean): Boolean = {
        if (mayInterruptIfRunning) {
          call.cancel()
        }
        super.cancel(mayInterruptIfRunning)
      }
    }
    call.enqueue(new Callback[R]() {
      def onResponse(call: Call[R], response: Response[R]) {
        if (response.isSuccessful) {
          future.complete(response.body)
        } else {
          future.completeExceptionally(new HttpException(response))
        }
      }

      def onFailure(call: Call[R], t: Throwable) {
        future.completeExceptionally(t)
      }
    })
    future
  }
}

class ResponseCallAdapter(val responseType: Type) extends CallAdapter[CompletableFuture[_]] {

  def adapt[R](call: Call[R]): CompletableFuture[Response[R]] = {
    val future: CompletableFuture[Response[R]] = new CompletableFuture[Response[R]]() {
      override def cancel(mayInterruptIfRunning: Boolean): Boolean = {
        if (mayInterruptIfRunning) {
          call.cancel()
        }
        super.cancel(mayInterruptIfRunning)
      }
    }
    call.enqueue(new Callback[R]() {
      def onResponse(call: Call[R], response: Response[R]) {
        future.complete(response)
      }

      def onFailure(call: Call[R], t: Throwable) {
        future.completeExceptionally(t)
      }
    })
    future
  }
}

class HttpException(val response: Response[_]) extends Exception("HTTP " + response.code + " " + response.message)