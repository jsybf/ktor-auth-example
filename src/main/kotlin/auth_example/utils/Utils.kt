package auth_example.utils

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject


fun jsonPrettier(jsonObject: String): String {
    val formatter: Json = Json { prettyPrint = true }
    return formatter.decodeFromString<JsonObject>(jsonObject).let { formatter.encodeToString(it) }
}
