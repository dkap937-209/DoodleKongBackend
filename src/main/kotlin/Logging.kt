import io.ktor.server.application.*
import io.ktor.server.plugins.callid.*
import io.ktor.server.plugins.calllogging.*
import io.ktor.server.request.*
import org.slf4j.event.Level
import java.util.*

fun Application.configureLogging() {
    install(CallId) {
        header("X-Request-ID")
        generate { UUID.randomUUID().toString() }
        verify { callId -> callId.isNotEmpty() }
        replyToHeader("X-Request-ID")
    }

    install(CallLogging) {
        callIdMdc("requestId")
        level = Level.INFO
        filter { call -> call.request.path().startsWith("/") }
        disableDefaultColors()
    }
}