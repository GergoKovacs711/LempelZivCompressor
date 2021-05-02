import Log.*
import com.xenomachina.argparser.ArgParser
import com.xenomachina.argparser.mainBody
import config.ConsoleArgument
import config.Mode
import file.FileAccess
import java.io.File

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

//-f src/main/resources/file/input/input_file_en.txt -c -v
//-f src/main/resources/file/input/compressed_file_en.lz -d -v

//const val inputString = "aababbbabaababbbabbabb"
//const val inputString = "ababcbababaa"
const val inputString = "aacaacabcabaaac"

var rootLogLevel = NONE
val direkRootOutPut = true

enum class Log(val level: Int) {
    TRACE(1),
    DEBUG(2),
    INFO(3),
    NONE(4),
    TEST(5),
}

private fun Int.toRange() = 1..this

var app: Application = Application()

@ExperimentalUnsignedTypes
fun main(args: Array<String>) = mainBody {
    ArgParser(args).parseInto(::ConsoleArgument).run {
        printArguments()
        app.process(filePath, mode, windowSize)
    }
}

fun ConsoleArgument.printArguments() {
    if (verbose) {
        rootLogLevel = TRACE
        filePath.print(INFO) { "File path is $it" }
        verbose.print(INFO) { "Verbose is ${if (verbose) "on" else "off"}" }
        mode.print(INFO) { "Mode is $it" }
        windowSize.print(INFO) { "Window size is $it" }
    }
}

//    val input =
//        "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"
//    val lookUpWindow = SlidingWindow(512)
//    val increments = app.compress(input, lookUpWindow)
//    app.encode(increments)
//return
//}

@ExperimentalUnsignedTypes
class Application {
    fun process(filePath: String, mode: Mode, windowSize: Int) {
        "Starting program".print(INFO)
        when (mode) {
            Mode.COMPRESS -> compressFile(filePath, SlidingWindow(windowSize))
            Mode.DECOMPRESS -> decompress(filePath)
        }
        "Ending program".print(INFO)
    }

    private fun compressFile(filePath: String, lookUpWindow: SlidingWindow) {
        val inputString = FileAccess.readFileAsString(filePath)
        compress(inputString, lookUpWindow)
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
                        if (result.prefix.length == tempString.length) {

                        }
                        val length = result.prefix.length
                        val offset = lookUpWindow.size() - result.index
                        Triplet(offset, length, tempString.getOrNull(length) ?: tempString.last())
                    }
                }
            }

            tripletCounter++.print(INFO)
            triplets.add(currentTriplet)

//            lookUpWindow.toString().print { "lookUpWindow: $it" }
            lookUpWindow.popIfFull(currentTriplet.length + 1)
//            lookUpWindow.toString().print { "lookUpWindow: $it" }
//            currentTriplet.print { "currentTriplet.length $it" }
//            tempString.print { "tempString $it" }

            val (front, back) = tempString.splitAtIndex(currentTriplet.length + 1)
//            front.print { "front $it" }
//            back.print { "back $it" }
            lookUpWindow.push(front)
//            lookUpWindow.toString().print { "lookUpWindow: $it" }
            tempString = back
//            currentTriplet.print { "currentTriplet $it" }
//            tempString.print { "tempString $it" }
//            tempString.print(TRACE) { "remaining input: $it" }
        }
        val tripletsAsByteArray = encode(triplets)
        // TODO: add flag for test so that output doesn't get flooded with test outputfiles...
        FileAccess.writeToFile(tripletsAsByteArray).print(INFO) { "Created file: $it" }
        return triplets
    }

    fun String.splitAtIndex(index: Int) = when {
        index < 0 -> 0
        index > length -> length
        else -> index
    }.let {
        take(it) to substring(it)
    }

    fun String.findLongestExistingPrefixOn(
        lookUpWindow: SlidingWindow,
        lookUp: (String) -> PrefixSearchResult
    ): PrefixSearchResult {
        val prefix = StringBuffer()
        val maxLookUpLength = lookUpWindow.maxSize.coerceAtMost(this.length - 1).print { "maxLookUpLength: [ $it ]" }

        var lastFoundPrefix: PrefixSearchResult = None
        for (index in 0 until maxLookUpLength) {
            val nextChar = this[index]
            prefix.append(nextChar)
            val nextResult =
                lookUp(prefix.toString()).also { "nextResult: [ ${if (it is PrefixFound) it.prefix else "None"} ]".print() }
            when (nextResult) {
                None -> return lastFoundPrefix.also { "no prefix exists for: [ $prefix ] returning: [ ${if (it is PrefixFound) it.prefix else "None"} ]\n".print() }
                is PrefixFound -> lastFoundPrefix = nextResult
            }
            continue
        }
        return lastFoundPrefix.also { "returning last found prefix: [ ${if (it is PrefixFound) it.prefix else "None"} ]\n".print() }
    }

    fun encode(triplets: List<Triplet>): ByteArray {
        val tripletsAsByteArray = ByteArray(triplets.size * 4)
        triplets.forEachIndexed { tripletIndex, triplet ->
            triplet.toByteArray().forEachIndexed { dataIndex, data ->
                tripletsAsByteArray[(tripletIndex * 4) + dataIndex] = data
            }
        }
        return tripletsAsByteArray
    }

    fun decode(filePath: String): List<Triplet> {
        val encodedData = FileAccess.readFromFile(filePath)
        return encodedData.toList().chunked(4).map { chunk ->
            ByteArray(4).apply {
                chunk.forEachIndexed { index, it -> set(index, it) }
            }
        }.map { Triplet(it) }
    }

    fun decompress(filePath: String) {
        val triplets = decode(filePath)
        val text = decompress(triplets)
        FileAccess.writeToFile(text).print(INFO) { "Created file: $it" }
    }

    fun decompress(triplets: List<Triplet>): String {
        val buffer = StringBuffer()
        triplets.forEach triplet@{
            if (it.offset == 0) {
                buffer.append(it.nextCharacter)
                return@triplet
            }
            buffer.apply {
                val startIndex = this.length - it.offset
                append(substring(startIndex, startIndex + it.length))
                append(it.nextCharacter)
            }
        }
        return buffer.toString()
    }
}

fun <T> T.print(logLevel: Log = TRACE, messageCreator: (String) -> String): T {
    if (logLevel >= rootLogLevel) println(messageCreator(this.toString()))
    return this
}

fun <T> T.print(logLevel: Log = TRACE): T {
    if (logLevel >= rootLogLevel) println(this)
    return this
}

