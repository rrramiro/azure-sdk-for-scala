package fr.ramiro.azure.services

import fr.ramiro.azure.RetrofitAzure
import fr.ramiro.azure.model.CloudException
import okhttp3.HttpUrl
import okhttp3.mockwebserver.{ Dispatcher, MockResponse, MockWebServer, RecordedRequest }
import org.scalamock.scalatest.MockFactory
import org.scalatest.{ BeforeAndAfterAll, FunSuite }
import retrofit2.Retrofit

class PagesServiceTest extends FunSuite with MockFactory with BeforeAndAfterAll {
  private var server: MockWebServer = _
  private var retrofit: Retrofit = _
  private var resourceGroupsService: ResourceGroupsService = _
  private var baseUrl: HttpUrl = _
  private val subscriptionId = "subscriptionId"

  override def beforeAll = {
    server = new MockWebServer
    baseUrl = server.url("/")
    retrofit = RetrofitAzure.retrofit(baseUrl)
    server.setDispatcher(new Dispatcher() {
      override def dispatch(request: RecordedRequest): MockResponse = {
        request.getPath.split("[?]").head match {
          case "/subscriptions/subscriptionId/resourcegroups/error" =>
            new MockResponse().setResponseCode(404).setBody(jsonError)
          case "/subscriptions/subscriptionId/resourcegroups/empty" =>
            new MockResponse().setResponseCode(404).setBody("")
          case "/subscriptions/subscriptionId/resourcegroups" =>
            new MockResponse().setBody(resourceGroupFirstPage)
          case "/subscriptions/subscriptionId/resourcegroups/next" =>
            new MockResponse().setBody(resourceGroupLastPage)
          case _ =>
            new MockResponse().setResponseCode(404)
        }
      }
    })
    resourceGroupsService = new ResourceGroupsService(retrofit, subscriptionId)
  }

  test("2 page success") {
    val result = resourceGroupsService.list
    assert(result.size === 2)
    assert(result.head.name === "myresourcegroup1")
    assert(result.last.name === "myresourcegroup2")
  }

  test("error") {
    val caught = intercept[CloudException] {
      resourceGroupsService.get("error")
    }
    assert(caught.getMessage === "Invalid status code 404")
  }

  test("error empty") {
    val caught = intercept[CloudException] {
      resourceGroupsService.get("empty")
    }
    assert(caught.getMessage === "Invalid status code 404")
  }

  test("error null") {
    val caught = intercept[CloudException] {
      resourceGroupsService.get("")
    }
    assert(caught.getMessage === "Invalid status code 404")
  }

  lazy val jsonError =
    """
      |{
      |  "Code": "NotFound",
      |  "Message": "Cannot find XXX.",
      |  "Target": null,
      |  "Details": [
      |    {
      |      "Message": "Cannot find XXX."
      |    },
      |    {
      |      "Code": "NotFound"
      |    }
      |  ]
      |}
    """.stripMargin

  lazy val resourceGroupFirstPage =
    s"""
      |{
      |  "value": [ {
      |    "id": "/subscriptions/subscriptionId/resourceGroups/myresourcegroup1",
      |    "name": "myresourcegroup1",
      |    "location": "westus",
      |    "tags": {
      |      "tagname1": "tagvalue1"
      |    },
      |    "properties": {
      |      "provisioningState": "Succeeded"
      |    }
      |  } ],
      |  "nextLink": "/subscriptions/subscriptionId/resourcegroups/next?api-version=2015-01-01&$$skiptoken=######"
      |}
    """.stripMargin

  lazy val resourceGroupLastPage =
    """
      |{
      |  "value": [ {
      |    "id": "/subscriptions/subscriptionId/resourceGroups/myresourcegroup2",
      |    "name": "myresourcegroup2",
      |    "location": "westus",
      |    "tags": {
      |      "tagname2": "tagvalue2"
      |    },
      |    "properties": {
      |      "provisioningState": "Succeeded"
      |    }
      |  } ]
      |}
    """.stripMargin
}
