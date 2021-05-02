package config

import com.xenomachina.argparser.ArgParser
import com.xenomachina.argparser.InvalidArgumentException
import com.xenomachina.argparser.default

enum class Mode {
    COMPRESS,
    DECOMPRESS
}

class ConsoleArgument(parser: ArgParser) {
    val verbose by parser.flagging(
        "-v", "--verbose",
        help = "enable verbose mode"
    )

    val filePath by parser.storing(
        "-f", "--file",
        help = "Path of the input file"
    )

    val mode by parser.mapping(
        "-c" to Mode.COMPRESS,
        "-d" to Mode.COMPRESS,
        help = "Mode of operation. -c for compression, -d for decompression"
    )

    val windowSize by parser.storing(
        "-w", "--window",
        help = "Sets the size of the sliding window. Valid values are  [5-2000]"
    ) { toInt() }
        .default(1024)
        .addValidator {
            if (value < 5 || value > 2000) throw InvalidArgumentException("Window size must be between 5 and 2000")
        }



//    val compress by parser.flagging(
//        "-c", "--compress", help = "set mode to compress"
//    )

//    val name by parser.storing(
//        "-N", "--name",
//        help = "name of the user"
//    )

//    val count by parser.storing(
//        "-c", "--count",
//        help = "number of widgets"
//    ) { toInt() }

//    val source by parser.positional(
//        "SOURCE",
//        help = "source filename"
//    )

//    val destination by parser.positional(
//        "DEST",
//        help = "destination filename"
//    )
}