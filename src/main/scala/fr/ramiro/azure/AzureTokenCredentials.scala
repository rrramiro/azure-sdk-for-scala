package fr.ramiro.azure

import java.util.concurrent.{ ExecutorService, Executors }

import com.microsoft.aad.adal4j.{ AuthenticationContext, AuthenticationResult, ClientCredential }
import com.microsoft.rest.credentials.TokenCredentials
import com.typesafe.config.{ Config, ConfigFactory }

object AzureTokenCredentials {
  /** The subscription GUID. */
  val SUBSCRIPTION_ID = "subscription"
  /** The tenant GUID or domain. */
  val TENANT_ID = "tenant"
  /** The client id for the client application. */
  val CLIENT_ID = "client"
  /** The client secret for the service principal. */
  val CLIENT_KEY = "key"
  /** The management endpoint. */
  val MANAGEMENT_URI = "managementURI"
  /** The base URL to the current Azure environment. */
  val BASE_URL = "baseURL"
  /** The URL to Active Directory authentication. */
  val AUTH_URL = "authURL"

  def apply(config: Config = ConfigFactory.load()) = {
    new AzureTokenCredentials(
      config.getString(CLIENT_ID),
      config.getString(TENANT_ID),
      config.getString(CLIENT_KEY),
      config.getString(SUBSCRIPTION_ID),
      config.getString(AUTH_URL),
      config.getString(MANAGEMENT_URI),
      config.getString(BASE_URL)
    )
  }
}

class AzureTokenCredentials(
    clientId: String,
    domain: String,
    secret: String,
    val subscriptionId: String,
    authenticationEndpoint: String = "https://login.windows.net/",
    tokenAudience: String = "https://management.core.windows.net/",
    val baseUrl: String = "https://management.azure.com/"
) extends TokenCredentials(null, null) {

  var authenticationResult: AuthenticationResult = null

  override def getToken = {
    if (authenticationResult == null || authenticationResult.getAccessToken == null) {
      refreshToken()
    }
    authenticationResult.getAccessToken
  }

  override def refreshToken(): Unit = {
    val executor: ExecutorService = Executors.newSingleThreadExecutor
    try {
      authenticationResult = new AuthenticationContext(authenticationEndpoint + domain, true, executor)
        .acquireToken(tokenAudience, new ClientCredential(clientId, secret), null).get
    } finally {
      executor.shutdown()
    }
  }
}

