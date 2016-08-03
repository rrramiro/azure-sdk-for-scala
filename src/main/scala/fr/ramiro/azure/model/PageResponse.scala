package fr.ramiro.azure.model

class PageResponse[T] {
  var nextLink: String = null
  var value: Seq[T] = null

  def updateItems(convert: T => T): PageResponse[T] = {
    value = value.map { convert }
    this
  }
}