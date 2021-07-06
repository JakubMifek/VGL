package org.mifek.vgl.utils

import kotlin.random.Random

fun <T> Map<T, Float>.random(rand: Random = Random.Default): T {
    val list = toList()
    val sum = values.sum()
    val vector = list.map { it.second / sum }.toTypedArray()
    val number = rand.nextDouble()
    var partialSum = 0f
    var i = 0
    do {
        partialSum += vector[i]
    } while (partialSum < number && ++i + 1 < vector.size)

    return list[i].first
}