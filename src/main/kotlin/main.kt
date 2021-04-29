import Log.*
import java.lang.StringBuilder

/**
 * 14. Készítsen programot, amely elvégzi egy szöveg Lempel-Ziv kódolását!
 * Kérek egy részletes elméleti leírást a különböző változatokról (pl.
 * LZ77, LZ78, LZW stb.).
 * Adatok beolvasása szöveges file-ból. Kiíratás szöveges file-ba.
 * Beadandó forrás és futtatható változat, program leírás és néhány futási
 * példa.
 *
 * Kérem, hogy a megoldás tartalmazzon egy leírást,
 * hogy milyen környezetben és hogyan futtatható a program.
 */

//const val inputString = "aababbbabaababbbabbabb"
//const val inputString = "ababcbababaa"
const val inputString = "aacaacabcabaaac"

val window = mutableMapOf<String, Int>()
val rootLogLevel = DEBUG

enum class Log(val level: Int) {
    TRACE(1),
    DEBUG(2),
    INFO(3),
}

fun main(args: Array<String>) {
    "Starting program".print(INFO)
    var tempString = inputString.onEachIndexed { index, _ -> "at index $index is ${inputString[index]}".print(TRACE)}
    var cursor: Int = 0
    while (cursor < inputString.length) {
        val prefix = registerPrefixFrom(cursor, tempString).also { "prefix [ $it ]".print(DEBUG) }

        val increment = when (prefix.isNotEmpty()) {
            true -> {
                val increment = Increment(cursor , prefix.length, tempString.nextCharAfter(prefix.length))
                cursor += prefix.length
                increment
            }
            false -> {
                // TODO: is this case even possible?
//                cursor += 1
//                if (tempString.isEmpty())
//                    continue
//                Increment(0, 0, tempString[0])
                "PREFIX WAS EMPTY!!".print(INFO)
                Increment(-1, -1, '-')
            }
        }
        output(increment)

        tempString = tempString.removeRange(0 until increment.length).also { it.print(TRACE) }

        // TODO :Move lookup window
    }

    window.print(DEBUG) { "dictionay:" }.forEach { "${it.key}, ${it.value}".print(DEBUG) }
    "Ending program".print(INFO)
}

fun String.nextCharAfter(lastIndex: Int): Char {
    if (lastIndex >= this.length) {
        return this.last()
    }
    return this[lastIndex + 1]
}

fun output(increment: Increment) = println(increment)

data class Increment(val offset: Int, val length: Int, val nextCharacter: Char) {
    override fun toString(): String {
        return "Triple($offset, $length, $nextCharacter)"
    }
}

fun registerPrefixFrom(cursor: Int, source: String) =
    source.prefixMatches { !window.containsKey(it) }
        .also { window[it] = cursor }
        .print(DEBUG) { "new prefix saved to dictionary [ $it ]" }


//fun findNextPefixFrom(source: String){
//    val buffer = StringBuilder()
//    for (index in 0 until length) {
//        "appending [ ${this[index]} ] to buffer".print(TRACE)
//        buffer.append(this[index])
//
//        val currentPrefix = buffer.toString()
//        "looking [ $currentPrefix ] up in dictionary".print(TRACE)
//        if (predicate.invoke(currentPrefix)) {
//            "[ $currentPrefix ] is a new prefix \n".print(TRACE)
//            return substring(0, index + 1)
//        }
//        "[ $currentPrefix ] is not a prefix".print(TRACE)
//    }
//    "no prefix found, returning $this".print(TRACE)
//    return this
//}

// TODO("inline")
fun String.prefixMatches(predicate: (String) -> Boolean): String {
    "\nfinding prefix for [ $this ]".print(TRACE)
    val buffer = StringBuilder()
    for (index in 0 until length) {
        "appending [ ${this[index]} ] to buffer".print(TRACE)
        buffer.append(this[index])

        val currentPrefix = buffer.toString()
        "looking [ $currentPrefix ] up in dictionary".print(TRACE)
        if (predicate.invoke(currentPrefix)) {
            "[ $currentPrefix ] is a new prefix \n".print(TRACE)
            return substring(0, index + 1)
        }
        "[ $currentPrefix ] is not a prefix".print(TRACE)
    }
    "no prefix found, returning $this".print(TRACE)
    return this
}

fun <T> T.print(logLevel: Log, messageCreator: (String) -> String): T {
    if (logLevel >= rootLogLevel) println(messageCreator(this.toString()))
    return this
}

fun <T> T.print(logLevel: Log) : T {
    if (logLevel >= rootLogLevel) println(this)
    return this
}

