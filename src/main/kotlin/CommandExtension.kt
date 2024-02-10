package brightspark

import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.application.slash.PublicSlashCommandContext
import com.kotlindiscord.kord.extensions.commands.application.slash.converters.ChoiceEnum
import com.kotlindiscord.kord.extensions.commands.application.slash.converters.impl.defaultingEnumChoice
import com.kotlindiscord.kord.extensions.commands.application.slash.publicSubCommand
import com.kotlindiscord.kord.extensions.commands.converters.impl.defaultingString
import com.kotlindiscord.kord.extensions.commands.converters.impl.string
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.publicSlashCommand
import org.springframework.stereotype.Component
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneOffset

@Component
class CommandExtension : Extension() {
	companion object {
		private val REGEX_TIME = Regex("^\\s*(?<hour>\\d{1,2})(?:\\s*:\\s*(?<minute>\\d{2}))?\\s*(?<ampm>am|pm)?\\s*$")
		private val DEFAULT_FORMAT = TimestampFormat.SHORT_TIME
	}

	override val name: String = "commands"

	override suspend fun setup() {
		publicSlashCommand {
			name = "timestamp"
			description = "Timestamp commands"

			publicSubCommand(::TimestampCommandArguments) {
				name = "now"
				description = "Gets the timestamp of the time now"

				action { respondWithTimestamp(Instant.now().epochSecond, arguments.format.code) }
			}

			publicSubCommand(::TimestampAtCommandArguments) {
				name = "at"
				description = "Gets the timestamp of the given time"

				action {
					val timeMatch = REGEX_TIME.matchEntire(arguments.time)!!
					val ampm = timeMatch.groups["ampm"]?.value
					val hour = timeMatch.groups["hour"]!!.value.toInt()
					val hour24 = ampm?.let { if (it == "pm") hour + 12 else hour } ?: hour
					val minute = timeMatch.groups["minute"]?.value?.toInt()

					val timeNow = OffsetDateTime.now(ZoneOffset.of(arguments.offset))
					var timeAt = timeNow.withHour(hour24)
					minute?.let { timeAt = timeAt.withMinute(it) }
					if (timeAt < timeNow) timeAt = timeAt.plusDays(1)

					respondWithTimestamp(timeAt.toEpochSecond(), arguments.format.code)
				}
			}
		}
	}

	private suspend fun PublicSlashCommandContext<out Arguments, *>.respondWithTimestamp(
		timestamp: Long,
		format: String
	) {
		respond { content = "<t:$timestamp:$format>" }
	}

	open inner class TimestampCommandArguments : Arguments() {
		val format: TimestampFormat by defaultingEnumChoice {
			name = "format"
			description = "The timestamp format"
			typeName = "Timestamp Format"
			defaultValue = DEFAULT_FORMAT
		}
	}

	inner class TimestampAtCommandArguments : Arguments() {
		val time: String by string {
			name = "time"
			description = "The time to get the timestamp for - examples: 1, 8pm, 13:30, 4:15am"
			validate {
				val match = REGEX_TIME.matchEntire(value) ?: run {
					fail("Invalid time format `$value`!")
					return@validate
				}
				val hour = match.groups["hour"]!!.value.toInt()
				val minute = match.groups["minute"]?.value?.toInt() ?: 0
				val ampm = match.groups["ampm"]?.value

				if (hour < 0 || hour > 23) {
					fail("Time hour `$hour` out of bounds (0-23)!")
					return@validate
				}
				if (minute < 0 || minute > 59) {
					fail("Time minute `$minute` out of bounds (0-59)!")
					return@validate
				}
				ampm?.let {
					if (hour > 12) {
						fail("Time hour `$hour` out of bounds (0-12)!")
						return@validate
					}
				}
			}
		}
		val offset: String by defaultingString {
			name = "offset"
			description = "The time zone offset - examples: Z, +10, -6, +06:30, -0730"
			defaultValue = "Z"
			validate {
				failIf("Invalid offset!") {
					runCatching { ZoneOffset.of(value) }.isFailure
				}
			}
		}
		val format: TimestampFormat by defaultingEnumChoice {
			name = "format"
			description = "The timestamp format"
			typeName = "Timestamp Format"
			defaultValue = DEFAULT_FORMAT
		}
	}

	@Suppress("unused")
	enum class TimestampFormat(val code: String, override val readableName: String) : ChoiceEnum {
		SHORT_TIME("t", "Short Time"),
		LONG_TIME("T", "Long Time"),
		SHORT_DATE("d", "Short Date"),
		LONG_DATE("D", "Long Date"),
		SHORT_DATE_TIME("f", "Short Date Time"),
		LONG_DATE_TIME("F", "Long Date Time"),
		RELATIVE("R", "Relative")
	}
}
