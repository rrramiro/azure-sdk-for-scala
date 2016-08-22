package fr.ramiro.azure.services

import fr.ramiro.azure.RetrofitAzure
import okhttp3.mockwebserver.{ MockResponse, MockWebServer }
import org.scalamock.scalatest.MockFactory
import org.scalatest.{ BeforeAndAfterEach, FunSuite }
import retrofit2.Retrofit

class ListServiceTest extends FunSuite with MockFactory with BeforeAndAfterEach {
  private var server: MockWebServer = _
  private var retrofit: Retrofit = _
  private var cdnProfilesService: CdnProfilesService = _

  override def beforeEach {
    server = new MockWebServer
    retrofit = RetrofitAzure.retrofit(server.url("/"))
    cdnProfilesService = new CdnProfilesService(retrofit, "subscriptionId", "resourceGroupName")
  }

  test("list") {
    server.enqueue(new MockResponse().setBody(cdnProfileList))
    val result = cdnProfilesService.list
    assert(result.size === 2)
    assert(result.head.name === "{profileName}")
    assert(result.last.name === "{profileName}")
  }

  test("get") {
    server.enqueue(new MockResponse().setBody(cdnProfile))
    val result = cdnProfilesService.get("{profileName}")
    assert(result !== null)
    assert(result.name === "{profileName}")
  }

  val cdnProfile =
    """
      |{
      |    "id": "/subscriptions/{id}/resourceGroups/{resourceGroupName}/providers/Microsoft.Cdn/Profiles/{profileName}",
      |    "name": "{profileName}",
      |    "type": "Microsoft.Cdn/Profiles",
      |    "location": "East US",
      |    "tags": {
      |        "key1": "value1",
      |        "key2": "value2"
      |    },
      |    "sku": {
      |            "name": "Standard_Verizon|Premium_Verizon|Standard_Akamai"
      |    },
      |    "properties": {
      |        "provisioningState": "Succeeded|InProgress",
      |        "resourceState": "Active"
      |    }
      |}
    """.stripMargin

  val cdnProfileList =
    """
      |{
      |    "value": [
      |    {
      |        "id": "/subscriptions/{id}/resourceGroups/{resourceGroupName}/providers/Microsoft.Cdn/Profiles/{profileName}",
      |        "name": "{profileName}",
      |        "type": "Microsoft.Cdn/Profiles",
      |        "location": "East US",
      |        "tags": {
      |            "key1": "value1",
      |            "key2": "value2"
      |        },
      |        "sku": {
      |                "name": "Standard_Verizon|Premium_Verizon|Standard_Akamai"
      |        },
      |        "properties": {
      |            "provisioningState": "Succeeded|InProgress",
      |            "resourceState": "Active"
      |        }
      |    },
      |    {
      |        "id": "/subscriptions/{id}/resourceGroups/{resourceGroupName}/providers/Microsoft.Cdn/Profiles/{profileName}",
      |        "name": "{profileName}",
      |        "type": "Microsoft.Cdn/Profiles",
      |        "location": "East US",
      |        "tags": {
      |            "key1": "value1",
      |            "key2": "value2"
      |        },
      |        "sku": {
      |                "name": "Standard_Verizon|Premium_Verizon|Standard_Akamai"
      |        },
      |        "properties": {
      |            "provisioningState": "Succeeded|InProgress",
      |            "resourceState": "Active"
      |        }
      |    }
      |    ]
      |}
    """.stripMargin
}
