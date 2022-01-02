package org.mifek.vgl.commands

import org.mifek.vgl.BREAKABLE
import org.mifek.vgl.implementations.*
import org.mifek.vgl.interfaces.IArea
import org.mifek.vgl.interfaces.IBlockStream
import org.mifek.vgl.wfc.MinecraftWfcAdapterOptions
import org.mifek.vgl.wfc.StreamOptions
import org.mifek.wfc.datatypes.Direction3D
import org.mifek.wfc.models.options.Cartesian3DModelOptions

@ExperimentalUnsignedTypes
open class GenerateRunnable(
    private val name: String,
    private val area: Area,
    private val stream: IBlockStream?
) {
    companion object {
        private val generate: Generate = Generate()
    }

    var res: Array<Array<Array<PlacedBlock?>>>? = null

    fun call(): Array<Array<Array<PlacedBlock?>>> {
        return try {
            var locRes: Array<Array<Array<PlacedBlock>>>? = generate.execute(
                name, area, MinecraftWfcAdapterOptions(
                    overlap = 2,
                    modelOptions = Cartesian3DModelOptions(
                        allowXRotations = false,
                        allowYRotations = true,
                        allowZRotations = false,
                        setPlanes = setOf(
                            Direction3D.UP,
                            Direction3D.FORWARD,
                            Direction3D.BACKWARD,
                            Direction3D.LEFT,
                            Direction3D.RIGHT
                        ),
                        allowXFlips = true,
                        allowYFlips = false,
                        allowZFlips = true,
                        weightPower = 1.0 / 3.0
                    ),
//                    setBlocks = arrayOf(Pair(Triple(0,0,0), Blocks.)),
                    repeats = 1,
                    streamOptions = if (stream != null) StreamOptions(
                        stream,
                        area,
                        PlacementStyle.ON_COLLAPSE
                    ) else null,
                    name = name
                )
            )?.mapIndexed { x, plane ->
                plane.mapIndexed { y, row ->
                    row.mapIndexed { z, it ->
                        PlacedBlock(area.x + x, area.y + y, area.z + z, it.block, it.props)
                    }.toTypedArray()
                }.toTypedArray()
            }?.toTypedArray()
                ?: return Array(0) {
                    Array(0) {
                        arrayOfNulls(
                            0
                        )
                    }
                }

            @Suppress("UNCHECKED_CAST")
            res = locRes as Array<Array<Array<PlacedBlock?>>>

            if (stream != null) streamHouse(stream, locRes)

            locRes
        } catch (error: Error) {
            Array(0) {
                Array(0) {
                    arrayOfNulls(
                        0
                    )
                }
            }
        }
    }

    private fun streamHouse(stream: IBlockStream, house: Array<Array<Array<PlacedBlock>>>) {
        if (house.isEmpty()) return

        streamBlocks(
            stream,
            Area(house[0][0][0].x, house[0][0][0].y, house[0][0][0].z, house.size, house[0].size, house[0][0].size),
            house, noFilter = BREAKABLE
        )
        streamBlocks(
            stream,
            Area(house[0][0][0].x, house[0][0][0].y, house[0][0][0].z, house.size, house[0].size, house[0][0].size),
            house, yesFilter = BREAKABLE
        )
    }

    private fun streamBlocks(
        stream: IBlockStream,
        area: IArea,
        blocks: Array<Array<Array<PlacedBlock>>>,
        yesFilter: Set<Blocks>? = null,
        noFilter: Set<Blocks>? = null
    ) {
        for (z in 0 until blocks[0][0].size) {
            for (y in 0 until blocks[0].size) {
                for (x in blocks.indices) {
                    val block = blocks[x][y][z]
                    if (yesFilter != null && block.block !in yesFilter) continue
                    if (noFilter != null && block.block in noFilter) continue

                    stream.add(
                        PlacedBlock(
                            area.x + x,
                            area.y + y,
                            area.z + z,
                            block.block,
                            block.props
                        )
                    )
                }
            }
        }
    }
}