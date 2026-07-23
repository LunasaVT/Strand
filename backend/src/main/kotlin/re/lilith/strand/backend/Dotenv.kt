package re.lilith.strand.backend

import java.io.File

object Dotenv {

    fun load(system: Map<String, String> = System.getenv()): Map<String, String> {
        val file = findEnvFile() ?: return system
        return parse(file) + system
    }

    private fun findEnvFile(): File? {
        var dir: File? = File("").absoluteFile
        repeat(5) {
            val candidate = File(dir, ".env")
            if (candidate.isFile) return candidate
            dir = dir?.parentFile
        }
        return null
    }

    private fun parse(file: File): Map<String, String> {
        val values = LinkedHashMap<String, String>()
        file.readLines().forEach { raw ->
            val line = raw.trim().removePrefix("export ").trim()
            if (line.isEmpty() || line.startsWith("#")) return@forEach
            val index = line.indexOf('=')
            if (index <= 0) return@forEach
            val key = line.substring(0, index).trim()
            var value = line.substring(index + 1).trim()
            if (value.length >= 2 &&
                ((value.startsWith("\"") && value.endsWith("\"")) || (value.startsWith("'") && value.endsWith("'")))
            ) {
                value = value.substring(1, value.length - 1)
            }
            values[key] = value
        }
        return values
    }
}
