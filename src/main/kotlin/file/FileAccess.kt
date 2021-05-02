package file

import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.random.Random

object FileAccess {
    private const val RESOURCE_PATH = "src/main/resources/file"
    private fun String.output() = "$RESOURCE_PATH/output/$this"
    private fun String.input() = "$RESOURCE_PATH/input/$this"

    private val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss")

    fun writeToFile(bytes: ByteArray): String {
        val fileName = randomTimestampedFileName()
        val file = File(fileName.output())
        file.writeBytes(bytes)
        return fileName
    }

    fun readFromFile(filePath: String): ByteArray {
        return File(filePath).readBytes()
    }

    fun randomTimestampedFileName(): String {
        var randomEnding = ""
        repeat(10) { randomEnding += Random.nextInt(10).toString() }
        return "test-file" + formatter.format(LocalDateTime.now()) + randomEnding + ".lz"
    }
}