import Log.*
import SlidingWindowTest.Companion.generateRandomFileName
import SlidingWindowTest.Companion.writeToRandomTextFile
import org.junit.jupiter.api.Test
import java.io.File
import java.util.*

@ExperimentalUnsignedTypes
class EncodingTest {
    @Test
    fun `Encoding test`() {
        val charPool: List<Char> = ('a'..'z') + ('A'..'Z') + ('0'..'9') + ('~'..'ž')
//        charPool.forEach { "char: [ $it ] ; int: [ ${it.toByte()} ]".print(Log.TEST) }
//        2.0.pow(10.0).print(TEST)
//        2.0.pow(11.0).print(TEST)
//        2.0.pow(12.0).print(TEST)

//        charPool.forEachIndexed { index, it ->
//            val char = it.toChar().toInt().toString(2).padStart(10, '0')
//            val charBits = char.toInt().toString(2).padStart(10, '0')
//            "char: [ ${it.toChar()} ] ; int: [ ${char.toInt()} ] ; char as bits: [ $charBits ]".prt()
//        }

//        (0..1023).forEachIndexed { index, it ->
//            "char: [ ${it.toChar()} ] ; int: [ ${index} ] ; char as bits: [ $ ]".prt()
//        }

        val offset = 2047
        val length = 128
        val char = 'ő'

        val buffer = StringBuffer()

        val offsetBits = offset.toUInt().toString(2).padStart(11, '0').prt()
        val lengthBits = length.toUInt().toString(2).padStart(11, '0').prt()
        val charBits = char.toInt().toString(2).padStart(10, '0').prt() // TODO: ez nem unsigned??

        buffer.apply {
            append(offsetBits)
            append(lengthBits)
            append(charBits)
        }.prt()

        val chunks = buffer.toString().chunked(8)
        chunks.forEach { "size: [ ${it.length}, chunk: [ $it ] ]".prt() }
        val bytes = chunks.map { it.toUInt(2).toUByte() }
        bytes.prt()

        val encodedBytes = ByteArray(4).apply {
            bytes.forEachIndexed { index, it ->
                set(index, it.toByte())
            }
        }
        writeToRandomFile(encodedBytes)
    }

    @Test
    fun readFile(){
        val file = File("test2021-05-01-22-54-42-4361415245.lz")
        val encodedBytes = file.readBytes()

        encodedBytes.forEach { it.prt() }
        val data = encodedBytes.joinToString("") { it.toUByte().toUInt().toString(2).padStart(8, '0') }.prt()

        val offsetString = data.substring(0, 11)
        val lengthString  = data.substring(11, 22)
        val charString  = data.substring(22, 32)

        val offset = offsetString.toByte(2)
        val length = lengthString.toByte(2)
        val char = charString.toByte(2)
    }

    fun ByteArray.byteToInt(): Int {
        var result = 0
        var shift = 0
        for (byte in this) {
            result = result or (byte.toInt() shl shift)
            shift += 8
        }
        return result
    }

    fun writeToRandomFile(
        bytes: ByteArray
    ) {
        val fileName = generateRandomFileName()
        val file = File(fileName)
        file.writeBytes(bytes)
    }
}

fun <T> T.prt(): T {
    println(this)
    return this
}