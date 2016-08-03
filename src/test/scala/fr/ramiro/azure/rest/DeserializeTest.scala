package fr.ramiro.azure.rest

import fr.ramiro.azure.Azure
import fr.ramiro.azure.model.ListResponse
import fr.ramiro.azure.services.cdn.model.CdnProfile
import org.scalatest.FunSuite
import scala.reflect.ClassTag

class DeserializeTest extends FunSuite {
  val objectMapper = Azure.objectMapper

  val builder = new AzureServiceResponseBuilder(Azure.objectMapper, null)
  test("deserializeList") {
    val result: ListResponse[CdnProfile] = builder.deserializeList[CdnProfile](cdnProfileList)
    assert(result.value.size == 2)
    assert(result.value.head.name === "{profileName}")
    assert(result.value.last.name === "{profileName}")
  }

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
