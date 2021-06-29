package org.mifek.vgl.wfc

import org.mifek.wfc.datastructures.IntArray2D
import org.mifek.wfc.datastructures.PatternsArrayBuilder
import org.mifek.wfc.datatypes.Direction2D
import org.mifek.wfc.models.options.Cartesian2DModelOptions
import org.mifek.vgl.utils.TemplateHolder
import org.mifek.wfc.core.Cartesian2DWfcAlgorithm
import org.mifek.wfc.datastructures.IntHolder
import org.mifek.wfc.models.Patterns
import org.mifek.wfc.models.Pixels
import org.mifek.wfc.topologies.Cartesian2DTopology
import org.mifek.wfc.utils.*
import kotlin.math.pow
import kotlin.math.sqrt
import kotlin.random.Random

class MinecraftVillageAdapter {
    companion object {
        val EMPTY_SPACE_PATTERN = IntArray2D(3, 3) { 0 }
        val templatePatterns: Array<IntArray2D> = arrayOf(
            // 1 for each corner
            Array(4) {
                intArrayOf(
                    0, 0, 0,
                    0, 0, 0,
                    0, 0, 1
                )
            },
            // 2 per each corner
            Array(8) {
                intArrayOf(
                    0, 0, 0,
                    0, 0, 0,
                    0, 1, 1
                )
            },
            // 2 per each corner
            Array(8) {
                intArrayOf(
                    0, 0, 0,
                    0, 0, 1,
                    0, 0, 1
                )
            },
            // 3 per side
            Array(12) {
                intArrayOf(
                    0, 0, 0,
                    0, 0, 0,
                    1, 1, 1
                )
            },
            // 1 per corner
            Array(4) {
                intArrayOf(
                    0, 0, 0,
                    0, 1, 1,
                    0, 1, 1
                )
            },
            // 3 per side
            Array(12) {
                intArrayOf(
                    0, 0, 0,
                    1, 1, 1,
                    1, 1, 1
                )
            },
            // We want decent size
            Array(9) {
                intArrayOf(
                    1, 1, 1,
                    1, 1, 1,
                    1, 1, 1
                )
            },
        ).flatten().map { item -> IntArray2D(3, 3) { item[it] } }.toTypedArray()

        /**
         * Buildings consist of template name, coordinates and sizes
         */
        @ExperimentalUnsignedTypes
        fun generate(
            width: Int = 200,
            height: Int = 200,
            options: MinecraftVillageAdapterOptions
        ): Iterable<Triple<String, Pair<Int, Int>, Pair<Int, Int>>>? {
            try {
//                println("creating mapping")
                val templateMapping =
                    mapOf(*(
                            options.templateOptions.keys.mapIndexed { index: Int, name: String ->
                                Pair(
                                    index + 1,
                                    name
                                )
                            }
                                .toTypedArray()
                            ))

                val sumWidths = options.templateOptions.keys.map { TemplateHolder.templates[it]?.size ?: 0 }
                val sumHeights =
                    options.templateOptions.keys.map { TemplateHolder.templates[it]?.firstOrNull()?.size ?: 0 }
                val sumAreas = sumWidths.zip(sumHeights).map { it.first * it.second }
                val totalSum = sumAreas.sum()

                // 1 for empty space
                val factors = sumAreas.map { it / totalSum }.plus(1)
                val emptySpaceWeight = options.templateOptions.size * options.emptySpaceWeight.toInt()

                // 36 / 63 spaces in patterns are empty and do not actually count.
                val fillFactor =
                    (63.0 * width * height / (
                            (totalSum + emptySpaceWeight) * 36.0 * options.desiredNumberOfHouses.toDouble()
                                .pow(5.0 / 7.0)
                            )).pow(4.0 / 3.0)
//                println("Fillfactor $fillFactor")

                println("Weights ready")

                val patterns: MutableList<IntArray2D> =
                    templateMapping.keys.map {
                        createTemplatePatterns(
                            it,
                        )
                    }.flatten().toMutableList()
                println("Patterns ready")

                val pab = PatternsArrayBuilder()
                for (pattern in patterns) {
                    addToBuilder(pab, pattern, options.modelOptions)
                }

                val patternCounts = pab.patterns.map { pair ->
                    Pair(
                        IntArray2D(3, 3) { pair.first[it] },
                        pair.second
                    )
                }.plus(Pair(EMPTY_SPACE_PATTERN, IntHolder(emptySpaceWeight)))
                patterns.add(EMPTY_SPACE_PATTERN)

                println("counts ready")
//                println(factors)

                val oneTemplate = (patternCounts.size - 1) / (options.templateOptions.size)

                val weightSum = patternCounts.sumOf { it.second.item }
                val weights =
                    DoubleArray(patternCounts.size) {
                        val modifier = if (patternCounts[it].first.all { pixel -> pixel != 0 }) fillFactor else 1.0
                        (patternCounts[it].second.item / weightSum.toDouble()).pow(options.modelOptions.weightPower) * factors[it / oneTemplate] * modifier
                    }

                println("weights ready")
//                println(weights.joinToString(" ", "[", "]"))

                val propagator = Array(4) { dir ->
                    Array(patternCounts.size) { patternIndex ->
                        val d = Direction2D.fromInt(dir)
                        patternCounts.indices.filter {
                            agrees(
                                patternCounts[patternIndex].first,
                                patternCounts[it].first,
                                d,
                                2,
                                3
                            )
                        }.toIntArray()
                    }
                }

//                println("Propagator")
//                println(propagator.joinToString("\n\t", "{\n\t", "\n}") { it.last().joinToString(", ", "[", "]") })

                val algPatterns = Patterns(patternCounts.map { it.first.asIntArray() }.toTypedArray())
                val algPixels = Pixels(
                    mapOf(
                        *algPatterns.pixels.distinct().map { pixel ->
                            Pair(
                                pixel,
                                algPatterns.pixels
                                    .mapIndexed { index, it -> Pair(it, index) }
                                    .filter { it.first == pixel }
                                    .map { it.second }
                                    .toIntArray()
                            )
                        }.toTypedArray()
                    )
                )

//                println("patterns and pixels")
//
//                println("Width $width, Height $height")
//
//                println("Patterns ${algPatterns.size}")
//                println("Weights ${weights.size}")
//
//                println(formatPatterns(algPatterns.toList().toTypedArray()))
//
//                println(formatPropagator(propagator))

                val algorithm = Cartesian2DWfcAlgorithm(
                    Cartesian2DTopology(width - 2, height - 2),
                    weights,
                    propagator,
                    algPatterns,
                    algPixels
                )

//                algorithm.beforeStart += {
//                    println("Starting")
//                }
//
//                algorithm.beforeStep += {
//                    println("Be Step")
//                }
//                algorithm.afterStep += {
//                    println("Af Step")
//                }
//                algorithm.beforeWarmup += {
//                    println("Before warmup")
//                }
//                algorithm.afterWarmup += {
//                    println("After warmup")
//                }
//                algorithm.beforeBan += {
//                    println("Before ban $it")
//                }
//                algorithm.afterFinished += {
//                    println("Finished")
//                }

//                println("alg")

                algorithm.beforeStart += {
                    if (options.modelOptions.roofed) {
                        for (x in 0 until width - 2) {
                            algorithm.setCoordinatePixel(x, 0, 0)
                        }
                    }
                    if (options.modelOptions.rightSided) {
                        val pats = algPatterns.mapIndexed { index: Int, ints: IntArray -> Pair(index, ints) }
                            .filter { it.second[2] == 0 && it.second[5] == 0 && it.second[8] == 0 }.map { it.first }
                        for (y in 0 until height - 2) {
                            algorithm.setCoordinatePatterns(width - 3, y, pats)
                        }
                    }
                    if (options.modelOptions.grounded) {
                        val pats = algPatterns.mapIndexed { index: Int, ints: IntArray -> Pair(index, ints) }
                            .filter { it.second[6] == 0 && it.second[7] == 0 && it.second[8] == 0 }.map { it.first }
                        for (x in 0 until width - 2) {
                            algorithm.setCoordinatePatterns(x, height - 3, pats)
                        }
                    }
                    if (options.modelOptions.leftSided) {
                        for (y in 0 until height - 2) {
                            algorithm.setCoordinatePixel(0, y, 0)
                        }
                    }
                }

                println("Computing")
                val result = algorithm.run(options.seed ?: Random.nextInt())

                if (!result) {
                    println("Failed!")
                    return null
                }

//                println("result")

                val output =
                    constructNullableOutput(width, height, algorithm, patternCounts.map { it.first }, algPatterns)

//                println("output")

//                printGrid(output)

                return Iterable {
                    iterator {
                        val used = Array(output.size) {
                            Array(output[it].size) {
                                false
                            }
                        }

                        for (x in output.indices) {
                            for (y in output[x].indices) {
                                if (output[x][y] == 0 || used[x][y]) continue

                                var houseWidth = 0
                                while (x + houseWidth < output.size && output[x + houseWidth][y] == output[x][y]) {
                                    houseWidth++
                                }

                                var houseHeight = 0
                                while (y + houseHeight < output[x].size && output[x + houseHeight][y] == output[x][y]) {
                                    houseHeight++
                                }

                                for (w in 0 until houseWidth) {
                                    for (h in 0 until houseHeight) {
                                        used[x + w][y + h] = true
                                    }
                                }

                                val name =
                                    options.templateOptions.filter { it.value.second.first >= houseWidth && it.value.second.third >= houseHeight }.keys.random()

                                yield(
                                    Triple(
                                        name,
                                        Pair(x, y),
                                        Pair(houseWidth, houseHeight)
                                    )
                                )
                            }
                        }
                    }
                }
            } catch (error: Exception) {
                println("error")
                println(error)
                println(error.stackTrace.joinToString("\n\t"))
                throw error
            }
        }

        /**
         * Checks whether two overlapping patterns agree
         */
        fun agrees(
            pattern1: IntArray2D,
            pattern2: IntArray2D,
            direction: Direction2D,
            overlap: Int,
            patternSideSize: Int,
        ): Boolean {
            val line1 = when (direction) {
                Direction2D.NORTH -> pattern1.rows(0 until overlap).iterator().asSequence()
                    .chain(overlap, patternSideSize)
                Direction2D.EAST -> pattern1.columns((patternSideSize - overlap) until patternSideSize).iterator()
                    .asSequence()
                    .chain(overlap, patternSideSize)
                Direction2D.SOUTH -> pattern1.rows((patternSideSize - overlap) until patternSideSize).iterator()
                    .asSequence()
                    .chain(overlap, patternSideSize)
                Direction2D.WEST -> pattern1.columns(0 until overlap).iterator().asSequence()
                    .chain(overlap, patternSideSize)
            }
            val line2 = when (direction) {
                Direction2D.NORTH -> pattern2.rows((patternSideSize - overlap) until patternSideSize).iterator()
                    .asSequence()
                    .chain(overlap, patternSideSize)
                Direction2D.EAST -> pattern2.columns(0 until overlap).iterator().asSequence()
                    .chain(overlap, patternSideSize)
                Direction2D.SOUTH -> pattern2.rows(0 until overlap).iterator().asSequence()
                    .chain(overlap, patternSideSize)
                Direction2D.WEST -> pattern2.columns((patternSideSize - overlap) until patternSideSize).iterator()
                    .asSequence()
                    .chain(overlap, patternSideSize)
            }
            return line1.contentEquals(line2)
        }

        private fun addToBuilder(
            pab: PatternsArrayBuilder,
            pattern: IntArray2D,
            options: Cartesian2DModelOptions
        ) {
            val foundPatterns = mutableListOf(pattern)

            if (options.allowHorizontalFlips || options.allowVerticalFlips) {
                if (options.allowHorizontalFlips) {
                    val patternH = pattern.hFlipped()
                    foundPatterns.add(patternH)

                    if (options.allowVerticalFlips) {
                        val patternHV = patternH.vFlipped()
                        foundPatterns.add(patternHV)
                    }
                }
                if (options.allowVerticalFlips) {
                    val patternV = pattern.vFlipped()
                    foundPatterns.add(patternV)
                }
            }

            if (options.allowRotations) {
                val pattern90 = pattern.rotated()
                val pattern180 = pattern90.rotated()
                val pattern270 = pattern180.rotated()

                foundPatterns.addAll(
                    sequenceOf(
                        pattern90,
                        pattern180,
                        pattern270,
                    )
                )

                if (options.allowHorizontalFlips || options.allowVerticalFlips) {
                    if (options.allowHorizontalFlips) {
                        val pattern90H = pattern90.hFlipped()
                        val pattern180H = pattern180.hFlipped()
                        val pattern270H = pattern270.hFlipped()
                        foundPatterns.addAll(
                            sequenceOf(
                                pattern90H,
                                pattern180H,
                                pattern270H,
                            )
                        )

                        if (options.allowVerticalFlips) {
                            val pattern90HV = pattern90H.vFlipped()
                            val pattern180HV = pattern180H.vFlipped()
                            val pattern270HV = pattern270H.vFlipped()
                            foundPatterns.addAll(
                                sequenceOf(
                                    pattern90HV,
                                    pattern180HV,
                                    pattern270HV,
                                )
                            )
                        }
                    }
                    if (options.allowVerticalFlips) {
                        val pattern90V = pattern90.vFlipped()
                        val pattern180V = pattern180.vFlipped()
                        val pattern270V = pattern270.vFlipped()
                        foundPatterns.addAll(
                            sequenceOf(
                                pattern90V,
                                pattern180V,
                                pattern270V,
                            )
                        )
                    }
                }
            }

            foundPatterns.forEach {
                pab.add(it.asIntArray())
            }
        }

        private fun createTemplatePatterns(
            index: Int,
        ): Iterable<IntArray2D> {
            return templatePatterns.map { pattern -> IntArray2D(3, 3) { pattern[it] * index } }.toList()
        }

        private fun onBoundary(waveIndex: Int, width: Int, height: Int, overlap: Int): Boolean {
            if (waveIndex % width >= width - overlap) {
                return true
            }
            if (waveIndex >= width * (height - overlap)) {
                return true
            }
            return false
        }

        /**
         * Shifts wave index from output coordinates to algorithm coordinates and an optional shift which represents index in the pattern (for boundary pixels).
         */
        private fun shiftOutputWave(wave: Int, width: Int, height: Int, overlap: Int): Pair<Int, Int> {
            var index = wave
            var shiftX = 0
            var shiftY = 0
            val outputSizes = intArrayOf(width, height)

            if (onBoundary(wave, width, height, overlap)) {
                val coordinates = wave.toCoordinates(outputSizes)

                if (coordinates[0] >= width - overlap) {
                    shiftX = coordinates[0] - width + overlap + 1
                    index -= shiftX
                }
                if (coordinates[1] >= height - overlap) {
                    shiftY = coordinates[1] - height + overlap + 1
                    index -= shiftY * width
                }
            }

            index -= (index / width) * overlap

            val shift = shiftY * (overlap + 1) + shiftX

            return Pair(index, shift)
        }

        /**
         * Uses Int.MIN_VALUE for pixels without any feasible pattern
         */
        @ExperimentalUnsignedTypes
        private fun constructNullableOutput(
            width: Int,
            height: Int,
            algorithm: Cartesian2DWfcAlgorithm,
            patternsArray: List<IntArray2D>,
            patterns: Patterns,
        ): Array<Array<Int?>> {
//            println("PA ${patternsArray.size}")
//            println("P ${patterns.size}")

            if (!algorithm.hasRun) {
                println("WARNING: Algorithm hasn't run yet.")
            }
            val outputSizes = intArrayOf(width, height)

            return Array(width) { x ->
                Array(height) { y ->
                    val waveIndex = intArrayOf(x, y).toIndex(outputSizes)
                    val pair = shiftOutputWave(waveIndex, width, height, 2)
                    val index = pair.first
                    val shift = pair.second

                    val a = 0
                    val b = 1
                    val sum = algorithm.waves[index].sumOf {
                        when (it) {
                            false -> a
                            true -> b
                        }
                    }
                    when (sum) {
                        0 -> Int.MIN_VALUE
                        1 -> patternsArray[patterns.indices.filter { algorithm.waves[index, it] }[0]][shift]
                        else -> null
                    }
                }
            }
        }

        fun printGrid(grid: Array<Array<Int?>>) {
            for (y in 0 until grid[0].size) {
                for (x in 0 until grid.size) {
                    print("${grid[x][y] ?: "X"} ")
                }
                println()
            }
        }
    }
}