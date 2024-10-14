package auth_example.sample

import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*


fun Application.configSamplePageRouting() {
    routing {
        authenticate("auth-session") {
            get("/protected/hello") { call.respond("hello") }
        }
    }
}
