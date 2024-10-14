package auth_example

import io.github.cdimascio.dotenv.Dotenv
import io.github.cdimascio.dotenv.dotenv
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*

fun Application.module() {
    val dotenv: Dotenv = dotenv()

    val kakaoAuthService: KakaoAuthService = KakaoAuthService(
        redirectUri = dotenv["REDIRECT_URI"],
        clientId = dotenv["KAKAO_CLIENT_KEY"],
        httpClient = HttpClient(CIO)
    )

    configSession()
    configSessionAuth()
    configKakaoAuthRouting(kakaoAuthService)
}

private fun Application.configSession() = install(Sessions) {
    //TODO: sessionStorage로 redis 사용
    cookie<UserSession>("user_session_1", SessionStorageMemory()) {
        // cookie.path = "/"
        cookie.extensions["SameSite"] = "Lax"
        cookie.maxAgeInSeconds = 60
    }
}

private fun Application.configSessionAuth() = install(Authentication) {
    session<UserSession>("auth-session") {
        validate { userSession: UserSession ->
            println(userSession)
            userSession
        }
        challenge {
            println("challenge")
            call.respond(HttpStatusCode.Unauthorized)
        }
    }
}

private fun Application.configKakaoAuthRouting(kakaoAuthService: KakaoAuthService) = routing {

    get("/login") {
        kakaoAuthService.run { kakaoAuthRedirect() }
    }
    get("/authenticate") {
        kakaoAuthService.run { authenticate() }
    }
}
