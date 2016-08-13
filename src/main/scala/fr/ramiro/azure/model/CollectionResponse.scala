package fr.ramiro.azure.model

import com.fasterxml.jackson.annotation.JsonIgnore

case class CollectionResponse[T](
    nextLink: String = null,
    value: Seq[T]
) {
  @JsonIgnore
  def hasNextPage: Boolean = nextLink != null
}
