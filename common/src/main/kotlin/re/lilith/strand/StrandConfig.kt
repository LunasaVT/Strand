package re.lilith.strand

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.exists

@Serializable
data class StrandConfig(
    val oidcClientId: String = "strand",
    val oidcRedirectUri: String = "http://127.0.0.1:4924/callback",
    val autoHostOnLanOpen: Boolean = true,
    val hideCodeInChat: Boolean = false,
) {
    companion object {
        private val json = Json { prettyPrint = true; ignoreUnknownKeys = true; encodeDefaults = true }

        fun load(configDir: Path): StrandConfig {
            val path = configDir.resolve("strand.json")
            if (!path.exists()) {
                val default = StrandConfig()
                runCatching { Files.writeString(path, json.encodeToString(default)) }
                return default
            }
            val parsed = runCatching { json.decodeFromString<StrandConfig>(Files.readString(path)) }
                .getOrElse { StrandConfig() }
            return parsed
        }

    }
}
