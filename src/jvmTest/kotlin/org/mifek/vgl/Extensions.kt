package org.mifek.vgl

import org.mifek.vgl.implementations.Block
import org.mifek.vgl.implementations.PlacedBlock

fun Array<Array<Array<PlacedBlock>>>.formatForPrint(defaultMapping: Map<String, String> = emptyMap()): String {
    if (isEmpty() || this[0].isEmpty() || this[0][0].isEmpty()) return ""

    val blockTypes = this.flatten().toTypedArray().flatten().map { it.serialize() }.distinct()

    val blocksAmount = blockTypes.size
    var tmp = blocksAmount
    var places = 0
    while (tmp > 0) {
        places++
        tmp /= 10
    }

    val b = StringBuilder()
    val mapping = hashMapOf(*defaultMapping.entries.map { Pair(it.key, it.value) }.toTypedArray())

    for (block in blockTypes) {
        if (block !in mapping) {
            mapping[block] = "${mapping.size}".padStart(places)
        }

        println("${mapping[block]}: $block (${Block.deserialize(block).block})")
    }

    for (z in 0 until this[0][0].size) {
        for (y in 0 until this[0].size) {
            for (x in 0 until size) {
                val block = this[x][this[0].size - 1 - y][z].serialize()

                b.append(mapping[block])
                if (x != size - 1) b.append(" ")
            }

            b.append('\n')
        }
        b.append("\n\n")
    }

    return b.toString()
}