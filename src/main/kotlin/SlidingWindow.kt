sealed class PrefixSearchResult {
    override fun toString(): String {
        return when(this) {
            is PrefixFound -> this.toString()
            is None -> "None"
        }
    }
}
data class PrefixFound(val index: Int, val prefix: String) : PrefixSearchResult()
object None : PrefixSearchResult()

class SlidingWindow(val maxSize: Int, private val window: ArrayDeque<Char> = ArrayDeque(maxSize)) {
    fun find(sequenceToLookUp: String): PrefixSearchResult {
        val windowString = windowAsString()
        return when (windowString.contains(sequenceToLookUp)) {
            false -> None
            true -> {
                val index = windowString.indexOf(sequenceToLookUp)
                PrefixFound(index, sequenceToLookUp)
            }
        }
    }

    fun popIfFull(amount: Int = 1): SlidingWindow {
        if(window.size < maxSize) {
            return this
        }
        repeat(amount) {
            window.removeFirstOrNull().let {
                if (it == null) {
                    return@popIfFull this
                }
            }
        }
        return this
    }

    fun push(chars: CharSequence): SlidingWindow {
        check(chars.length <= maxSize) { "The size of [ $chars ] exceeds max window size [ $maxSize ]!"}
        window.addAll(chars.toList())
        return this
    }

    fun size () : Int {
        return window.size
    }

    private fun windowAsString(): String {
        return buildString { window.forEach { append(it) } }
    }

    override fun toString(): String {
        return window.toString()
    }
}