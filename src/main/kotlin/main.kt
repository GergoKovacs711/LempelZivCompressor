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

/**
 * Max 2 GB input file
 * Max UTF 1023 characters
 * Max 2047 sliding window
 */

//const val inputString = "aababbbabaababbbabbabb"
//const val inputString = "ababcbababaa"
const val inputString = "aacaacabcabaaac"

val rootLogLevel = NONE

enum class Log(val level: Int) {
    TRACE(1),
    DEBUG(2),
    INFO(3),
    NONE(4),
    TEST(5),
}

private fun Int.toRange() = 1..this

fun main(args: Array<String>) {
    wrapper()
    return
}

fun wrapper(){
    "Starting program".print(INFO)
    var tempString = inputString.print(INFO) { "input string: $it" }
        .onEachIndexed { index, _ -> "at index $index is ${inputString[index]}".print(TRACE) }

    val lookUpWindow = SlidingWindow(50)
    val triplets = compress(tempString, lookUpWindow)

    lookUpWindow.print(DEBUG) { "dictionary: $it" }
    triplets.print()
    "Ending program".print(INFO)
}

fun compress(inputString: String, lookUpWindow: SlidingWindow): List<Triplet> {
    var tempString = inputString
    val triplets = mutableListOf<Triplet>()
    var tripletCounter = 0
    while (tempString.isNotBlank()) {
        val currentTriplet = if (tempString.length == 1) {
            Triplet(0, 0, tempString[0])
        } else {
            when (val result = tempString.findLongestExistingPrefixOn(lookUpWindow) { lookUpWindow.find(it) }) {
                None -> {
                    Triplet(0, 0, tempString.first())
                }
                is PrefixFound -> {
                    if(result.prefix.length == tempString.length) {

                    }
                    val length = result.prefix.length
                    val offset = lookUpWindow.size() - result.index
                    Triplet(offset, length, tempString.getOrNull(length) ?: tempString.last())
                }
            }
        }

        tripletCounter++.print(INFO)

        output(currentTriplet)
        triplets.add(currentTriplet)

        lookUpWindow.toString().print { "lookUpWindow: $it" }
        lookUpWindow.popIfFull(currentTriplet.length + 1)
        lookUpWindow.toString().print { "lookUpWindow: $it" }
        currentTriplet.print{"currentTriplet.length $it"}
        tempString.print{"tempString $it"}

        val (front, back) = tempString.splitAtIndex(currentTriplet.length + 1)
        front.print { "front $it"}
        back.print { "back $it"}
        lookUpWindow.push(front)
        lookUpWindow.toString().print { "lookUpWindow: $it" }
        tempString = back
        currentTriplet.print{"currentTriplet $it"}
        tempString.print{ "tempString $it"}
        tempString.print(TRACE) { "remaining input: $it" }
    }
    return triplets
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

fun output(triplet: Triplet) {
    triplet.print(DEBUG)
}



fun String.findLongestExistingPrefixOn(lookUpWindow: SlidingWindow, lookUp: (String) -> PrefixSearchResult): PrefixSearchResult {
    val prefix = StringBuffer()
    val maxLookUpLength = lookUpWindow.maxSize.coerceAtMost(this.length - 1).print { "maxLookUpLength: [ $it ]"}

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

fun decompress(triplets: List<Triplet>) : String {
    val buffer = StringBuffer()

    triplets.forEachIndexed triplet@{ index, it ->
        if (it.offset == 0) {
            buffer.append(it.nextCharacter)
            return@triplet
        }
        buffer.apply {
            val startIndex = this.length - it.offset
            try{
                append(substring(startIndex, startIndex + it.length))
            } catch (e: StringIndexOutOfBoundsException){
                throw e
            }

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

