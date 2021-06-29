package org.mifek.vgl.wfc

import org.mifek.vgl.implementations.*
import org.mifek.vgl.utils.toBlockData
import org.mifek.vgl.utils.toIntArray3D
import org.mifek.wfc.datastructures.IntArray3D
import org.mifek.wfc.models.OverlappingCartesian3DModel
import org.mifek.wfc.models.options.Cartesian3DModelOptions
import org.mifek.wfc.models.storage.PatternWeights3D
import org.mifek.wfc.utils.toCoordinates
import kotlin.random.Random

@Suppress("TYPE_INFERENCE_ONLY_INPUT_TYPES_WARNING")
@ExperimentalUnsignedTypes
class MinecraftWfcAdapter {
    companion object {
        private val cache = HashMap<String, PatternWeights3D>()

        private fun serializeOptions(options: Cartesian3DModelOptions): String {
            println(options.toString())
            return "=[^=]*(=|$)".toRegex().findAll(
                options.toString()
                    .substringAfter('(')
                    .substringBefore(')')
            ).joinToString("") { it.value.substringBeforeLast(',').substring(1) }
        }

        fun imitate(
            template: Array<Array<Array<Block>>>,
            outputDimensions: Triple<Int, Int, Int>,
            options: MinecraftWfcAdapterOptions,
        ): Array<Array<Array<Block>>>? {
            println("Creating mapping...")
            val dataMapping = template.toIntArray3D()

            println("Mapping created.")
            var patternWeights: PatternWeights3D? = null
            val key = "${options.name}_${options.overlap}_${serializeOptions(options.modelOptions)}"

            // Cache retrieval
            if (options.name != null && cache.containsKey(key)) {
                println("Using stored $key")
                patternWeights = cache[key]
            }

            println("Creating model...")
            // Use cache if possible
            val model = if (patternWeights != null) OverlappingCartesian3DModel(
                patternWeights,
                outputDimensions.first,
                outputDimensions.second,
                outputDimensions.third,
            ) else OverlappingCartesian3DModel(
                dataMapping.first,
                options.overlap,
                outputDimensions.first,
                outputDimensions.second,
                outputDimensions.third,
                options.modelOptions
            )

            // Store to cache if not already present
            if (options.name != null && patternWeights == null) {
                println("Storing $key")
                cache[key] = model.storage
            }

            if (options.setBlocks != null) {
                println("Setting blocks...")
                options.setBlocks.forEach {
                    val coordinates = it.first
                    val value = dataMapping.third[it.second.serialize()]
                        ?: throw Error("Fixed block was not in the template palette.")

                    model.setPixel(
                        coordinates.first,
                        coordinates.second,
                        coordinates.third,
                        value
                    )
                }
            }

            println("Creating algorithm...")
            val algorithm = model.build()

            if (options.streamOptions != null) {
                println("Setting up stream...")
                if (options.streamOptions.placementStyle == PlacementStyle.ON_COLLAPSE) {
                    algorithm.afterCollapse += { triple ->
                        try {
                            val outputCoordinates =
                                model.shiftAlgorithmWave(triple.second).toCoordinates(model.outputSizes)
//                            println("collapsed ${triple.second}")
//                            println("shifted to ${outputCoordinates.joinToString(", ", "[", "]")}")
                            val pattern = IntArray3D(
                                model.overlap + 1,
                                model.overlap + 1,
                                model.overlap + 1
                            ) { model.patterns[triple.third][it] }

                            val blocks =
                                Array(pattern.width) { x -> Array(pattern.height) { y -> Array<Int?>(pattern.depth) { z -> pattern[x, y, z] } } }
                                    .toBlockData(
                                        dataMapping.second,
                                        options.debugOptions
                                    )


                            for (x in 0 until pattern.width) {
                                for (y in 0 until pattern.height) {
                                    for (z in 0 until pattern.depth) {
                                        val block = blocks[x][y][z]
                                        if (block.block == Blocks.NONE) {
                                            continue
                                        }

//                                        println(
//                                            "Adding block ${block.block.name} to ${
//                                                intArrayOf(
//                                                    options.streamOptions.area.x + /*options.streamOptions.area.width - 2 -*/ (x + outputCoordinates[0]),
//                                                    options.streamOptions.area.y + (y + outputCoordinates[1]),
//                                                    options.streamOptions.area.z + (z + outputCoordinates[2]),
//                                                ).joinToString(", ", "[", "]")
//                                            }"
//                                        )

                                        options.streamOptions.stream.add(
                                            PlacedBlock(
                                                options.streamOptions.area.x + /*options.streamOptions.area.width - 2 -*/ (x + outputCoordinates[0]),
                                                options.streamOptions.area.y + (y + outputCoordinates[1]),
                                                options.streamOptions.area.z + (z + outputCoordinates[2]),
                                                block.block,
                                                block.props
                                            )
                                        )
                                    }
                                }
                            }
                        } catch (error: Error) {
                            println(error)
                        }
                    }
                }

                var step = 0
                if (options.streamOptions.placementStyle == PlacementStyle.EACH_STEP) {
                    algorithm.afterStep += {
                        try {
                            if (++step % 10 == 0) {
                                val output =
                                    model.constructNullableOutput(algorithm)
                                        .toBlockData(dataMapping.second, options.debugOptions)

                                for (x in output.indices) for (y in output[x].indices) for (z in output[x][y].indices) {
                                    val block: Block = output[x][y][z]
                                    if (block.block == Blocks.NONE ||
                                        (options.debugOptions != null && block.block == options.debugOptions.undeterminedBlock ||
                                                options.debugOptions == null && block.block == Blocks.BEACON)
                                    ) {
                                        continue
                                    }

                                    options.streamOptions.stream.add(
                                        PlacedBlock(
                                            options.streamOptions.area.x + options.streamOptions.area.width - 2 - x,
                                            options.streamOptions.area.y + y,
                                            options.streamOptions.area.z - z,
                                            block.block,
                                            block.props
                                        )
                                    )
                                }
                            }
                        } catch (error: Error) {
                            println(error)
                        }
                    }
                }

                if (options.streamOptions.placementStyle == PlacementStyle.EACH_STEP ||
                    options.streamOptions.placementStyle == PlacementStyle.ON_FINISH
                ) {
                    algorithm.afterFinished += {
                        try {
                            val output =
                                model.constructNullableOutput(algorithm)
                                    .toBlockData(dataMapping.second, options.debugOptions)

                            for (x in output.indices) for (y in output[x].indices) for (z in output[x][y].indices) {
                                var block: Block = output[x][y][z]
                                if (block.block == Blocks.NONE) {
                                    continue
                                }

                                options.streamOptions.stream.add(
                                    PlacedBlock(
                                        options.streamOptions.area.x + options.streamOptions.area.width - 2 - x,
                                        options.streamOptions.area.y + y,
                                        options.streamOptions.area.z - z,
                                        block.block,
                                        block.props
                                    )
                                )
                            }
                        } catch (error: Error) {
                            println(error)
                        }
                    }
                }
            }

            if (options.debugOptions != null) {
                if (options.debugOptions.verbose) {
                    println("Setting up debug outputs...")
//                    algorithm.afterObserve += {
//                        println("observed ${it.third} on ${it.second}")
//                    }

                    var i = 0
                    algorithm.afterBan += {
                        i++
                        if (i > 200) {
                            i--
                            try {
                                println("Ban")
                                val output =
                                    model.constructNullableOutput(algorithm)
//                                        .toBlockData(dataMapping.second, options.debugOptions)
                                println(output.joinToString("\n\n\n") {
                                    it.reversed()
                                        .joinToString("\n") { it.joinToString(", ") { it.toString() /*.serialize()*/ } }
                                })
                                println("\n\n--------------------------------------------------------------------------------------------\n\n")
                            } catch (error: Error) {
                                println(error)
                            }
                        }
                    }

//                    algorithm.afterObserve += {
//                        try {
//                            println("Observe")
//                            val output =
//                                model.constructNullableOutput(algorithm)
//                                    .toBlockData(dataMapping.second, options.debugOptions)
//                            println(output.joinToString("\n\n\n") { it.joinToString("\n") { it.joinToString(", ") { it.serialize() } } })
//                            println("\n\n--------------------------------------------------------------------------------------------\n\n")
//                        } catch (error: Error) {
//                            println(error)
//                        }
//                    }


//                    algorithm.afterClear += {
//                        try {
//                            println("Clear")
//                            val output =
//                                model.constructNullableOutput(algorithm)
//                                    .toBlockData(dataMapping.second, options.debugOptions)
//                            println(output.joinToString("\n\n\n") { it.joinToString("\n") { it.joinToString(", ") { it.serialize() } } })
//                            println("\n\n--------------------------------------------------------------------------------------------\n\n")
//                        } catch (error: Error) {
//                            println(error)
//                        }
//                    }


                    algorithm.beforeStart += {
                        try {
                            println("Start")
                            val output =
                                model.constructNullableOutput(algorithm)
                                    .toBlockData(dataMapping.second, options.debugOptions)
                            println(output.joinToString("\n\n\n") {
                                it.reversed().joinToString("\n") { it.joinToString(", ") { it.serialize() } }
                            })
                            println("\n\n--------------------------------------------------------------------------------------------\n\n")
                        } catch (error: Error) {
                            println(error)
                        }
                    }

                    algorithm.beforePropagation += {
                        try {
                            println("Before")
                            val output =
                                model.constructNullableOutput(algorithm)
                                    .toBlockData(dataMapping.second, options.debugOptions)
                            println(output.joinToString("\n\n\n") {
                                it.reversed().joinToString("\n") { it.joinToString(", ") { it.serialize() } }
                            })
                            println("\n\n--------------------------------------------------------------------------------------------\n\n")
                        } catch (error: Error) {
                            println(error)
                        }
                    }

                    algorithm.afterPropagation += {
                        try {
                            println("After")
                            val output =
                                model.constructNullableOutput(algorithm)
                                    .toBlockData(dataMapping.second, options.debugOptions)
                            println(output.joinToString("\n\n\n") {
                                it.reversed().joinToString("\n") { it.joinToString(", ") { it.serialize() } }
                            })
                            println("\n\n--------------------------------------------------------------------------------------------\n\n")
                        } catch (error: Error) {
                            println(error)
                        }
                    }
                    /*algorithm.afterStep += {
                        val output =
                            model.constructNullableOutput(algorithm).toBlockData(pair.second, options.debugOptions)
                        println(output.joinToString("\n\n\n") { it.joinToString("\n") { it.joinToString(", ") { it.serialize() } } })
                        println("\n\n--------------------------------------------------------------------------------------------\n\n")
                    }*/
                }
            }

            val random = Random(options.debugOptions?.seed ?: Random.Default.nextInt())
            var failed = false

            algorithm.afterFail += {
                println("Failed")
                failed = true
            }

            var result = false
            for (i in 0..options.repeats) {
                val seed = random.nextInt()
                println("---------")
                println("Try ${i + 1}; seed = $seed")
                println("---------")
                failed = false
                result = algorithm.run(seed)
                if (result) break
            }

            if (!failed && result) {
                println("Succeeded")
            } else {
                println("Algorithm was unsuccessful.")
                return null
            }

            if (options.streamOptions != null) return null

            return model.constructNullableOutput(algorithm).toBlockData(dataMapping.second, options.debugOptions)
        }
    }
}