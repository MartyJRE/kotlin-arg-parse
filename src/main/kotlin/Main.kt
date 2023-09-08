import kotlin.system.exitProcess

fun main(args: Array<String>) {
    val parser = ArgumentParser()

    parser.addArgument("width", Float, "Describes something something something", true)
    parser.addArgument("test", "Sets the dry run")
    parser.addArgument("height", String, "", false)

    val result = parser.parseArgs(args)

    println(result)
}

data class Argument<T>(
    val name: String,
    val type: T,
    val required: Boolean,
    val storeTrue: Boolean,
    val description: String?
)

class ArgumentParser {
    private var arguments: List<Argument<*>> = emptyList()

    fun <T> addArgument(name: String, type: T, description: String, required: Boolean = false) {
        arguments += Argument(name, type, required, false, description)
    }

    fun addArgument(name: String, description: String, required: Boolean = false) {
        arguments += Argument(name, Boolean, required, true, description)
    }

    private fun <T> companionToTypeName(companionString: T): String {
        return companionString.toString()
            .replace("^kotlin\\.jvm\\.internal\\.(\\w+)CompanionObject@.+$".toRegex(), "$1")
    }

    private fun printUsage() {
        println("\nUsage:\n<program> \\")
        val argumentsLength = arguments.size
        var i = 0
        arguments.forEach {
            if (it.storeTrue) {
                val usage = "  --${it.name}"
                printUsageOfItem(it, usage, argumentsLength, i)
            } else {
                val usage = "  --${it.name} <value: ${companionToTypeName(it.type)}>"
                printUsageOfItem(it, usage, argumentsLength, i)
            }
            i++
        }
    }

    private fun printUsageOfItem(
        it: Argument<*>,
        usage: String,
        argumentsLength: Int,
        i: Int
    ) {
        var usageCopy = usage
        if (argumentsLength - 1 > i) {
            usageCopy += " \\"
        }
        if (it.description != null && it.description.isNotEmpty()) {
            usageCopy += " (${it.description})"
        }

        println(usageCopy)
    }

    fun parseArgs(args: Array<String>): MutableMap<String, Any> {
        val result: MutableMap<String, Any> = mutableMapOf()
        val iter = args.iterator()

        while (iter.hasNext()) {
            val current = iter.next()
            val trimmed = current.trim('-')

            val found = arguments.find { it.name == trimmed } ?: continue

            if (found.storeTrue) {
                result[trimmed] = true
            } else {
                if (!iter.hasNext()) {
                    printUsage()
                    exitProcess(1)
                }
                val next = iter.next()
                try {
                    val parsed = when (found.type) {
                        is Int.Companion -> {
                            next.toInt()
                        }

                        is Float.Companion -> {
                            next.toFloat()
                        }

                        is Char.Companion -> {
                            next[0]
                        }

                        is Boolean.Companion -> {
                            next.toBooleanStrict()
                        }

                        is String.Companion -> {
                            next
                        }

                        else -> {
                            println("Unknown type of ${found.name}: ${companionToTypeName(found.type)}")
                            printUsage()
                            exitProcess(1)
                        }
                    }

                    result[trimmed] = parsed
                } catch (error: Exception) {
                    println(
                        "The value \"$next\" of argument ${found.name} could not be parsed as ${
                            companionToTypeName(
                                found.type
                            )
                        }."
                    )
                    printUsage()
                    exitProcess(1)
                }
            }
        }

        arguments.forEach {
            if (it.required && !result.containsKey(it.name)) {
                println(
                    "Required argument ${it.name} was not provided."
                )
                printUsage()
                exitProcess(1)
            }
        }

        return result
    }
}
