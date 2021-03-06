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
            "Egy sz??veg ??br??zol??s??hoz sz??ks??g van ??r??sra is, amely eszk??zt??r??val (pl. bet??kkel) fon??m??kat, sz??tagokat, ill. szavakat ??s fogalmakat k??dol. K??l??nb??z?? kult??r??k ??s korok erre a c??lra k??l??nb??z?? jelrendszert haszn??lnak. A sz??veg egyik legfontosabb ??s megker??lhetetlen (immanens) tulajdons??ga, amelyet mind az ??r?? mind az olvas?? k??nytelen k??vetni (ha a sz??veget olvasni akarja) a linearit??s.Az ??rott sz??veg az emberis??g t??rt??nelm??ben hatalmas el??rel??p??s, hiszen ??gy a t??rt??nelme folyam??n egyed??li m??don lehet??v?? v??lt az inform??ci?? szem??lyt??l t??rben ??s id??ben f??ggetlen t??rol??sa szemben a sz??jhagyom??nnyal, amely mind t??rben mind id??ben adott szem??lyhez vagy szem??lyekhez k??t??tt. A t??rt??nelemr??l r??nk maradt inform??ci??k legnagyobb r??sze a XX. sz??zadig ??r??sos sz??vegeml??kekb??l ??ll. Azok a sz??vegek, amelyek olyan kult??r??kt??l sz??rmaznak, ahol az ??r??sos inform??ci??r??gz??t??s l??tezik, a sz??vegek fel??p??t??se alapvet??en k??l??nb??zik az olyan kult??r??k sz??vegeit??l, ahol inform??ci??k csak sz??jhagyom??ny ??tj??n maradtak fenn. A t??rsadalomtudom??nyokban a sz??veges hagyom??ny n??lk??li kult??r??kat nagyr??szt az ??kori ill. t??rt??nelme el??tti kult??r??khoz sorolj??k. ??gy a t??rsadalomtudom??nyban l??tezik a kult??r??nak egy olyan fontos meghat??roz??sa, amelynek alapj??ul k??zvetetten b??r de a sz??veg szolg??l."

        // when
        val lookUpWindow = SlidingWindow(50)
        val triplets = app.compress(hungarianText, lookUpWindow)
        val decompressedText = app.decompress(triplets)

        // then
        assertEquals(hungarianText, decompressedText)
    }
}

private fun Int.toRange() = 1..this
