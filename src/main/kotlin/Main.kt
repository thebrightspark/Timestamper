package brightspark

import com.kotlindiscord.kord.extensions.ExtensibleBot
import com.kotlindiscord.kord.extensions.extensions.Extension
import kotlinx.coroutines.runBlocking
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import kotlin.concurrent.thread

@SpringBootApplication
class TimestamperApp {
	@Bean
	fun bot(
		@Value("\${bot.token}") token: String,
		extensions: Collection<Extension>
	): ExtensibleBot = runBlocking {
		ExtensibleBot(token) {
			intents(addDefaultIntents = false, addExtensionIntents = true) {}
			extensions.forEach { extensionsBuilder.add { it } }
		}.apply { thread { runBlocking { start() } } }
	}
}

fun main(args: Array<String>) {
	runApplication<TimestamperApp>(*args)
}
