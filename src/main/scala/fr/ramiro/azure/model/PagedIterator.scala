package fr.ramiro.azure.model

import java.util.NoSuchElementException

abstract class PagedIterator[E](page: PageResponse[E]) extends Iterator[E] {
  private var nextLink: String = page.nextLink
  private var value: Seq[E] = page.value
  private var itemsListItr: Iterator[E] = null

  def nextPage(nextPageLink: String): PageResponse[E]

  def hasNext: Boolean = {
    if (itemsListItr == null) {
      itemsListItr = value.iterator
    }
    itemsListItr.hasNext || hasNextPage
  }

  private def hasNextPage: Boolean = nextLink != null

  def next: E = {
    if (itemsListItr == null) {
      itemsListItr = value.iterator
    }
    if (!itemsListItr.hasNext) {
      if (!hasNextPage) {
        throw new NoSuchElementException
      } else {
        val newPage = nextPage(nextLink)
        nextLink = newPage.nextLink
        value = newPage.value
        itemsListItr = value.iterator
      }
    }
    itemsListItr.next
  }
}
