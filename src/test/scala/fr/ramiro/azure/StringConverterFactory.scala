package fr.ramiro.azure

import java.lang.annotation.Annotation
import java.lang.reflect.Type

import okhttp3.{ MediaType, RequestBody, ResponseBody }
import retrofit2.{ Converter, Retrofit }

class StringConverterFactory extends Converter.Factory {
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
