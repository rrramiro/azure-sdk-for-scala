package fr.ramiro.azure.services

import fr.ramiro.azure.Azure
import fr.ramiro.azure.services.resourceGroups.model.ResourceGroup
import okhttp3.ResponseBody
import org.scalamock.scalatest.MockFactory
import org.scalatest.FunSuite
import retrofit2.Call
import scala.collection.JavaConverters.collectionAsScalaIterableConverter

class PagesServiceTest extends FunSuite with MockFactory with ServicesFixture {
  val mockCall = mock[Call[ResponseBody]]

  object FakePagedService extends PagedService[ResourceGroup] {
    override val objectMapper = Azure.objectMapper
    override def listInternal: Call[ResponseBody] = mockCall
    override def listNextInternal(nextPageLink: String): Call[ResponseBody] = mockCall
    override def getInternal(id: String): Call[ResponseBody] = mockCall
    override def addParent(child: ResourceGroup): ResourceGroup = child
  }

  test("paged") {
    (mockCall.execute _).expects().returns(createSuccessResponse(resourceGroupFirstPage))
    (mockCall.execute _).expects().returns(createSuccessResponse(resourceGroupLastPage))
    val result = FakePagedService.list.getBody.asScala
    assert(result.size === 2)
    assert(result.head.name === "myresourcegroup1")
    assert(result.last.name === "myresourcegroup2")
  }

  val resourceGroupFirstPage =
    """
      |{
      |  "value": [ {
      |    "id": "/subscriptions/########-####-####-####-############/resourceGroups/myresourcegroup1",
      |    "name": "myresourcegroup1",
      |    "location": "westus",
      |    "tags": {
      |      "tagname1": "tagvalue1"
      |    },
      |    "properties": {
      |      "provisioningState": "Succeeded"
      |    }
      |  } ],
      |  "nextLink": "https://management.azure.com/subscriptions/########-####-####-####-############/resourcegroups?api-version=2015-01-01&$skiptoken=######"
      |}
    """.stripMargin

  val resourceGroupLastPage =
    """
      |{
      |  "value": [ {
      |    "id": "/subscriptions/########-####-####-####-############/resourceGroups/myresourcegroup2",
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
