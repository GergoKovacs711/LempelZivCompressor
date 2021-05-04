package model

@ExperimentalUnsignedTypes
data class Triplet(val offset: Int, val length: Int, val nextCharacter: Char) {
    init {
        require(offset > -1 && offset < 2048) { "Offset out of range (0..2047)! Received offset: $offset" }
        require(length > -1 && length < 2048) { "Length out of range (0..2047)! Received length: $length" }
        require(nextCharacter.toInt() > -1 && nextCharacter.toInt() < 1024) { "NextCharacter out of range (UTF 0..1023)! Received nextCharacter: $nextCharacter" }
    }

    /**
     * Creates a model.Triplet for a 4 byte ByteArray. The bits from the 4 bytes are squashed together and decoded as:
     * { offset } is decoded from the first 11th bits as an unsigned integer between 0..2047,
     * { length } is decoded from the the next 11th bits as an unsigned integer between 0..2047,
     * { nextCharacter } is decoded from the last 10 bits as an UTF character between 0..1024
     */
    companion object {
        operator fun invoke(byteArray: ByteArray): Triplet {
            require(byteArray.size == 4) {
                "Triplets can only be created from a ByteArray of size 4! The received array was size of ${byteArray.size}"
            }
            val data = byteArray.joinToString("") { it.toUByte().toUInt().toString(2).padStart(8, '0') }
            val offset = data.substring(0, 11).padStart(16, '0').toUInt(2).toInt()
            val length = data.substring(11, 22).padStart(16, '0').toUInt(2).toInt()
            val char = data.substring(22, 32).padStart(16, '0').toInt(2).toChar()

            require(offset > -1 && offset < 2048) { "Offset out of range (0..2047)! Received offset: $offset" }
            require(length > -1 && offset < 2048) { "Length out of range (0..2047)! Received length: $length" }
            return Triplet(offset, length, char)
        }
    }

    /**
     * Transforms the triplet into a 4 byte array.
     * { offset } is encoded to a 11 bit bit sequence as an ,
     * { length } is encoded to a 11 bit bit sequence,
     * { nextCharacter } is encoded to a 10 bit bit sequence.
     * which equals to 32 bits (4 bytes) which is then returned as an array of 4 bytes
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
        return ByteArray(4).apply {
            bytes.forEachIndexed { index, it ->
                set(index, it.toByte())
            }
        }
    }

    fun conciseString() = "($offset, $length, $nextCharacter)"
}