package config

import com.xenomachina.argparser.ArgParser
import com.xenomachina.argparser.InvalidArgumentException
import com.xenomachina.argparser.default
import logging.LogLevel
import logging.LogLevel.*
import logging.print

enum class Mode {
    COMPRESS,
    DECOMPRESS
}

enum class Verbosity {
    TRACE,
    VERBOSE,
    INFO,
    NONE
}

sealed class FilePathOption
class Provided(val path: String) : FilePathOption()
object NotProvided : FilePathOption()

class ConsoleArgument(parser: ArgParser) {
    val verbosity by parser.mapping(
        "-t" to Verbosity.TRACE,
        "-v" to Verbosity.VERBOSE,
        "-n" to Verbosity.NONE,
        help = "Modifies logging verbosity: -n turns off logging, -v for VERBOSE (provides additional logging), -t for TRACE (most detailed level, not recommended for big files). When no flag is provided, the default INFO level is active."
    ).default { Verbosity.INFO }

    val filePath by parser.storing(
        "-f", "--file",
        help = "Path of the input file. When no file is provided. The default test files are used to generated the output file."
    ).default<String?>(null)

    val mode by parser.mapping(
        "-c" to Mode.COMPRESS,
        "-d" to Mode.DECOMPRESS,
        help = "Mode of operation: -c for compression, -d for decompression"
    )

    val windowSize by parser.storing(
        "-w", "--window",
        help = "Sets the size of the sliding window. Valid values are  [5-2000]"
    ) { toInt() }
        .default(2000)
        .addValidator {
            if (value < 5 || value > 2000) throw InvalidArgumentException("Window size must be between 5 and 2000")
        }

    fun parseFilePath(): FilePathOption {
        return if (filePath.isNullOrBlank()) NotProvided else Provided(filePath!!)
    }

    fun parseVerbosity(): LogLevel {
        return when (verbosity) {
            Verbosity.TRACE -> TRACE
            Verbosity.VERBOSE -> DEBUG
            Verbosity.INFO -> INFO
            Verbosity.NONE -> NONE
        }
    }

    fun printArguments() {
        filePath.print(DEBUG) { "File path is $it" }
        verbosity.print(DEBUG) { "Verbosity is $verbosity" }
        mode.print(DEBUG) { "Mode is $it" }
        windowSize.print(DEBUG) { "Window size is $it" }
    }
}