package fr.ramiro.azure.model

class PageResponse[T] {
  var nextLink: String = null
  var value: Seq[T] = null

  def hasNextPage: Boolean = nextLink != null

}
