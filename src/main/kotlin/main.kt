import Log.*

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
val lookUpWindow = SlidingWindow(6)
val rootLogLevel = INFO

sealed class PrefixSearchResult
data class PrefixFound(val index: Int, val prefix: String) : PrefixSearchResult()
object None : PrefixSearchResult()

class SlidingWindow(val maxSize: Int, private val window: ArrayDeque<Char> = ArrayDeque(maxSize)) {
    fun find(sequenceToLookUp: String): PrefixSearchResult {
        val windowString = windowAsString()
        return when (windowString.contains(sequenceToLookUp)) {
            false -> None
            true -> {
                val index = windowString.indexOf(sequenceToLookUp)
                PrefixFound(index, sequenceToLookUp)
            }
        }
    }

    fun popIfFull(amount: Int = 1): SlidingWindow {
        if(window.size < maxSize) {
            return this
        }
        repeat(amount) {
            window.removeFirstOrNull().let {
                if (it == null) {
                    return@popIfFull this
                }
            }
        }
        return this
    }

    fun push(chars: CharSequence): SlidingWindow {
        check(chars.length <= maxSize) { "The size of [ $chars ] exceeds max window size [ $maxSize ]!"}
        window.addAll(chars.toList())
        return this
    }
    
    fun size () : Int {
        return window.size
    }

    private fun windowAsString(): String {
        return buildString { window.forEach { append(it) } }
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
    var tempString = inputString.print(INFO) { "input string: $it" }
        .onEachIndexed { index, _ -> "at index $index is ${inputString[index]}".print(TRACE) }

    val increments = compress(tempString, lookUpWindow)

    lookUpWindow.print(DEBUG) { "dictionary: $it" }
    increments.print()
    "Ending program".print(INFO)
}

fun compress(inputString: String, lookUpWindow: SlidingWindow): List<Increment> {
    var tempString = inputString
    val increments = mutableListOf<Increment>()
    while (tempString.isNotBlank()) {
        val currentIncrement = if (tempString.length == 1) {
            Increment(0, 0, tempString[0])
        } else {
            when (val result = tempString.findLongestExistingPrefixOn { lookUpWindow.find(it) }) {
                None -> {
                    Increment(0, 0, tempString.first())
                }
                is PrefixFound -> {
                    val length = result.prefix.length
                    val offset = lookUpWindow.size() - result.index
                    Increment(offset, length, tempString.getOrNull(length) ?: tempString.last())
                }
            }
        }

        output(currentIncrement).print(DEBUG)
        increments.add(currentIncrement)

        lookUpWindow.toString().print { "lookUpWindow: ${it}" }
        lookUpWindow.popIfFull(currentIncrement.length + 1)
        lookUpWindow.toString().print { "lookUpWindow: $it" }
        currentIncrement.print{"currentIncrement.length $it"}
        tempString.print{"tempString $it"}

        val (front, back) = tempString.splitAtIndex(currentIncrement.length + 1)
        front.print { "front $it"}
        back.print { "back $it"}
        lookUpWindow.push(front)
        lookUpWindow.toString().print { "lookUpWindow: $it" }
        tempString = back
        currentIncrement.print{"currentIncrement $it"}
        tempString.print{ "tempString $it"}
        tempString.print(TRACE) { "remaining input: $it" }
    }
    return increments
}

fun String.splitAtIndex(index: Int) = when {
    index < 0 -> 0
    index > length -> length
    else -> index
}.let {
    take(it) to substring(it)
}

fun String.nextCharAfter(lastIndex: Int): Char {
    if (lastIndex >= this.length) {
        return this.last()
    }
    return this[lastIndex + 1]
}

fun output(increment: Increment) {
//    println(increment)
}

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


fun String.findLongestExistingPrefixOn(lookUp: (String) -> PrefixSearchResult): PrefixSearchResult {
    val prefix = StringBuffer()
    val maxLookUpLength = lookUpWindow.maxSize.coerceAtMost(this.length).print { "maxLookUpLength: [ $it ]"}

    var lastFoundPrefix : PrefixSearchResult = None
    for (index in 0 until maxLookUpLength) {
        val nextChar = this[index]
        prefix.append(nextChar)
        val nextResult = lookUp(prefix.toString()).also {  "nextResult: [ ${if(it is PrefixFound) it.prefix else "None"} ]".print() }
        when(nextResult) {
            None -> return lastFoundPrefix.also { "no prefix exists for: [ $prefix ] returning: [ ${if(it is PrefixFound) it.prefix else "None"} ]\n".print()}
            is PrefixFound -> lastFoundPrefix = nextResult
        }
        continue
    }
    return lastFoundPrefix.also { "returning last found prefix: [ ${if(it is PrefixFound) it.prefix else "None"} ]\n".print()}
}


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

//
//// TODO("inline")
//fun String.prefixMatches(predicate: (String) -> Boolean): String {
//    "\nfinding prefix for [ $this ]".print(TRACE)
//    val buffer = StringBuilder()
//    for (index in 0 until length) {
//        "appending [ ${this[index]} ] to buffer".print(TRACE)
//        buffer.append(this[index])
//
//        val currentPrefix = buffer.toString()
//        "looking [ $currentPrefix ] up in dictionary".print(TRACE)
//        if (predicate.invoke(currentPrefix)) {
//            "[ $currentPrefix ] is a new prefix \n".print(TRACE)
//            return substring(0, index)
//        }
//        "[ $currentPrefix ] is not a prefix".print(TRACE)
//    }
//    "no prefix found, returning $this".print(TRACE)
//    return this
//}

fun decompress(increments : List<Increment>) : String {
    val buffer = StringBuffer()

    increments.forEach increment@{
        if (it.offset == 0) {
            buffer.append(it.nextCharacter)
            return@increment
        }
        buffer.apply {
            val startIndex = this.length - it.offset
            append(substring(startIndex, startIndex + it.length))
            append(it.nextCharacter)
        }
    }
    return buffer.toString()
}

fun <T> T.print(logLevel: Log = TRACE, messageCreator: (String) -> String): T {
    if (logLevel >= rootLogLevel) println(messageCreator(this.toString()))
    return this
}

fun <T> T.print(logLevel: Log = TRACE): T {
    if (logLevel >= rootLogLevel) println(this)
    return this
}

