package config

import com.xenomachina.argparser.ArgParser
import com.xenomachina.argparser.InvalidArgumentException
import com.xenomachina.argparser.default
import util.LogLevel.INFO
import util.LogLevel.TRACE
import util.print
import util.rootLogLevel

enum class Mode {
    COMPRESS,
    DECOMPRESS
}

sealed class FilePathOption
class Provided(val path: String) : FilePathOption()
object NotProvided: FilePathOption()

class ConsoleArgument(parser: ArgParser) {
    val verbose by parser.flagging(
        "-v", "--verbose",
        help = "enable verbose mode"
    )

    val filePath by parser.storing(
        "-f", "--file",
        help = "Path of the input file"
    ).default<String?>(null)

    val mode by parser.mapping(
        "-c" to Mode.COMPRESS,
        "-d" to Mode.DECOMPRESS,
        help = "Mode of operation. -c for compression, -d for decompression"
    )

    val windowSize by parser.storing(
        "-w", "--window",
        help = "Sets the size of the sliding window. Valid values are  [5-2000]"
    ) { toInt() }
        .default(2000)
        .addValidator {
            if (value < 5 || value > 2000) throw InvalidArgumentException("Window size must be between 5 and 2000")
        }

    fun parseFilePath() : FilePathOption {
        return if(filePath.isNullOrBlank()) NotProvided else Provided(filePath!!)
    }

    fun printArguments() {
        if (verbose) {
            rootLogLevel = TRACE
            filePath.print(INFO) { "File path is $it" }
            verbose.print(INFO) { "Verbose is ${if (verbose) "on" else "off"}" }
            mode.print(INFO) { "Mode is $it" }
            windowSize.print(INFO) { "Window size is $it" }
            val path = parseFilePath()
        }
    }
}