package auth_example

import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.sessions.*
import io.ktor.server.util.*
import io.ktor.util.pipeline.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

typealias CallPipelineContext = PipelineContext<Unit, ApplicationCall>

@Serializable
data class UserSession(val name: String, val count: Int) : Principal


/**
 * 책임: 카카오 oauth로 인증/인가 및 세션 부여하기
 */
class KakaoAuthService(
    private val redirectUri: String,
    private val clientId: String,
    private val httpClient: HttpClient
) {
    /**
     * 카카오 auth page로 redirect
     * 로그인 기능의 entrypoint
     * @param
     */
    suspend fun CallPipelineContext.kakaoAuthRedirect() {
        val kakaoAuthUrl: String = url {
            protocol = URLProtocol.HTTPS
            host = "kauth.kakao.com"
            path("oauth", "authorize")
            parameters.append("client_id", clientId)
            parameters.append("redirect_uri", redirectUri)
            parameters.append("response_type", "code")
        }

        call.respondRedirect(kakaoAuthUrl)
    }

    /**
     * 카카오 로그인 페이지에서 확인을 누르면 카카오 서버가 호출함
     * query param으로 인가코드, 에러메세지 반환받음
     */
    suspend fun CallPipelineContext.authenticate() {
        // query param parse
        val code: String? = call.parameters["code"]
        val state: String? = call.parameters["state"]
        val error: String? = call.parameters["error"]
        val errorDescription: String? = call.parameters["error_description"]

        // 카카오 페이지 인가 실패 -> Unautorized 반환
        if (code == null && state == null) {
            val errorMessage = mapOf("error_code" to error!!, "errorDescription" to errorDescription!!)
            call.respond(HttpStatusCode.Unauthorized, errorMessage)
            return
        }

        // requestAccessToken 호출 -> access_token 받기
        val tokenResp: JsonObject = requestAccessToken(httpClient, clientId, redirectUri, code!!)
        val accessToken: String = tokenResp["access_token"]!!.jsonPrimitive.toString()

        // access_token 바탕으로 사용자 정보 받기
        val userInfoResp: JsonObject = requestUserInfo(httpClient, accessToken)

        // set session
        // TODO: 세션 생성하는 함수를 따로 함수인자로 받던가 세션 생성 함수를 오버로딩 할 수 있도록 이 클래스 자체를 abstract 클래스로 변환
        call.sessions.set(UserSession(userInfoResp["properties"]!!.jsonObject["nickname"]!!.jsonPrimitive.toString(), 1))

        // TODO: redirect to main page
        call.respond(Json { prettyPrint = true }.encodeToString(userInfoResp))
    }

    private suspend fun requestAccessToken(httpClient: HttpClient, clientId: String, redirectUri: String, authCode: String): JsonObject {
        // TODO: char utf-8 명시하기
        val resp: HttpResponse = httpClient.submitForm(
            url = "https://kauth.kakao.com/oauth/token",
            formParameters = parameters {
                append("grant_type", "authorization_code")
                append("client_id", clientId)
                append("redirect_uri", redirectUri)
                append("code", authCode)
            }
        )
        val respBody: JsonObject = Json.decodeFromString<JsonObject>(resp.bodyAsText())
        return respBody
    }

    private suspend fun requestUserInfo(httpClient: HttpClient, accessToken: String): JsonObject {
        // TODO: char utf-8 명시하기
        val resp: HttpResponse = httpClient.submitForm("https://kapi.kakao.com/v2/user/me") {
            header("Authorization", "Bearer ${accessToken}")
        }
        val respBody: JsonObject = Json.decodeFromString<JsonObject>(resp.bodyAsText())
        return respBody
    }

}


