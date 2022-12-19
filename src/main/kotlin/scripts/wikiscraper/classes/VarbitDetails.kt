package scripts.wikiscraper.classes


/* Written by IvanEOD 12/8/2022, at 6:56 PM */
data class VarbitDetails(
    val type: VarbitType,
    val index: Int,
    val name: String,
    val content: String,
    val classType: ClassType,
    val description: String
) {
    enum class VarbitType {
        Varbit,
        VarPlayer
    }

    enum class ClassType {
        Enum,
        Switch,
        Bitmap,
        Counter,
        Other
    }

    companion object {

        fun fromText(text: String): VarbitDetails {
            val lines = text.split("\n")
            var foundVarbitEnd = false
            var index = 0
            var type: VarbitType = VarbitType.Varbit
            var classType = ClassType.Other
            var varbitIndex = 0
            var content = ""
            var description = ""
            var name = ""
            while (index < lines.size) {
                val line = lines[index]
                if (index == 0 && !line.startsWith("{{Infobox Var")) {
                    index++
                    continue
                }
                index++
                if (line == "{{Similar Vars}}") break
                if (foundVarbitEnd) description += line
                if (line.startsWith("}}")) foundVarbitEnd = true
                if (line.startsWith("|")) {
                    val split = line.split("=")
                    if (split.size < 2) continue
                    val key = split[0].substring(1).trim()
                    val value = split[1].trim()
                    when (key) {
                        "type" -> type = if (value.equals("varbit", true)) VarbitType.Varbit else VarbitType.VarPlayer
                        "index" -> varbitIndex = if (value == "NUMBER") continue
                        else if (value.toIntOrNull() == null) {
                            println("Unknown index: $value - $name - $text")
                            -1
                        }  else value.toInt()
                        "class" -> classType = kotlin.runCatching { ClassType.valueOf(value) }.getOrNull() ?: continue
                        "content" -> content = value
                        "name" -> name = value
                    }
                }

            }
            return VarbitDetails(type, varbitIndex, name, content, classType, description.trim())
        }

    }


}


