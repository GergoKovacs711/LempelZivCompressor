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

    private const val compressionFile = "input_file_en.txt"
    private const val decompressionFile = "compressed_file_en.lz"

    private val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss")

    fun writeToFile(bytes: ByteArray, filePath: FilePathOption  = NotProvided): String {
        when (filePath) {
            NotProvided -> File(compressionFile.output())
            is Provided -> File(filePath.path.output())
        }.writeBytes(bytes)
        return "" // TODO: fix for tests
    }

    fun writeToFile(data: String, filePath: FilePathOption = NotProvided): String {
        when (filePath) {
            NotProvided -> File(decompressionFile.output())
            is Provided -> File(filePath.path.output())
        }.writeText(data)
        return "" // TODO: fix for tests
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
        var randomEnding = ""
        repeat(10) { randomEnding += Random.nextInt(10).toString() }
        return "test-file" + formatter.format(LocalDateTime.now()) + randomEnding + "." + fileSuffix
    }
}