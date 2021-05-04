import config.Mode
import config.NotProvided
import file.FileAccess
import file.writeToRandomOutPutFile
import org.junit.jupiter.api.Test
import logging.LogLevel
import model.None
import model.Prefix
import model.SlidingWindow
import model.Triplet
import kotlin.random.Random
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@ExperimentalUnsignedTypes
class SlidingWindowTest {
    private val app = Application(
        filePath = NotProvided,
        logLevel = LogLevel.NONE,
        mode = Mode.COMPRESS,
        windowSize = 512,
        directRootOutPut = FileAccess.DirectoryOutput.TEST
    )

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
        assertTrue(result is Prefix)
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
        assertTrue(result is Prefix)
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
        assertTrue(result is Prefix)
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
        val result = app.decompress(triplets)

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
        val result = app.compress(inputString, lookUpWindow)
        val decompressed = app.decompress(result)

        // then
        assertEquals(triplets, result)
        assertEquals("aacaacabcabaaac", decompressed)
    }

    @Test
    fun `Randomised stress-test`() {
        // given
        val charPool: List<Char> = ('a'..'z') + ('A'..'Z') + ('0'..'9')
        val runs = 20
        val inputSize = 10000

        val inputs = mutableListOf<String>()
        val outputs = mutableListOf<String>()
        var errorOccurred: Exception? = null
        // when
        try {
            for (index in runs.toRange()) {
                val lookUpWindow = SlidingWindow(2000)
                val inputText = inputSize.toRange()
                    .map { Random.nextInt(1, charPool.size) }
                    .map { charPool[it] }
                    .joinToString("")
                    .also { inputs.add(it) }

                val triplets = app.compress(inputText, lookUpWindow)
                val decompressedText = app.decompress(triplets).also { outputs.add(it) }

                // then
                assertEquals(inputText, decompressedText)
            }
        } catch (e: Exception) {
            errorOccurred = e
        }
        errorOccurred.apply {
            writeToRandomOutPutFile(inputs, outputs, this?.stackTraceToString())
        }?.also {
            throw it
        }
    }

    @Test
    fun `Run a compression-decompression cycle with hungarian text`() {
        // given
        val hungarianText =
            "Egy szöveg ábrázolásához szükség van írásra is, amely eszköztárával (pl. betűkkel) fonémákat, szótagokat, ill. szavakat és fogalmakat kódol. Különböző kultúrák és korok erre a célra különböző jelrendszert használnak. A szöveg egyik legfontosabb és megkerülhetetlen (immanens) tulajdonsága, amelyet mind az író mind az olvasó kénytelen követni (ha a szöveget olvasni akarja) a linearitás.Az írott szöveg az emberiség történelmében hatalmas előrelépés, hiszen így a történelme folyamán egyedüli módon lehetővé vált az információ személytől térben és időben független tárolása szemben a szájhagyománnyal, amely mind térben mind időben adott személyhez vagy személyekhez kötött. A történelemről ránk maradt információk legnagyobb része a XX. századig írásos szövegemlékekből áll. Azok a szövegek, amelyek olyan kultúráktól származnak, ahol az írásos információrögzítés létezik, a szövegek felépítése alapvetően különbözik az olyan kultúrák szövegeitől, ahol információk csak szájhagyomány útján maradtak fenn. A társadalomtudományokban a szöveges hagyomány nélküli kultúrákat nagyrészt az ókori ill. történelme előtti kultúrákhoz sorolják. Így a társadalomtudományban létezik a kultúrának egy olyan fontos meghatározása, amelynek alapjául közvetetten bár de a szöveg szolgál."

        // when
        val lookUpWindow = SlidingWindow(50)
        val triplets = app.compress(hungarianText, lookUpWindow)
        val decompressedText = app.decompress(triplets)

        // then
        assertEquals(hungarianText, decompressedText)
    }
}

private fun Int.toRange() = 1..this
