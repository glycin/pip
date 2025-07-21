package com.glycin.pipserver.util

import kotlin.math.ceil
import kotlin.random.Random

/*
    Implementation derived from https://github.com/viascom/nanoid-kotlin/blob/main/src/main/kotlin/io/viascom/nanoid/NanoId.kt
*/

object NanoId {

    private const val DEFAULT_ALPHABET = "_-0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ"
    private const val DEFAULT_SIZE = 10
    private const val DEFAULT_ADDITIONAL_BYTES_FACTOR = 1.6

    fun generate(
        size: Int = DEFAULT_SIZE,
        alphabet: String = DEFAULT_ALPHABET,
        additionalBytesFactor: Double = DEFAULT_ADDITIONAL_BYTES_FACTOR,
        random: Random = Random.Default
    ): String {
        require(alphabet.isNotEmpty() && alphabet.length < 256)
        require(size > 0)

        val mask = calcMask(alphabet)
        val step = calcStep(size, alphabet, additionalBytesFactor)
        return buildString(size) {
            val bytes = ByteArray(step)
            while (length < size) {
                random.nextBytes(bytes)
                for (byte in bytes) {
                    val idx = byte.toInt() and mask
                    if (idx < alphabet.length) {
                        append(alphabet[idx])
                        if (length == size) break
                    }
                }
            }
        }
    }

    private fun calcMask(alphabet: String): Int =
        (2 shl (Int.SIZE_BITS - 1 - Integer.numberOfLeadingZeros(alphabet.length - 1))) - 1

    private fun calcStep(size: Int, alphabet: String, factor: Double) =
        ceil(factor * calcMask(alphabet) * size / alphabet.length).toInt()
}