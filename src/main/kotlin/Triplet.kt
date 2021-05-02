@ExperimentalUnsignedTypes
data class Triplet(val offset: Int, val length: Int, val nextCharacter: Char) {
    companion object {
        operator fun invoke(byteArray : ByteArray): Triplet {
            val data = byteArray.joinToString("") { it.toUByte().toUInt().toString(2).padStart(8, '0') }
            val offset = data.substring(0, 11).padStart(16, '0').toUInt(2).toInt()
            val length  = data.substring(11, 22).padStart(16, '0').toUInt(2).toInt()
            val char  = data.substring(22, 32).padStart(16, '0').toInt(2).toChar()
            return Triplet(offset, length, char)
        }
    }

    /**
     * Transforms the triplet into a 4 byte array.
     * offset is encoded to a 11 bit bit sequence,
     * length is encoded to a 11 bit bit sequence,
     * nextCharacter is encoded to a 10 bit bit sequence
     * which equals to 32 bit (4 bytes) which is then returned as an array of 4 bytes
     */
    fun toByteArray(): ByteArray {
        val offsetBits = offset.toUInt().toString(2).padStart(11, '0')
        val lengthBits = length.toUInt().toString(2).padStart(11, '0')
        val charBits = nextCharacter.toInt().toString(2).padStart(10, '0')

        val buffer = StringBuffer().apply {
            append(offsetBits)
            append(lengthBits)
            append(charBits)
        }
        val bytes = buffer.toString().chunked(8).map { it.toUInt(2).toUByte() }
        return ByteArray(4).apply { bytes.forEach { this.plus(it.toByte()) }
        }
    }

    override fun toString(): String {
        return "[$offset, $length, $nextCharacter]"
    }
}