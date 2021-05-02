package file

import file.FileAccess.output
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.random.Random

const val direktRootOutput = true

object FileAccess {
    private const val RESOURCE_PATH = "src/main/resources/file"
    private fun String.output() = if(direktRootOutput) this else "$RESOURCE_PATH/output/$this"
    private fun String.input() = if(direktRootOutput) this else "$RESOURCE_PATH/input/$this"

    private val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss")

    fun writeToFile(bytes: ByteArray): String {
        val fileName = randomTimestampedFileName("lz")
        val file = File(fileName.output())
        file.writeBytes(bytes)
        return fileName
    }

    fun writeToFile(data: String, filePath: String = ""): String {
        val fileName = randomTimestampedFileName("txt")
        val file = File(fileName.output())
        file.writeText(data)
        return fileName
    }

    fun readFromFile(filePath: String): ByteArray {
        return File(filePath).readBytes()
    }

    fun readFileAsString(filePath: String): String {
        return File(filePath).readText()
    }

    fun randomTimestampedFileName(fileSuffix: String): String {
        var randomEnding = ""
        repeat(10) { randomEnding += Random.nextInt(10).toString() }
        return "test-file" + formatter.format(LocalDateTime.now()) + randomEnding + "." + fileSuffix
    }
}