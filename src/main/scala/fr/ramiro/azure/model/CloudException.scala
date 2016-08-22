package fr.ramiro.azure.model

import com.microsoft.azure.CloudError
import retrofit2.Response
import scala.util.control.NoStackTrace

class CloudException(msg: String, body: CloudError, response: Response[_]) extends Exception(msg) with NoStackTrace
