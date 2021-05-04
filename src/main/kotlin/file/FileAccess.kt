package file

import config.FilePathOption
import config.NotProvided
import config.Provided
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.random.Random

object FileAccess {
    enum class DirectoryOutput {
        DIRECT,
        PATH,
        TEST
    }

    var directoryOutput : DirectoryOutput = DirectoryOutput.DIRECT

    private const val RESOURCE_PATH = "src/main/resources/file"
    private const val OUTPUT_PATH = "${RESOURCE_PATH}/output"
    private const val INPUT_PATH = "${RESOURCE_PATH}/input"
    private const val TEST_PATH = "${RESOURCE_PATH}/temp"

    private fun String.output() : String {
        return when (directoryOutput) {
            DirectoryOutput.DIRECT -> this
            DirectoryOutput.PATH -> "$OUTPUT_PATH/$this"
            DirectoryOutput.TEST -> "$TEST_PATH/$this"
        }
    }

    private fun String.input() : String {
        return when (directoryOutput) {
            DirectoryOutput.DIRECT -> this
            DirectoryOutput.PATH -> "$INPUT_PATH/$this"
            DirectoryOutput.TEST -> "$TEST_PATH/$this"
        }
    }

    private fun String.randomEnd(suffix: String) : String {
        return this + "-" + randomTimestamp() + "." + suffix
    }

    private const val compressionFile = "input_file_en.txt"
    private const val decompressionFile = "output_file_en.lz"

    private val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss")

    fun writeToFile(bytes: ByteArray, filePath: FilePathOption  = NotProvided): String {
        val file = when (filePath) {
            NotProvided -> File("compression-test".randomEnd("lz").output())
            is Provided -> File(filePath.path.output())
        }.also { it.writeBytes(bytes) }
        return file.name
    }

    fun writeToFile(data: String, filePath: FilePathOption = NotProvided): String {
        val file = when (filePath) {
            NotProvided -> File("decompression-test".randomEnd("txt").output())
            is Provided -> File(filePath.path.output())
        }.also { it.writeText(data) }
        return file.name
    }

    fun readFromFile(filePath: FilePathOption): ByteArray {
        return when (filePath) {
            NotProvided -> File(decompressionFile.input())
            is Provided -> File(filePath.path.input())
        }.readBytes()
    }

    fun readFileAsString(filePath: FilePathOption): String {
        return when (filePath) {
            NotProvided -> File(compressionFile.input())
            is Provided -> File(filePath.path.input())
        }.readText()
    }


    fun randomTimestampedFileName(fileSuffix: String): String {
        return "test-file" + randomTimestamp() + fileSuffix
    }

    private fun randomTimestamp(): String {
        var randomEnding = ""
        repeat(10) { randomEnding += Random.nextInt(10).toString() }
        return formatter.format(LocalDateTime.now()) + randomEnding
    }
}