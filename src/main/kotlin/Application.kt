import com.xenomachina.argparser.ArgParser
import com.xenomachina.argparser.mainBody
import config.*
import file.FileAccess
import file.FileAccess.DirectoryOutput.DIRECT
import logging.LogLevel
import logging.LogLevel.DEBUG
import logging.LogLevel.INFO
import logging.print
import logging.printO
import logging.rootLogLevel
import model.*

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

@ExperimentalUnsignedTypes
fun main(args: Array<String>) = mainBody {
    ArgParser(args).parseInto(::ConsoleArgument).run {
        printArguments()
        Application(
            filePath = parseFilePath(),
            logLevel = parseVerbosity(),
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
        "Program started.\n".print(INFO)
        when (mode) {
            Mode.COMPRESS -> compressFile(filePath, SlidingWindow(windowSize))
            Mode.DECOMPRESS -> decompress(filePath)
        }
        "Ending program".print(INFO)
    }

    private fun compressFile(filePath: FilePathOption, lookUpWindow: SlidingWindow) {
        when(filePath) {
            NotProvided -> "No filepath provided. Compressing input_file_en.txt\n".print(INFO)
            is Provided -> "Compressing ${filePath.path}\n".print(INFO)
        }
        val inputString = FileAccess.readFileAsString(filePath)
        val tripletsAsByteArray = compress(inputString, lookUpWindow)
            .printO(DEBUG) { "[ ${it.size} ] triplets created" }
            .printO { it.joinToString(", ") { triplet -> triplet.conciseString() } }
            .let { encode(it) }
            .print(DEBUG) { "Triplets encoded" }
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
                    is Prefix -> {
                        val length = result.prefix.length
                        val offset = lookUpWindow.size() - result.index
                        Triplet(offset, length, inputBuffer.getOrNull(length) ?: inputBuffer.last())
                    }
                }
            }
            currentTriplet.print(DEBUG) { "Created triplet [ $it ]\n" }
            tripletCounter++.print { "model.Triplet count [ $it ]\n" }
            triplets.add(currentTriplet)

            lookUpWindow.toString().print { "Lookup window before popping last elements $it" }
            val segmentLength = (currentTriplet.length + 1).print { "Removing last [ $it ] element from lookup window, if it is already full" }
            lookUpWindow.popIfFull(segmentLength)
            lookUpWindow.toString().print { "Lookup window after popping last elements $it" }

            inputBuffer.print { "Input buffer before splitting [ $it ]" }
            segmentLength.print { "Splitting input buffer at [ $segmentLength ] index" }
            val (front, back) = inputBuffer.splitAtIndex(segmentLength)
            front.print { "Front [ $it ]" }
            back.print { "Back [ $it ]" }

            lookUpWindow.push(front).print { "Pushing front into lookup window" }
            lookUpWindow.toString().print { "Lookup window $it" }

            inputBuffer = back
            inputBuffer.print { "Remaining input buffer [ $it ]\n" }
        }
        return triplets
    }

    private fun String.findLongestExistingPrefixOn(
        lookUpWindow: SlidingWindow,
        lookUp: (String) -> PrefixSearchResult
    ): PrefixSearchResult {
        val prefix = StringBuffer()
        val maxLookUpLength = lookUpWindow.maxSize.coerceAtMost(this.length - 1).print(DEBUG) { "Max lookup length: [ $it ]" }

        var lastFoundPrefix: PrefixSearchResult = None
        for (index in 0 until maxLookUpLength) {
            this[index].let { prefix.append(it) }
            when (val nextResult = lookUp(prefix.toString()).printO { "Next lookup result: [ $it ]" }) {
                None -> return lastFoundPrefix.printO(DEBUG) { "No prefix exists for [ $prefix ] returning the last prefix [ $it ]\n" }
                is Prefix -> lastFoundPrefix = nextResult.printO(DEBUG) { "model.Prefix found. Storing [ $it ] as last prefix, continue lookup" }
            }
            continue
        }
        return lastFoundPrefix.printO(DEBUG) { "Run out of lookup length, returning last prefix [ $it ]\n" }
    }

    private fun encode(triplets: List<Triplet>): ByteArray {
        print(DEBUG) { "Encoding triplets on 4 bytes. 11 bits for offset, 11 bits for length as unsigned integers, and 8 bits for the next character." }
        val tripletsAsByteArray = ByteArray(triplets.size * 4)
        triplets.forEachIndexed { tripletIndex, triplet ->
            triplet.toByteArray().forEachIndexed { dataIndex, data ->
                tripletsAsByteArray[(tripletIndex * 4) + dataIndex] = data
            }
        }
        return tripletsAsByteArray
    }

    private fun decode(filePath: FilePathOption): List<Triplet> {
        val encodedData = FileAccess.readFromFile(filePath)
        return encodedData.toList().chunked(4).map { chunk ->
            ByteArray(4).apply {
                chunk.forEachIndexed { index, it -> set(index, it) }
            }
        }.map { Triplet(it) }
    }

    private fun decompress(filePath: FilePathOption) {
        when(filePath) {
            NotProvided -> "No filepath provided. Decompressing output_file_en.lz\n".print(INFO)
            is Provided -> "Decompressing ${filePath.path}\n".print(INFO)
        }
        val triplets = decode(filePath).printO(DEBUG) { "Decoded [ ${it.size} ] triplets" }
        val text = decompress(triplets).print(DEBUG) { "Reconstructed text from triplets" }
        FileAccess.writeToFile(text).print(INFO) { "Created file: $it" }
    }

    fun decompress(triplets: List<Triplet>): String {
        val buffer = StringBuffer()
        print(DEBUG) { "Reconstructing triplets\n" }
        triplets.forEach triplet@{ triplet ->
            printO(DEBUG) { "model.Triplet [ $triplet ]" }
            if (triplet.offset == 0) {
                print(DEBUG) { "model.Triplet has a zero offset, adding it as a single character\n" }
                buffer.append(triplet.nextCharacter)
                return@triplet
            }
            buffer.apply {
                print(DEBUG) { "model.Triplet has an offset. Copying segment." }
                val startIndex = (this.length - triplet.offset)
                substring(startIndex, startIndex + triplet.length)
                    .print { "Getting substring between [ $startIndex ] - [ ${startIndex + triplet.length} ]" }
                    .print(DEBUG) { "Segment [ $it ]"}
                    .print(DEBUG) { "Adding next character [ ${triplet.nextCharacter} ] to the end" }
                    .let { it + triplet.nextCharacter }
                    .print { "The result concatenated [ $it ]" }
                    .print(DEBUG) { "Appending result to the buffer\n" }
                    .also { append(it) }
            }
        }
        return buffer.toString().print { "The decompressed text [ $it ]\n" }
    }

    companion object {
        private fun String.splitAtIndex(index: Int) = when {
            index < 0 -> 0
            index > length -> length
            else -> index
        }.let {
            take(it) to substring(it)
        }
    }
}
