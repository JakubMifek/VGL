package org.mifek.vgl.utils

import org.mifek.vgl.implementations.Block
import org.mifek.vgl.implementations.Blocks
import org.mifek.vgl.wfc.DebugOptions
import org.mifek.wfc.datastructures.IntArray2D
import org.mifek.wfc.utils.toCoordinates

fun Array<Array<Block>>.toIntArray2D(): Triple<IntArray2D, Map<Int, Block>, Map<String, Int>> {
    val sizes = intArrayOf(size, this[0].size)
    val mapping = mutableMapOf<String, Int>()
    val reversedMapping = mutableMapOf<Int, Block>()
    var max = 0
    val data = IntArray2D(sizes[0], sizes[1]) {
        val coords = it.toCoordinates(sizes)
        val block = this[coords[0]][coords[1]].serialize()
        if (block !in mapping) {
            mapping[block] = max
            reversedMapping[max++] = this[coords[0]][coords[1]]
        }
        mapping[block]!!
    }
    return Triple(data, reversedMapping, mapping)
}

fun IntArray2D.toBlockData(mapping: Map<Int, Block>, debugOptions: DebugOptions?): Array<Array<Block>> {
    return Array(width) { w ->
        Array(height) { h ->
            mapping[this[w, h]] ?: Block(debugOptions?.undeterminedBlock ?: Blocks.BEACON)
        }
    }
}

fun Array<Array<Int?>>.toBlockData(
    mapping: Map<Int, Block>,
    debugOptions: DebugOptions?
): Array<Array<Block>> {
    return Array(size) { w ->
        Array(this[w].size) { h ->
            val key = this[w][h]
            if (key != null) {
                if (key !in mapping) {
                    throw Error("Key $key is missing from mapping!")
                }
                mapping[key]!!
            } else {
                Block(debugOptions?.undeterminedBlock ?: Blocks.BEACON)
            }
        }
    }
}