package fr.ramiro.azure

import java.lang.annotation.Annotation
import java.lang.reflect.{ ParameterizedType, Type }
import java.util.concurrent.CompletableFuture

import retrofit2._

class AzureCallAdapterFactory extends CallAdapter.Factory {
  def get(returnType: Type, annotations: Array[Annotation], retrofit: Retrofit): CallAdapter[_] = {

    if (CallAdapter.Factory.getRawType(returnType) ne classOf[CompletableFuture[_]]) {
      return null
    }
    if (!returnType.isInstanceOf[ParameterizedType]) {
      throw new IllegalStateException("CompletableFuture return type must be parameterized" + " as CompletableFuture<Foo> or CompletableFuture<? extends Foo>")
    }
    val innerType: Type = CallAdapter.Factory.getParameterUpperBound(0, returnType.asInstanceOf[ParameterizedType])
    if (CallAdapter.Factory.getRawType(innerType) ne classOf[Response[_]]) {
      return new BodyCallAdapter(innerType)
    }
    if (!innerType.isInstanceOf[ParameterizedType]) {
      throw new IllegalStateException("Response must be parameterized" + " as Response<Foo> or Response<? extends Foo>")
    }
    val responseType: Type = CallAdapter.Factory.getParameterUpperBound(0, innerType.asInstanceOf[ParameterizedType])
    new ResponseCallAdapter(responseType)
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