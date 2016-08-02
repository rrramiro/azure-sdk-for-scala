package fr.ramiro.azure.rest

import java.lang.reflect.Type

import com.google.common.reflect.TypeToken
import fr.ramiro.azure.Azure
import fr.ramiro.azure.model.PageImpl
import fr.ramiro.azure.services.ListService
import fr.ramiro.azure.services.cdn.model.CdnProfile
import okhttp3.{ MediaType, ResponseBody }
import org.scalamock.scalatest.MockFactory
import org.scalatest.FunSuite
import retrofit2.{ Call, Response }
import scala.collection.JavaConverters.collectionAsScalaIterableConverter

class AzureServiceResponseBuilderTest extends FunSuite with MockFactory {
  val mockCall = mock[Call[ResponseBody]]

  object FakeListService extends ListService[CdnProfile] {
    override val mapperAdapter = new Azure.AzureJacksonMapperAdapter
    override val listedType: Type = new TypeToken[PageImpl[CdnProfile]]() {}.getType
    override val getType: Type = new TypeToken[CdnProfile]() {}.getType
    override val listInternal: Call[ResponseBody] = mockCall
    override def getInternal(id: String): Call[ResponseBody] = mockCall
    override def addParent(child: CdnProfile): CdnProfile = child
  }

  test("list") {
    (mockCall.execute _).expects().returns(Response.success[ResponseBody](
      ResponseBody.create(MediaType.parse("text/plain"), cdnProfileList)
    ))

    val result = FakeListService.list.getBody.asScala

    assert(result.size === 2)
    assert(result.head.name === "{profileName}")
    assert(result.last.name === "{profileName}")
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