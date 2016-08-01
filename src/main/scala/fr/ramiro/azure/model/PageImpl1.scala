package fr.ramiro.azure.model

import java.util

import com.fasterxml.jackson.annotation.JsonProperty
import com.microsoft.azure.Page
import scala.collection.JavaConverters.collectionAsScalaIterableConverter
import scala.collection.JavaConverters.seqAsJavaListConverter

class PageImpl1[T] extends Page[T] {
  @JsonProperty("nextLink") private var nextPageLink: String = null
  @JsonProperty("value") private var items: util.List[T] = null

  def getNextPageLink: String = nextPageLink

  def getItems: util.List[T] = items

  def setNextPageLink(nextPageLink: String): PageImpl1[T] = {
    this.nextPageLink = nextPageLink
    this
  }

  def setItems(items: util.List[T]): PageImpl1[T] = {
    this.items = items
    this
  }

  def updateItems(convert: T => T): PageImpl1[T] = {
    setItems(getItems.asScala.map { convert }.toSeq.asJava)
  }
}