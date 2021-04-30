import Log.INFO
import Log.TRACE

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

const val LOOKUP_WINDOW_SIZE = 6
val rootLogLevel = TRACE


class SlidingWindow(private val maxSize: Int, private val window: ArrayDeque<Char> = ArrayDeque(maxSize)) {
    fun pop(amount: Int = 1) : SlidingWindow {
        repeat(amount) {
            window.removeFirstOrNull().let {
                if(it == null) {
                    return@pop this
                }
            }
        }
        return this
    }

    fun push(chars: CharSequence) : SlidingWindow {
        window.addAll(chars.toList())
        return this
    }

    override fun toString(): String {
        return window.toString()
    }
}

enum class Log(val level: Int) {
    TRACE(1),
    DEBUG(2),
    INFO(3),
}

fun main(args: Array<String>) {
    "Starting program".print(INFO)
    val lookUpWindow = SlidingWindow(LOOKUP_WINDOW_SIZE)
    lookUpWindow.push("123").print()
    lookUpWindow.push("45").print()
    lookUpWindow.pop(2).print()
    lookUpWindow.pop(10).print()
    lookUpWindow.push("6789").print()
    lookUpWindow.pop(4).print()
    while (lookUpWindow.toString() != "[]") {
        lookUpWindow.pop().print()
    }
    return

//    var tempString = inputString.print(INFO) { "input string: $it" }
//        .onEachIndexed { index, _ -> "at index $index is ${inputString[index]}".print(TRACE) }

    var cursor: Int = 0
    while (cursor < inputString.length) {
//        val result = tempString.findLongestExistingPrefix { lookUpWindow.contains(it) }

//        val increment = when (result) {
//            is FullPrefix -> {
//                cursor += 1
//                lookUpWindow.add(result.prefix)
////                tempString = tempString.substring(1) TODO: wtf to do here? xd
//                Increment(0, 0, ' ')
//            } // TODO: this is still BS!
//            is SingleCharPrefix -> {
//                cursor += 1
//                lookUpWindow.add(result.char.toString())
//                tempString = tempString.substring(1)
//                Increment(0, 0, result.char)
//            }
//            is SubPrefix -> {
//                cursor += result.prefix.length
//                lookUpWindow.add(result.prefix)
//                tempString = tempString.substring(result.prefix.length)
//                Increment(result.prefix.length, result.prefix.length, result.nextChar)
//            }
//        }
//        output(increment).print(INFO) { "\n" }
//        tempString.print(TRACE) { "remaining input: $it" }
////        TODO :Move lookup window
    }
//    lookUpWindow.print(DEBUG) { "dictionary:" }.forEach { it.print(DEBUG) }
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
//
//fun registerPrefixFrom(cursor: Int, source: String) =
//    source.prefixMatches { !lookUpWindow.containsKey(it) }
//        .also { lookUpWindow[it] = cursor }
//        .print(DEBUG) { "new prefix saved to dictionary [ $it ]" }


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

sealed class Prefix
class FullPrefix(val prefix: String) : Prefix()
class SubPrefix(val prefix: String, val nextChar: Char) : Prefix()
class SingleCharPrefix(val char: Char) : Prefix()

//fun String.findLongestExistingPrefix(doesExist: (String) -> Boolean): Prefix {
//    var startIndex = 0
//
//
//
//
//}


//fun String.findLongestExistingPrefix(doesExist: (String) -> Boolean): Prefix {
//    val firstChar = this[0]
//    val prefix = StringBuffer().append(firstChar)
//
//    // if the first char is already a new prefix, we handle that differently
//    if (!doesExist(prefix.toString())) {
//        "Found non-existing single-prefix: [ $prefix ]\n".print(TRACE)
//        return SingleCharPrefix(firstChar)
//    }
//
//    // finding the longest existing prefix, checking one-by-one till the is no existing prefix
//    for (index in 1 until length) {
//        val nextChar = this[index]
//        prefix.append(nextChar)
//        "Checking existence on [ $prefix ]".print(TRACE)
//
//        if (doesExist(prefix.toString())) {
//            "Prefix exists in dictionary, continue".print(TRACE)
//            // prefix still exists, need the check with the next char
//            continue
//        }
//
//        // the part of the string is a prefix, we return the prefix plus the next char
//        "Found sub prefix [ $prefix ]\n".print(TRACE)
//        return SubPrefix(prefix.dropLast(1).toString(), nextChar)
//    }
//
//    // the whole string is a unique prefix, we return that as is
//    "The whole string is a prefix\n".print(TRACE)
//    return FullPrefix(this)
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
            return substring(0, index)
        }
        "[ $currentPrefix ] is not a prefix".print(TRACE)
    }
    "no prefix found, returning $this".print(TRACE)
    return this
}

fun <T> T.print(logLevel: Log = TRACE, messageCreator: (String) -> String): T {
    if (logLevel >= rootLogLevel) println(messageCreator(this.toString()))
    return this
}

fun <T> T.print(logLevel: Log = TRACE): T {
    if (logLevel >= rootLogLevel) println(this)
    return this
}

