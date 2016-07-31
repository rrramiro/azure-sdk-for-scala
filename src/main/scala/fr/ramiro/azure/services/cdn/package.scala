package fr.ramiro.azure.services

import fr.ramiro.azure.Azure

package object cdn {
  implicit class CdnServiceWrapper(azure: Azure) {
    def cdn = new CdnService(azure)
  }
}
