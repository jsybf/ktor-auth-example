import io.ktor.http.*
import io.ktor.server.util.*
import kotlin.test.Test

class UrlBuilderTest {
    @Test
    fun urlBuilderExample() {
        val url: String = url {
            this.host = "kauth.kakao.com"
            this.path("oauth", "authorize")
            parameters.append("response_type", "code")
            parameters.append("client_id", "a28eae8adcc2baf8ded7e86fc1b59532")
            parameters.append("redirect_uri", "foo")
        }

        println(url)
    }
}