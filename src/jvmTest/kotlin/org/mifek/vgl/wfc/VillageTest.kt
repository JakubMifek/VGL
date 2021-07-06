package org.mifek.vgl.wfc

import org.mifek.wfc.models.options.Cartesian2DModelOptions
import kotlin.math.abs
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@ExperimentalUnsignedTypes
class VillageTest {
    fun printFloorPlan(result: Array<Array<Int>>) {
        for (x in result.indices) {
            for (y in result[x].indices) {
                print("${result[x][y]} ")
            }
            println()
        }
        println()
    }

    @Test
    fun testVillageLayoutGeneration() {
        val result = MinecraftVillageAdapter.generate(
            40,
            40,
            MinecraftVillageAdapterOptions(
                modelOptions = Cartesian2DModelOptions(
                    allowRotations = true,
                    allowHorizontalFlips = true,
                    allowVerticalFlips = true,
                    weightPower = 1.0,
                    roofed = true,
                    rightSided = true,
                    grounded = true,
                    leftSided = true
                ),
                templateOptions = mapOf(Pair("test3", Triple(1f, Pair(3, 3), Pair(20, 20)))),
                emptySpaceWeight = 1.5f,
                desiredNumberOfHouses = 5,
                seed = 12345
            ),
        )
        assertNotNull(result, "Result should not be null.")
        val layout = result.toList()
        assertFalse(layout.isEmpty(), "There should be houses in the layout.")
        printLayout(layout)
    }

    private fun printLayout(layout: Iterable<Triple<String, Pair<Int, Int>, Pair<Int, Int>>>) {
        for (item in layout) {
            println("Should build ${item.first} at [${item.second.first}, ${item.second.second}] of size ${item.third.first}x${item.third.second}")
        }
    }

    @Test
    fun testHouseQuantityGeneration() {
        val limit = 100
        var total = limit
        var houses = 0
        var wb = 0
        var t3 = 0
        val desiredHouses = 10
        var min = Int.MAX_VALUE
        var max = Int.MIN_VALUE
        val numbers = mutableMapOf<Int, Int>()
        val rand = Random(1234)

        val options = MinecraftVillageAdapterOptions(
            modelOptions = Cartesian2DModelOptions(
                allowRotations = true,
                allowHorizontalFlips = true,
                allowVerticalFlips = true,
                weightPower = 1.0,
                roofed = true,
                rightSided = true,
                grounded = true,
                leftSided = true
            ),
            templateOptions = mapOf(
                Pair("test3", Triple(1f, Pair(1, 1), null)),
                Pair("wood_block", Triple(2f, Pair(1, 1), null))
            ),
            emptySpaceWeight = 1.5f,
            desiredNumberOfHouses = desiredHouses,
        )

        for (i in 1..limit) {
            val result = MinecraftVillageAdapter.generate(
                20,
                20,
                options.copy(seed = rand.nextInt())
            )

            if (result == null) {
                total--
                continue
            }

            val layout = result.toList()

            t3 += layout.filter { it.first == "test3" }.count()
            wb += layout.filter { it.first == "wood_block" }.count()

            val count = layout.size

            houses += count

            numbers[count] = (numbers[count] ?: 0) + 1

            if (count < min) min = count
            if (count > max) max = count
        }

        println("Houses $houses (min $min max $max), total $total, average ${1.0 * houses / total}.")

        val deviation = 0.5

        println("Deviation $deviation, desired $desiredHouses")

        val maxOccurrence = numbers.maxOf { it.value }
        println("Ones ${numbers[1]}")
        println("MedianL ${numbers.keys.findLast { numbers[it] == maxOccurrence }} (${numbers[numbers.keys.findLast { numbers[it] == maxOccurrence }]})")
        println("MedianH ${numbers.keys.find { numbers[it] == maxOccurrence }} (${numbers[numbers.keys.find { numbers[it] == maxOccurrence }]})")

        assertTrue(
            abs(1.0 * houses / total - desiredHouses) < deviation,
            "Expected average number of houses (${1.0 * houses / total}) to be close to the desired one ($desiredHouses)."
        )
        assertTrue(
            abs(1.0 * wb / t3 - 2) < deviation,
            "Expected wood_blocks (${wb}) to be on average two times as many as test3s (${t3}) but got ratio ${1.0 * wb / t3}."
        )
    }
}