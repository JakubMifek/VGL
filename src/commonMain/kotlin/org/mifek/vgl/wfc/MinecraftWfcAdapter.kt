package org.mifek.vgl.wfc

import org.mifek.vgl.implementations.Block
import org.mifek.vgl.implementations.Blocks
import org.mifek.vgl.implementations.PlacedBlock
import org.mifek.vgl.implementations.PlacementStyle
import org.mifek.vgl.utils.toBlockData
import org.mifek.vgl.utils.toIntArray3D
import org.mifek.wfc.datastructures.IntArray3D
import org.mifek.wfc.models.OverlappingCartesian3DModel
import org.mifek.wfc.utils.toCoordinates
import kotlin.random.Random

@Suppress("TYPE_INFERENCE_ONLY_INPUT_TYPES_WARNING")
class MinecraftWfcAdapter {
    companion object {
        @ExperimentalUnsignedTypes
        fun imitate(
            template: Array<Array<Array<Block>>>,
            outputDimensions: Triple<Int, Int, Int>,
            options: MinecraftWfcAdapterOptions
        ): Array<Array<Array<Block>>>? {
            val dataMapping = template.toIntArray3D()
            val model = OverlappingCartesian3DModel(
                dataMapping.first,
                options.overlap,
                outputDimensions.first,
                outputDimensions.second,
                outputDimensions.third,
                options.modelOptions
            )
            if (options.fixedBlocks != null) {
                options.fixedBlocks.forEach {
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

            val algorithm = model.build()

            if (options.streamOptions != null) {
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
                    /*algorithm.afterObserve += {
                        println("observed ${it.third} on ${it.second}")
                    }

                    var i = 0
                    algorithm.afterBan += {
                        if(++i % 10 == 0) {
                            try {
                                println("Ban")
//                            val output =
//                                model.constructNullableOutput(algorithm).toBlockData(pair.second, options.debugOptions)
                                val output = algorithm.constructOutput()
                                for (z in 0 until output.depth) {
                                    for (y in 0 until output.height) {
                                        for (x in 0 until output.width) {
                                            print("${output[x, y, z]} ")
                                        }
                                        println()
                                    }
                                    println("\n---\n")
                                }

//                            println(output.joinToString("\n\n\n") { it.joinToString("\n") { it.joinToString(", ") { it.serialize() } } })
                                println("\n\n--------------------------------------------------------------------------------------------\n\n")
                            } catch (error: Error) {
                                println(error)
                            }
                        }
                    }*/

                    algorithm.afterObserve += {
                        try {
                            println("Observe")
                            val output =
                                model.constructNullableOutput(algorithm)
                                    .toBlockData(dataMapping.second, options.debugOptions)
                            println(output.joinToString("\n\n\n") { it.joinToString("\n") { it.joinToString(", ") { it.serialize() } } })
                            println("\n\n--------------------------------------------------------------------------------------------\n\n")
                        } catch (error: Error) {
                            println(error)
                        }
                    }


                    algorithm.afterClear += {
                        try {
                            println("Clear")
                            val output =
                                model.constructNullableOutput(algorithm)
                                    .toBlockData(dataMapping.second, options.debugOptions)
                            println(output.joinToString("\n\n\n") { it.joinToString("\n") { it.joinToString(", ") { it.serialize() } } })
                            println("\n\n--------------------------------------------------------------------------------------------\n\n")
                        } catch (error: Error) {
                            println(error)
                        }
                    }


                    algorithm.beforeStart += {
                        try {
                            println("Start")
                            val output =
                                model.constructNullableOutput(algorithm)
                                    .toBlockData(dataMapping.second, options.debugOptions)
                            println(output.joinToString("\n\n\n") { it.joinToString("\n") { it.joinToString(", ") { it.serialize() } } })
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
                            println(output.joinToString("\n\n\n") { it.joinToString("\n") { it.joinToString(", ") { it.serialize() } } })
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
                            println(output.joinToString("\n\n\n") { it.joinToString("\n") { it.joinToString(", ") { it.serialize() } } })
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

            return model.constructNullableOutput(algorithm).toBlockData(dataMapping.second, options.debugOptions)
        }
    }
}