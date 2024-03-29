/*
package org.mifek.vgl.village

import org.mifek.vgl.palettes.PaletteKeys
import org.mifek.vgl.utils.TemplateHolder
import org.mifek.wfc.models.OverlappingCartesian2DModel
import org.mifek.wfc.models.options.Cartesian2DModelOptions
import org.mifek.wfc.models.storage.PatternWeights2D
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

@ExperimentalUnsignedTypes
class FloorPlan {
    companion object {
        private const val REPEATS = 5



        fun generate(name: String, width: Int, height: Int, seed: Int = Random.nextInt()): Array<Array<PaletteKeys>> {
            val model = OverlappingCartesian2DModel(
                floorPlanPatternWeights,
                width,
                height
            )

            for (x in width / 4 until 3 * width / 4) {
                for (y in height / 4 until 3 * height / 4) {
                    model.banPixel(x, y, PaletteKeys.GROUND.id)
                }
            }

            val algorithm = model.build()
            var result = false

            val r = Random(seed)

            for (i in 1..REPEATS) {
                result = algorithm.run(r.nextInt())
                if (result) break
            }

            if (!result) {
                throw Error("Unable to generate floor plan!")
            }

            val output = model.constructOutput(algorithm)

            val plan = Array(output.height) { y -> Array(output.width) { x -> PaletteKeys.getById(output[x, y]) } }

            return naiveDoorFix(
                plan, r
            )
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
}*/
