import SlidingWindowTest.Companion.generateRandomFileName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.io.File
import kotlin.random.Random
import kotlin.test.assertEquals

@ExperimentalUnsignedTypes
class EncodingTest {
    @Test
    fun `Decode one triplet from file`() {
        // given
        val file = File("src/test/resources/file/decode_001.lz")
        val encodedBytes = file.readBytes()

        // when
        encodedBytes.forEach { it.prt() }
        val data = encodedBytes.joinToString("") { it.toUByte().toUInt().toString(2).padStart(8, '0') }.prt()

        val offsetString = data.substring(0, 11)
        val lengthString = data.substring(11, 22)
        val charString = data.substring(22, 32)

        val offset = offsetString.padStart(16, '0').toUInt(2).prt()
        val length = lengthString.padStart(16, '0').toUInt(2).prt()
        val char = charString.padStart(16, '0').toInt(2).toChar().prt()

        // then
        assertEquals(2047u, offset)
        assertEquals(128u, length)
        assertEquals('ő', char)
    }

    @Test
    fun `Encode then decode one triplet`() {
        // given
        val offset = 2047
        val length = 128
        val char = 'ő'
        val originalTriplet = Triplet(offset, length, char)

        // when
        val encodedTriplet = originalTriplet.toByteArray()
        val decodedTriplet = Triplet(encodedTriplet)

        // then
        assertEquals(originalTriplet, decodedTriplet)
    }

    @Test
    fun `Encode then decode triplets`() {
        // given
        val originalTriplets = mutableListOf<Triplet>().apply {
            add(Triplet(0, 0, 'a'))
            add(Triplet(1, 1, 'c'))
            add(Triplet(3, 3, 'a'))
            add(Triplet(0, 0, 'b'))
            add(Triplet(3, 3, 'a'))
            add(Triplet(6, 1, 'a'))
            add(Triplet(0, 0, 'c'))
        }

        // when
        val encodedTriplets = originalTriplets.map { it.toByteArray() }
        val decodedTriplets = encodedTriplets.map { Triplet(it) }

        // then
        assertEquals(originalTriplets, decodedTriplets)
    }

    @Test
    fun `Testing value limits for encoding`() {
        // given
        val validLowestTriplet = Triplet(0, 0, ' ')
        val validHighestTriplet = Triplet(2047, 2047, 'Ͽ')
        val originalTriplets = listOf(validLowestTriplet, validHighestTriplet)

        // when
        val encodedTriplets = originalTriplets.map { it.toByteArray() }
        val decodedTriplets = encodedTriplets.map { Triplet(it) }

        // then
        assertEquals(validLowestTriplet, decodedTriplets[0])
        assertEquals(validHighestTriplet, decodedTriplets[1])
    }

    @Test
    fun `Randomised triplet encoding-decoding stress test`() {
        // given
        val batches = 0..5
        val tripletsPerBatch = 0..1000
        val validOffsetAndLengthRange = 2048
        val validCharRange = 1024

        // when
        batches.forEach { _ ->
            tripletsPerBatch.forEach { _ ->
                val offset = Random.nextInt(validOffsetAndLengthRange)
                val length = Random.nextInt(validOffsetAndLengthRange)
                val char = Random.nextInt(validCharRange).toChar()
                val randomValidTriplet = Triplet(offset, length, char)
                val newTriplet = Triplet(randomValidTriplet.toByteArray())
                // then
                assertEquals(randomValidTriplet, newTriplet)
            }
        }
    }

    @Test
    fun `Test Triplet creation with too high, out of bound parameters`() {
        // given
        val tooHighOffset: () -> Triplet = { Triplet(2048, 0, ' ') }
        val tooHighLength: () -> Triplet = { Triplet(0, 2048, ' ') }
        val tooHighChar: () -> Triplet = { Triplet(0, 0, 'Ѐ') }

        // when
        listOf(tooHighOffset, tooHighLength, tooHighChar).forEach {
            // then
            assertThrows<IllegalArgumentException> { it.invoke() }
        }
    }

    private fun writeToRandomFile(
        bytes: ByteArray
    ): String {
        val fileName = generateRandomFileName()
        val file = File("src/test/resources/$fileName")
        file.writeBytes(bytes)
        return fileName
    }
}

fun <T> T.prt(): T {
    println(this)
    return this
}