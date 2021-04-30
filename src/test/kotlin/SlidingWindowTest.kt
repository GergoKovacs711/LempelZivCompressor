import org.junit.jupiter.api.Test
import kotlin.random.Random
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SlidingWindowTest {
    // given
    // when
    // then

    @Test
    fun `Window return the actual number of elemnts`() {
        // given
        val windowEmpty = SlidingWindow(10)
        val windowWithThreeElement = SlidingWindow(10).also { it.push("aaa") }
        val windowWithTenElement = SlidingWindow(10).also { it.push("aaaaaaaaaa") }
        val windowWithTenElementMinusFour = SlidingWindow(10).also { it.push("aaaaaaaaaa") }.also { it.popIfFull(4) }

        // then
        assertEquals(windowEmpty.size(), 0)
        assertEquals(windowWithThreeElement.size(), 3)
        assertEquals(windowWithTenElement.size(), 10)
        assertEquals(windowWithTenElementMinusFour.size(), 6)
    }

    @Test
    fun `Empty window returns None`(){
        // given
        val window = SlidingWindow(10)

        // when
        val result = window.find("f")

        // then
        assert(result is None)
    }

    @Test
    fun `Window returns None when no matching sequence is present`(){
        // given
        val window = SlidingWindow(10).also { it.push("ababababa") }

        // when
        val result = window.find("x")

        // then
        assert(result is None)
    }

    @Test
    fun `Window returns match when the sequence is at the end`(){
        // given
        val window = SlidingWindow(10).also { it.push("ababababat") }

        // when
        val result = window.find("t")

        // then
        assertTrue(result is PrefixFound)
        assertEquals(9, result.index)
        assertEquals("t", result.prefix)
    }

    @Test
    fun `Window returns first match when multiple matching sequences are present`(){
        // given
        val window = SlidingWindow(10).also { it.push("ababababat") }

        // when
        val result = window.find("aba")

        // then
        assertTrue(result is PrefixFound)
        assertEquals(0, result.index)
        assertEquals("aba", result.prefix)
    }

    @Test
    fun `Find specific last sequence in window`(){
        // given
        val window = SlidingWindow(7).also { it.push("acaacab") }

        // when
        val result = window.find("cab")

        // then
        assertTrue(result is PrefixFound)
        assertEquals(result.index, 4)
        assertEquals(result.prefix, "cab")
    }

    @Test
    fun `Basic decompression` () {
        // given
        val increments = mutableListOf<Increment>().apply {
            add(Increment(0,0, 'a'))
            add(Increment(1,1, 'c'))
            add(Increment(3,3, 'a'))
            add(Increment(0,0, 'b'))
            add(Increment(3,3, 'a'))
            add(Increment(6,1, 'a'))
            add(Increment(0,0, 'c'))
        }

        // when
        val result = decompress(increments)

        // then
        assertEquals("aacaacabcabaaac", result)
    }

    @Test
    fun `Basic compress than decompress` () {
        // given
        val inputString = "aacaacabcabaaac"

        val increments = mutableListOf<Increment>().apply {
            add(Increment(0,0, 'a'))
            add(Increment(1,1, 'c'))
            add(Increment(3,3, 'a'))
            add(Increment(0,0, 'b'))
            add(Increment(3,3, 'a'))
            add(Increment(6,1, 'a'))
            add(Increment(0,0, 'c'))
        }

        // when
        val result = compress(inputString, lookUpWindow)
        val decompressed = decompress(result)

        // then
        assertEquals(increments, result)
        assertEquals("aacaacabcabaaac", decompressed)
    }

    @Test
    fun `Randomised text compression and decompression`() {
        // given
        val charPool : List<Char> = ('a'..'z') + ('A'..'Z') + ('0'..'9')
        val runs = 10
        val inputSize = 1000

        val lookUpWindow = SlidingWindow(6)

        // when
        repeat(runs) {
            val inputText = inputSize.toRange()
                .map { _ -> Random.nextInt(1, charPool.size) }
                .map { charPool::get }
                .joinToString ("")

            println("Testing: ${inputText.toString()}")

            val increments = compress(inputText, lookUpWindow)
            val decompressedText = decompress(increments)

            // then
            assertEquals(inputText, decompressedText)
            println("Test ${it.toString()} success")
        }

    }

    private fun Int.toRange() = 1..this


/*    @Test
    fun test(){
        // given

        // when

        // then
    }*/
    // test template
}