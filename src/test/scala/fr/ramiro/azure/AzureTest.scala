package fr.ramiro.azure

import fr.ramiro.azure.services.cdn._
import org.scalatest.FunSuite
import System.getenv
class AzureTest extends FunSuite {
  test("purge") {
    Azure(AzureTokenCredentials()).cdn.cdnPurge(getenv("resourceGroupName"), getenv("profileName"), getenv("endpointName"), "/*")
  }
}
