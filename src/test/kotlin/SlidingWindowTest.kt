import org.junit.jupiter.api.Test
import java.io.File
import java.lang.Exception
import java.time.LocalDate
import java.time.LocalTime
import kotlin.random.Random
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SlidingWindowTest {

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
    fun `Empty window returns None`() {
        // given
        val window = SlidingWindow(10)

        // when
        val result = window.find("f")

        // then
        assert(result is None)
    }

    @Test
    fun `Window returns None when no matching sequence is present`() {
        // given
        val window = SlidingWindow(10).also { it.push("ababababa") }

        // when
        val result = window.find("x")

        // then
        assert(result is None)
    }

    @Test
    fun `Window returns match when the sequence is at the end`() {
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
    fun `Window returns first match when multiple matching sequences are present`() {
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
    fun `Find specific last sequence in window`() {
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
    fun `Basic decompression`() {
        // given
        val triplets = mutableListOf<Triplet>().apply {
            add(Triplet(0, 0, 'a'))
            add(Triplet(1, 1, 'c'))
            add(Triplet(3, 3, 'a'))
            add(Triplet(0, 0, 'b'))
            add(Triplet(3, 3, 'a'))
            add(Triplet(6, 1, 'a'))
            add(Triplet(0, 0, 'c'))
        }

        // when
        val result = decompress(triplets)

        // then
        assertEquals("aacaacabcabaaac", result)
    }

    @Test
    fun `Basic compress than decompress`() {
        // given
        val inputString = "aacaacabcabaaac"

        val triplets = mutableListOf<Triplet>().apply {
            add(Triplet(0, 0, 'a'))
            add(Triplet(1, 1, 'c'))
            add(Triplet(3, 3, 'a'))
            add(Triplet(0, 0, 'b'))
            add(Triplet(3, 3, 'a'))
            add(Triplet(6, 1, 'a'))
            add(Triplet(0, 0, 'c'))
        }

        val lookUpWindow = SlidingWindow(6)

        // when
        val result = compress(inputString, lookUpWindow)
        val decompressed = decompress(result)

        // then
        assertEquals(triplets, result)
        assertEquals("aacaacabcabaaac", decompressed)
    }

    @Test
    fun `Randomised stress-test`() {
        // given
        val charPool: List<Char> = ('a'..'z') + ('A'..'Z') + ('0'..'9')
        val runs = 100
        val inputSize = 10000

        val inputs = mutableListOf<String>()
        val outputs = mutableListOf<String>()
        var errorOccurred: Exception? = null
        // when
        try {
            for (index in runs.toRange()) {
                val lookUpWindow = SlidingWindow(256)
                val inputText = inputSize.toRange()
                    .map { _ -> Random.nextInt(1, charPool.size) }
                    .map { i -> charPool[i] }
                    .joinToString("")
                    .also { inputs.add(it) }

                val triplets = compress(inputText, lookUpWindow)
                val decompressedText = decompress(triplets).also { outputs.add(it) }

                // then
                if(inputText != decompressedText){
                    inputText.print()
                }
                assertEquals(inputText, decompressedText)
                println("Test $index success")
            }
        } catch (e: Exception) {
            errorOccurred = e
        }
        errorOccurred.apply {
            writeToRandomTextFile(inputs, outputs, this?.stackTraceToString())
        }?.also {
            throw it
        }
    }

    @Test
    fun `Pesky bug finder`() {
        // given
        val buggyInput =
            "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"
        // when

        repeat(10) {
            var lookUpWindow = SlidingWindow(256)
            val badlyCompressedTriplets = compress(buggyInput, lookUpWindow)
            var decompressedText = decompress(badlyCompressedTriplets)

            // then
            if(buggyInput != decompressedText){
                buggyInput.print()
            }
            assertEquals(buggyInput, decompressedText)
        }
    }

    @Test
    fun `Run a compression-decompression cycle with hungarian text`() {
        // given
        val hungarianText =
            "Egy szöveg ábrázolásához szükség van írásra is, amely eszköztárával (pl. betűkkel) fonémákat, szótagokat, ill. szavakat és fogalmakat kódol. Különböző kultúrák és korok erre a célra különböző jelrendszert használnak. A szöveg egyik legfontosabb és megkerülhetetlen (immanens) tulajdonsága, amelyet mind az író mind az olvasó kénytelen követni (ha a szöveget olvasni akarja) a linearitás.Az írott szöveg az emberiség történelmében hatalmas előrelépés, hiszen így a történelme folyamán egyedüli módon lehetővé vált az információ személytől térben és időben független tárolása szemben a szájhagyománnyal, amely mind térben mind időben adott személyhez vagy személyekhez kötött. A történelemről ránk maradt információk legnagyobb része a XX. századig írásos szövegemlékekből áll. Azok a szövegek, amelyek olyan kultúráktól származnak, ahol az írásos információrögzítés létezik, a szövegek felépítése alapvetően különbözik az olyan kultúrák szövegeitől, ahol információk csak szájhagyomány útján maradtak fenn. A társadalomtudományokban a szöveges hagyomány nélküli kultúrákat nagyrészt az ókori ill. történelme előtti kultúrákhoz sorolják. Így a társadalomtudományban létezik a kultúrának egy olyan fontos meghatározása, amelynek alapjául közvetetten bár de a szöveg szolgál."

        // when
        var lookUpWindow = SlidingWindow(50)
        val triplets = compress(hungarianText, lookUpWindow)
        var decompressedText = decompress(triplets)

        // then
        assertEquals(hungarianText, decompressedText)
    }

    companion object {
        fun writeToRandomTextFile(
            inputs: MutableList<String>,
            outputs: MutableList<String>,
            error: String? = null
        ) {
            val fileName = generateRandomFileName()
            val file = File(fileName)
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

        fun generateRandomFileName(): String {
            val date = LocalDate.now()
            val time = LocalTime.now()
            var randomEnding = ""
            repeat(10) { randomEnding += Random.nextInt(10).toString() }
            return "test" + date + "-" + time.hour + "-" + time.minute + "-" + time.second + "-" + randomEnding + ".lz"
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