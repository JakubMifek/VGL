package org.mifek.vgl.wfc

import org.mifek.vgl.implementations.Block
import org.mifek.vgl.implementations.Blocks
import org.mifek.vgl.implementations.PlacedBlock
import org.mifek.vgl.implementations.PlacementStyle
import org.mifek.vgl.palettes.PaletteKeys
import org.mifek.vgl.utils.toBlockData
import org.mifek.vgl.utils.toIntArray2D
import org.mifek.wfc.datastructures.IntArray2D
import org.mifek.wfc.datastructures.IntArray3D
import org.mifek.wfc.models.OverlappingCartesian2DModel
import org.mifek.wfc.models.options.Cartesian2DModelOptions
import org.mifek.wfc.models.storage.PatternWeights2D
import org.mifek.wfc.utils.formatPatterns
import org.mifek.wfc.utils.toCoordinates
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

@ExperimentalUnsignedTypes
class FloorPlan {
    companion object {
        private val cache = HashMap<String, PatternWeights2D>()

        private fun serializeOptions(options: Cartesian2DModelOptions): String {
            return options.toString()
                .substringAfter('(')
                .substringBefore(')')
                .split(", ")
                .joinToString("") { it.split("=")[1] }
        }

        val mapping = mapOf(
            Pair(Blocks.OAK_WOOD_PLANK, "▣"),
            Pair(Blocks.STONE, "⬛"),
            Pair(Blocks.GRASS, "⬜"),
            Pair(PaletteKeys.DOORS, "▣"),
            Pair(PaletteKeys.WALL, "⬛"),
            Pair(PaletteKeys.GROUND, "⬜"),
            Pair(PaletteKeys.FLOOR, "⬜"),
            Pair(null, "?")
        )

        fun printFloorPlan(result: Array<Array<PaletteKeys>>) {
            for (x in result.indices) {
                for (y in result[x].indices) {
                    print(mapping[result[x][y]] + " ")
                }
                println()
            }
            println()
        }

        fun printFloorPlan(result: Array<Array<Block>>) {
            for (x in result.indices) {
                for (y in result[x].indices) {
                    print(mapping[result[x][y].block] + " ")
                }
                println()
            }
            println()
        }

        fun generate(
            template: Array<Array<Block>>,
            width: Int,
            height: Int,
            options: FloorPlanOptions,
        ): Array<Array<Block>>? {
            val dataMapping = template.toIntArray2D()

            var patternWeights: PatternWeights2D? = null
            val key = "${options.name}_${options.overlap}_${serializeOptions(options.modelOptions)}"

            // Cache retrieval
            if (options.name != null && cache.containsKey(key)) {
                patternWeights = cache[key]
                println("Using stored $key")
            }

            // Use cache if possible
            val model = if (patternWeights != null) OverlappingCartesian2DModel(
                patternWeights,
                width,
                height
            ) else OverlappingCartesian2DModel(
                dataMapping.first,
                options.overlap,
                width,
                height,
                options.modelOptions
            )

            // Store to cache if not already present
            if (options.name != null && patternWeights == null) {
                println("Storing $key")
                cache[key] = model.storage
            }
//
//            for ((i, pattern) in model.patterns.withIndex()) {
//                println("$i:")
//                val formatted = IntArray2D(model.patternSideSize, model.patternSideSize) { pattern[it] }
//                for (y in 0 until model.patternSideSize) {
//                    for (x in 0 until model.patternSideSize) {
//                        print("${formatted[x, y]} ")
//                    }
//                    println()
//                }
//            }

            if (options.setBlocks != null) {
                options.setBlocks.forEach {
                    val coordinates = it.first
                    val value = dataMapping.third[it.second.serialize()]
                        ?: throw Error("Set block was not in the template palette.")
                    model.setPixel(
                        coordinates.first,
                        coordinates.second,
                        value
                    )
                }
            }

            if (options.bannedBlocks != null) {
                options.bannedBlocks.forEach {
                    val coordinates = it.first
                    val value = dataMapping.third[it.second.serialize()]
                        ?: return@forEach
                    model.banPixel(
                        coordinates.first,
                        coordinates.second,
                        value
                    )
                }
            }

            val algorithm = model.build()
/*
            algorithm.afterBan += {
                if (it.second == 37) {
                    println(
                        "Banning ${it.third} for ${it.second}. Remaining ${it.first.waves[37].sumOf { if (it) 1.0 else 0.0 }} - first is ${
                            it.first.waves[37].indexOf(
                                true
                            )
                        }"
                    )

                    printFloorPlan(
                        model.constructNullableOutput(algorithm).toBlockData(dataMapping.second, options.debugOptions)
                    )
                }
            }
*/

            val random = Random(options.debugOptions?.seed ?: Random.Default.nextInt())
            var failed = false

            algorithm.afterFail += {
                println("Failed")
                failed = true
            }

            for (i in 0 until options.repeats) {
                val seed = random.nextInt()
                println("---------")
                println("Try ${i + 1}; seed = $seed")
                println("---------")
                failed = false
                if (algorithm.run(seed)) break
            }

            if (!failed) {
                println("Succeeded")
            } else {
                return null
            }

            val output = model.constructNullableOutput(algorithm).toBlockData(dataMapping.second, options.debugOptions)

//            printFloorPlan(output)

//            val paletteMapped = Array(output.size) { x ->
//                Array(output[x].size) { y ->
//                    val res = when (output[x][y].block) {
//                        Blocks.DIRT -> PaletteKeys.GROUND
//                        Blocks.GRASS -> PaletteKeys.GROUND
//                        Blocks.STONE -> PaletteKeys.WALL
//                        else -> PaletteKeys.FLOOR
//                    }
//                    // If the result is to be floor
//                    if (res == PaletteKeys.FLOOR
//                        // But there are horizontal walls around
//                        && (x > 0 && x < output.size - 1 && output[x - 1][y].block == Blocks.STONE && output[x + 1][y].block == Blocks.STONE
//                                // Or vertical walls around
//                                || y > 0 && y < output[x].size - 1 && output[x][y - 1].block == Blocks.STONE && output[x][y + 1].block == Blocks.STONE)
//                    ) {
//                        // It's actually a door separating two rooms
//                        PaletteKeys.DOORS
//                    } else res
//                }
//            }

            return output

//            printFloorPlan(paletteMapped)

//            val plan = naiveDoorFix(paletteMapped, random)
//
//            return Array(output.size) { x ->
//                Array(output[x].size) { y ->
//                    when (plan[x][y]) {
//                        PaletteKeys.WALL -> Block(Blocks.STONE)
//                        PaletteKeys.GROUND -> Block(Blocks.GRASS)
//                        else -> Block(Blocks.OAK_WOOD_PLANK)
//                    }
//                }
//            }
        }

        fun naiveDoorFix(data: Array<Array<PaletteKeys>>, r: Random = Random.Default): Array<Array<PaletteKeys>> {
            val mapped = Array(data.size) { x ->
                IntArray(data[x].size) { y ->
                    when (data[x][y]) {
                        PaletteKeys.GROUND -> -1
                        PaletteKeys.DOORS -> -2
                        PaletteKeys.FLOOR -> -1
                        PaletteKeys.WALL -> -2
                    }
                }
            }

            // Color the rooms in separate colors (outdoors gets color 0)
            var latestColor = 0
            val indices = arrayListOf<Triple<Int, Int, Int>>()
            for (x in mapped.indices) {
                for (y in mapped[x].indices) {
                    if (mapped[x][y] == -1) {
                        color(mapped, latestColor, x, y)
                        indices.add(Triple(latestColor, x, y))
                        latestColor++
                    }
                }
            }

            val walls =
                mapped.indices.map { x ->
                    mapped[x].indices.filter { y -> mapped[x][y] == -2 }.map { y ->
                        val hor = mapped[x + 1][y] == -2 || mapped[x - 1][y] == -2
                        val ver = mapped[x][y + 1] == -2 || mapped[x][y - 1] == -2
                        Triple(
                            when {
                                hor == ver -> null
                                hor -> Pair(
                                    min(mapped[x][y - 1], mapped[x][y + 1]),
                                    max(mapped[x][y - 1], mapped[x][y + 1])
                                )
                                else -> Pair(
                                    min(mapped[x - 1][y], mapped[x + 1][y]),
                                    max(mapped[x - 1][y], mapped[x + 1][y])
                                )
                            }, x, y
                        )
                    }
                }.flatten()

            // Remove false doors
            val invalidDoors = walls.filter {
                data[it.second][it.third] == PaletteKeys.DOORS &&
                        (it.first == null || it.first!!.first == it.first!!.second)
            }
            for (door in invalidDoors) {
                data[door.second][door.third] = PaletteKeys.WALL
                mapped[door.second][door.third] = -2
            }

            // Find edges between rooms
            val doors: MutableList<Triple<Pair<Int, Int>, Int, Int>> =
                walls.filter { data[it.second][it.third] == PaletteKeys.DOORS }
                    .map { Triple(it.first!!, it.second, it.third) }
                    .sortedBy {
                        it.first.first * latestColor + it.first.second
                    }.toMutableList()

            // Cleanup - remove duplicate doors, make sure each room is accessible from any other
            // First remove duplicates (makes the rest easier)
            val doorsToRemove = mutableListOf<Triple<Pair<Int, Int>, Int, Int>>()
            if (doors.size > 0) {
                var j = 0
                var colors = doors[0].first
                for (i in doors.indices) {
                    if (colors.first == doors[i].first.first && colors.second == doors[i].first.second) continue

                    // interval between [j; i) has same colors
                    val randomPick = r.nextInt(j, i)
                    for (k in j until i) {
                        if (k == randomPick) continue

                        data[doors[k].second][doors[k].third] = PaletteKeys.WALL
                        mapped[doors[k].second][doors[k].third] = -2

                        doorsToRemove.add(doors[k])
                    }

                    j = i
                    colors = doors[i].first
                }

                val randomPick = r.nextInt(j, doors.size)
                for (k in j until doors.size) {
                    if (k == randomPick) continue

                    data[doors[k].second][doors[k].third] = PaletteKeys.WALL
                    mapped[doors[k].second][doors[k].third] = -2

                    doorsToRemove.add(doors[k])
                }
            }

            doors.removeAll(doorsToRemove)

            val decomposed = decompose(latestColor, doors)

            unifyComponents(data, mapped, walls, doors, decomposed, r)

            return data
        }

        fun unifyComponents(
            data: Array<Array<PaletteKeys>>,
            mapped: Array<IntArray>,
            walls: List<Triple<Pair<Int, Int>?, Int, Int>>,
            doors: MutableList<Triple<Pair<Int, Int>, Int, Int>>,
            decomposed: IntArray,
            r: Random = Random.Default
        ) {
            val components = decomposed.distinct().toMutableList()

            if (components.size < 2) return

            while (components.size > 2) {
                val first = components.random(r)
                var second = components.random(r)
                while (second == first) {
                    second = components.random(r)
                }

                val ret = unifyTwoComponents(
                    data,
                    mapped,
                    walls,
                    doors,
                    decomposed,
                    min(first, second),
                    max(first, second),
                    r
                )
                if (ret == -1) continue

                components.remove(first)
                components.remove(second)
                components.add(ret)

                for (i in decomposed.indices)
                    if (decomposed[i] == first || decomposed[i] == second)
                        decomposed[i] = ret
            }

            val ret = unifyTwoComponents(
                data,
                mapped,
                walls,
                doors,
                decomposed,
                min(components[0], components[1]),
                max(components[0], components[1]),
                r
            )

            for (i in decomposed.indices)
                if (decomposed[i] == components[0] || decomposed[i] == components[1])
                    decomposed[i] = ret
        }

        private fun unifyTwoComponents(
            data: Array<Array<PaletteKeys>>,
            mapped: Array<IntArray>,
            walls: List<Triple<Pair<Int, Int>?, Int, Int>>,
            doors: MutableList<Triple<Pair<Int, Int>, Int, Int>>,
            decomposed: IntArray,
            component1: Int,
            component2: Int,
            r: Random = Random.Default
        ): Int {
            val possibleWalls =
                walls.filter {
                    it.first != null // Between two rooms
                            && data[it.second][it.third] != PaletteKeys.DOORS // Cannot be doors
                            && decomposed[it.first!!.first] != decomposed[it.first!!.second] // Components differ
                            && (decomposed[it.first!!.first] == component1 || decomposed[it.first!!.first] == component2) // First room is one of components
                            && (decomposed[it.first!!.second] == component1 || decomposed[it.first!!.second] == component2) // Second room is one of components
                }.toList()
            if (possibleWalls.isEmpty())
                return -1

            // Select random wall
            val wall = possibleWalls.random(r)

            // Make it a door
            doors.add(Triple(wall.first!!, wall.second, wall.third))
            data[wall.second][wall.third] = PaletteKeys.DOORS
            mapped[wall.second][wall.third] = -2

            // Unify under smaller component number
            return component1
        }

        fun decompose(rooms: Int, doors: List<Triple<Pair<Int, Int>, Int, Int>>): IntArray {
            val ret = IntArray(rooms) { it }
            var i = 0

            // Go through rooms
            for (color in ret.indices) {

                val relevantDoors = doors.filter { it.first.first == color || it.first.second == color }

                // Go through neighbours
                for (door in relevantDoors) {
                    val neighbour = if (door.first.first == color) door.first.second else door.first.first

                    // If their colors differ, unify
                    if (ret[color] != ret[neighbour]) {
                        for (j in ret.indices) {
                            if (ret[j] == ret[neighbour]) {
                                ret[j] = ret[color]
                            }
                        }
                    }

                    i++
                }
            }

            return ret
        }

        fun color(data: Array<IntArray>, color: Int, x: Int, y: Int) {
            data[x][y] = color

            // Recursion
            if (x > 0 && data[x - 1][y] == -1) color(data, color, x - 1, y)
            if (y > 0 && data[x][y - 1] == -1) color(data, color, x, y - 1)
            if (x < data.size - 1 && data[x + 1][y] == -1) color(data, color, x + 1, y)
            if (y < data[x].size - 1 && data[x][y + 1] == -1) color(data, color, x, y + 1)
        }
    }
}