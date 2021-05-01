import Log.*
import java.io.File
import java.time.LocalDate
import java.time.LocalTime
import kotlin.random.Random

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

val rootLogLevel = NONE

enum class Log(val level: Int) {
    TRACE(1),
    DEBUG(2),
    INFO(3),
    NONE(4)
}

private fun Int.toRange() = 1..this

fun main(args: Array<String>) {
    test()
    return
}

fun test(){
    // given
    val charPool : List<Char> = ('a'..'z') + ('A'..'Z') + ('0'..'9')
    val runs = 10
    val inputSize = 1000

    val lookUpWindow = SlidingWindow(50)

    var randomEnding = ""
    repeat(10) { randomEnding += Random.nextInt(10).toString() }

    val inputs = mutableListOf<String>()
    val outputs = mutableListOf<String>()
    // when
    for (index in runs.toRange()) {
        val inputText = inputSize.toRange()
            .map { _ -> Random.nextInt(1, charPool.size) }
            .map { i -> charPool[i] }
            .joinToString("")
            .also { inputs.add(it) }

        println("Testing: ${inputText.toString()}")

        val increments = compress(inputText, lookUpWindow)
        println("Number of increments: ${increments.size}")
        println(" \t " + Runtime.getRuntime().freeMemory() +
                        " \t \t " + Runtime.getRuntime().totalMemory() +
                        " \t \t " + Runtime.getRuntime().maxMemory());
        val decompressedText = decompress(increments).also { outputs.add(it) }

        // then
//            assertEquals(inputText, decompressedText)
        println("Test ${index.toString()} success")
    }

    val date = LocalDate.now()
    val time = LocalTime.now()
    val fileName = "test" + date + "-" + time.hour +  "-" + time.minute + "-" + time.second + "-" + randomEnding
    val file = File(fileName)

    file.apply {
        bufferedWriter().use { out ->
            repeat(outputs.size){
                out.write("Input text [ run: ${it + 1} ] \n")
                out.write(inputs[it])
                out.write("\n\n\n")
                out.write("Decompressed output text [ run: ${it + 1} ] \n")
                out.write(outputs[it])
                out.write("\n\n\n")
            }
        }
    }
}

fun wrapper(){
    "Starting program".print(INFO)
    var tempString = inputString.print(INFO) { "input string: $it" }
        .onEachIndexed { index, _ -> "at index $index is ${inputString[index]}".print(TRACE) }

    val lookUpWindow = SlidingWindow(50)
    val increments = compress(tempString, lookUpWindow)

    lookUpWindow.print(DEBUG) { "dictionary: $it" }
    increments.print()
    "Ending program".print(INFO)
}

fun compress(inputString: String, lookUpWindow: SlidingWindow): List<Increment> {
    var tempString = inputString
    val increments = mutableListOf<Increment>()
    var incrementCounter = 0
    while (tempString.isNotBlank()) {
        val currentIncrement = if (tempString.length == 1) {
            Increment(0, 0, tempString[0])
        } else {
            when (val result = tempString.findLongestExistingPrefixOn(lookUpWindow) { lookUpWindow.find(it) }) {
                None -> {
                    Increment(0, 0, tempString.first())
                }
                is PrefixFound -> {
                    if(result.prefix.length == tempString.length) {

                    }
                    val length = result.prefix.length
                    val offset = lookUpWindow.size() - result.index
                    Increment(offset, length, tempString.getOrNull(length) ?: tempString.last())
                }
            }
        }

        incrementCounter++.print(INFO)

        output(currentIncrement)
        increments.add(currentIncrement)

        lookUpWindow.toString().print { "lookUpWindow: $it" }
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
    increment.print(DEBUG)
}

data class Increment(val offset: Int, val length: Int, val nextCharacter: Char) {
    override fun toString(): String {
        return "Triple($offset, $length, $nextCharacter)"
    }
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

fun decompress(increments : List<Increment>) : String {
    val buffer = StringBuffer()

    increments.forEachIndexed increment@{ index, it ->
        if (it.offset == 0) {
            buffer.append(it.nextCharacter)
            return@increment
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

