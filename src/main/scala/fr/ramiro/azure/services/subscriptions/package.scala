package fr.ramiro.azure.services

import fr.ramiro.azure.Azure

package object subscriptions {
  implicit class SubscriptionsServiceWrapper(azure: Azure) {
    def subscriptions = new SubscriptionsService(azure)
  }
}
