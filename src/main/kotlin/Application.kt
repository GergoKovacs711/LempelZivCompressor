import com.xenomachina.argparser.ArgParser
import com.xenomachina.argparser.mainBody
import config.ConsoleArgument
import config.FilePathOption
import config.Mode
import file.FileAccess
import file.FileAccess.DirectoryOutput.*
import util.LogLevel
import util.LogLevel.*
import util.print
import util.printO
import util.rootLogLevel

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

@ExperimentalUnsignedTypes
fun main(args: Array<String>) = mainBody {
    ArgParser(args).parseInto(::ConsoleArgument).run {
        printArguments()
        val app = Application(
            filePath = parseFilePath(),
            logLevel = INFO,
            mode = mode,
            windowSize = windowSize,
            directRootOutPut = DIRECT
        ).process()
    }
}

@ExperimentalUnsignedTypes
class Application(
    val filePath: FilePathOption,
    val logLevel: LogLevel,
    val mode: Mode,
    val windowSize: Int,
    val directRootOutPut: FileAccess.DirectoryOutput
) {
    init {
        FileAccess.directoryOutput = directRootOutPut
        rootLogLevel = logLevel
    }

    fun process() {
        "Starting program".print(INFO)
        when (mode) {
            Mode.COMPRESS -> compressFile(filePath, SlidingWindow(windowSize))
            Mode.DECOMPRESS -> decompress(filePath)
        }
        "Ending program".print(INFO)
    }

    private fun compressFile(filePath: FilePathOption, lookUpWindow: SlidingWindow) {
        val inputString = FileAccess.readFileAsString(filePath)
        val tripletsAsByteArray = compress(inputString, lookUpWindow)
            .print(DEBUG) { "Created triplets [ $it ]" }
            .let { encode(it) }
            .print(DEBUG) { "Encoding triplets" }
        FileAccess.writeToFile(tripletsAsByteArray).print(INFO) { "Created file [ $it ]" }
    }

    fun compress(inputString: String, lookUpWindow: SlidingWindow): List<Triplet> {
        var inputBuffer = inputString
        val triplets = mutableListOf<Triplet>()
        var tripletCounter = 0
        while (inputBuffer.isNotBlank()) {
            val currentTriplet = if (inputBuffer.length == 1) {
                Triplet(0, 0, inputBuffer[0])
            } else {
                when (val result = inputBuffer.findLongestExistingPrefixOn(lookUpWindow) { lookUpWindow.find(it) }) {
                    None -> {
                        Triplet(0, 0, inputBuffer.first())
                    }
                    is PrefixFound -> {
                        val length = result.prefix.length
                        val offset = lookUpWindow.size() - result.index
                        Triplet(offset, length, inputBuffer.getOrNull(length) ?: inputBuffer.last())
                    }
                }
            }
            currentTriplet.print(DEBUG) { "Created triplet [ $it ]" }
            tripletCounter++.print(DEBUG) { "Triplet count [ $it ]" }
            triplets.add(currentTriplet)

            lookUpWindow.toString().print { "Lookup window before popping last elements [ $it ]" }
            lookUpWindow.popIfFull(currentTriplet.length + 1)
            lookUpWindow.toString().print { "Lookup window after popping last elements [$it ]" }

            inputBuffer.print { "Input buffer before splitting [ $it ]" }
            val (front, back) = inputBuffer.splitAtIndex(currentTriplet.length + 1)
            front.print { "Front [ $it ]" }
            back.print { "Back [ $it ]" }

            lookUpWindow.push(front).print { "Pushing front into lookup window" }
            lookUpWindow.toString().print { "Lookup window [ $it ]" }

            inputBuffer = back
            inputBuffer.print { "Remaining input buffer [ $it ]" }
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
            val nextResult = lookUp(prefix.toString()).printO { "nextResult: [ $it ]" }
            when (nextResult) {
                None -> return lastFoundPrefix.also { "no prefix exists for: [ $prefix ] returning: [ $it ]\n".print() }
                is PrefixFound -> lastFoundPrefix = nextResult
            }
            continue
        }
        return lastFoundPrefix.also { "returning last found prefix: [ $it ]\n".print() }
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

    fun decode(filePath: FilePathOption): List<Triplet> {
        val encodedData = FileAccess.readFromFile(filePath)
        return encodedData.toList().chunked(4).map { chunk ->
            ByteArray(4).apply {
                chunk.forEachIndexed { index, it -> set(index, it) }
            }
        }.map { Triplet(it) }
    }

    fun decompress(filePath: FilePathOption) {
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
