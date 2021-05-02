import file.*
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertThrows
import kotlin.random.Random
import kotlin.test.assertEquals

@ExperimentalUnsignedTypes
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class EncodingTest {
    @Test
    fun `Decode one triplet from file`() {
        // given
        val fileName = "decode_001.lz"
        val encodedBytes = readStaticTestFile(fileName)

        // when
        val data = encodedBytes.joinToString("") { it.toUByte().toUInt().toString(2).padStart(8, '0') }

        val offsetString = data.substring(0, 11)
        val lengthString = data.substring(11, 22)
        val charString = data.substring(22, 32)

        val offset = offsetString.padStart(16, '0').toUInt(2)
        val length = lengthString.padStart(16, '0').toUInt(2)
        val char = charString.padStart(16, '0').toInt(2).toChar()

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
        val tripletsPerBatch = 0..5000
        val validOffsetAndLengthRange = 2048
        val validCharRange = 1024

        // when
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

    @Test
    fun `Write and read randomised triplets`() {
        // given
        val tripletsPerBatch = 0..10000
        val validOffsetAndLengthRange = 2048
        val validCharRange = 1024

        val originalTriplets = mutableListOf<Triplet>()

        // when
        tripletsPerBatch.forEach { _ ->
            val offset = Random.nextInt(validOffsetAndLengthRange)
            val length = Random.nextInt(validOffsetAndLengthRange)
            val char = Random.nextInt(validCharRange).toChar()
            val randomValidTriplet = Triplet(offset, length, char)
            originalTriplets.add(randomValidTriplet)
        }

        val tripletsAsByteArray = ByteArray(originalTriplets.size * 4)
        originalTriplets.forEachIndexed { tripletIndex, triplet ->
            triplet.toByteArray().forEachIndexed { dataIndex, data ->
                tripletsAsByteArray[(tripletIndex * 4) + dataIndex] = data
            }
        }

        val fileName = writeToRandomTestFile(tripletsAsByteArray)
        val encodedBytes = readTempTestFile(fileName)

        val decodedTriplets = encodedBytes.toList().chunked(4).map { chunk ->
            ByteArray(4).apply {
                chunk.forEachIndexed { index, it ->
                    set(index, it)
                }
            }
        } .map { Triplet(it) }


        // then
        assertEquals(originalTriplets, decodedTriplets)
    }

    @Test
    fun `Generate, encode and write input text then read, decode and compare it with the original`(){
        // given
        val fileName = "decode_001.lz"
        val encodedBytes = readStaticTestFile(fileName)

        // when
        encodedBytes.toList().chunked(4).map { chunk ->
            ByteArray(4).apply {
                chunk.forEachIndexed { index, it ->
                    set(index, it)
                }
            }
        } .map { Triplet(it) }

        // then
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

    @Test
    fun `Write encoded Triplet to file than read it back and decode it`() {
        // given
        val offset = 2047
        val length = 128
        val char = 'ő'
        val originalTriplet = Triplet(offset, length, char)

        // when
        val fileName = writeToRandomTestFile(originalTriplet.toByteArray())
        val bytes = readTempTestFile(fileName)
        val decodedTriplet = Triplet(bytes)

        // then
        assertEquals(originalTriplet, decodedTriplet)

        // after
        // TODO: remove when the teardown cleanup works
        removeTestFile(fileName)
    }

    @AfterAll
    fun tearDown() {
        "teardown".prt()
        removeAllTestFiles()
    }
}

// for debugging
fun <T> T.prt(): T {
    println(this)
    return this
}