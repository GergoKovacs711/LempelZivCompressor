package file

import file.FileAccess.randomTimestampedFileName
import java.io.File

const val TEST_PATH = "src/test/resources/file/"
const val TEMP = "temp"
const val STATIC = "static"
const val OUTPUT = "output"

private fun String.temp() = "$TEST_PATH/$TEMP/$this"
private fun String.static() = "$TEST_PATH/$STATIC/$this"
private fun String.output() = "$TEST_PATH/$OUTPUT/$this"

fun writeToRandomTestFile(bytes: ByteArray): String {
    val fileName = randomTimestampedFileName()
    val file = File(fileName.temp())
    file.writeBytes(bytes)
    return fileName
}

fun readTempTestFile(fileName: String): ByteArray {
    return File(fileName.temp()).readBytes()
}

fun readStaticTestFile(fileName: String): ByteArray {
    return File(fileName.static()).readBytes()
}

fun removeTestFile(path: String) {
    File(path).delete()
}

fun writeToRandomOutPutFile(inputs: MutableList<String>, outputs: MutableList<String>, error: String? = null) {
    val fileName = randomTimestampedFileName()
    val file = File(fileName.output())
    file.apply {
        bufferedWriter().use { out ->
            repeat(outputs.size) {
                out.write("Input text [ run: ${it + 1} ] \n")
                out.write(inputs[it])
                out.write("\n\n\n")
                out.write("Decompressed output text [ run: ${it + 1} ] \n")
                out.write(outputs[it])
                out.write("\n\n\n")
                error?.let {
                    out.write("An error occurred during test execution:\n")
                    out.write(error)
                }
            }
        }
    }
}

fun removeAllTestFiles(){
    // TODO: fix this, it doesn't seem to work yet...
//    File(TEST_PATH + TEMP).list()?.forEach {
//        File(it).delete()
//    }
}
