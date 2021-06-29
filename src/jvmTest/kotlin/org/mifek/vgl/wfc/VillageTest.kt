package org.mifek.vgl.wfc

import org.mifek.wfc.models.options.Cartesian2DModelOptions
import kotlin.math.abs
import kotlin.test.Test
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
                templateOptions = mapOf(Pair("test3", Pair(1f, Triple(3, 3, 3)))),
                emptySpaceWeight = 57f,
                desiredNumberOfHouses = 2,
                seed = 12345
            ),
        )
        assertNotNull(result)
//        printFloorPlan(result)
    }


    @Test
    fun testHouseQuantityGeneration() {
        val limit = 100
        var total = limit
        var houses = 0
        val desiredHouses = 10
        var min = Int.MAX_VALUE
        var max = Int.MIN_VALUE
        val numbers = mutableMapOf<Int, Int>()

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
            templateOptions = mapOf(Pair("test3", Pair(1f, Triple(3, 3, 3)))),
            emptySpaceWeight = 57f,
            desiredNumberOfHouses = desiredHouses,
        )

        for (i in 1..limit) {
            val result = MinecraftVillageAdapter.generate(
                20,
                20,
                options
            )

            if (result == null) {
                total--
                continue
            }

            val count = result.count()

            houses += count

            numbers[count] = (numbers[count] ?: 0) + 1

            if (count < min) min = count
            if (count > max) max = count
        }

        println("Houses $houses (min $min max $max), total $total, average ${1.0 * houses / total}.")

        val deviation = 0.05

        println("Deviation $deviation, desired $desiredHouses")

        val maxOccurrence = numbers.maxOf { it.value }
        println("Ones ${numbers[1]}")
        println("MedianL ${numbers.keys.findLast { numbers[it] == maxOccurrence }} (${numbers[numbers.keys.findLast { numbers[it] == maxOccurrence }]})")
        println("MedianH ${numbers.keys.find { numbers[it] == maxOccurrence }} (${numbers[numbers.keys.find { numbers[it] == maxOccurrence }]})")

        assertTrue(abs(1.0*houses / total - desiredHouses) < deviation)
    }
}