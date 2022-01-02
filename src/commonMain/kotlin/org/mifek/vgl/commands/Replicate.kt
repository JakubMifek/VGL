package org.mifek.vgl.commands

import org.mifek.vgl.implementations.Block
import org.mifek.vgl.implementations.PlacementStyle
import org.mifek.vgl.interfaces.IArea
import org.mifek.vgl.utils.TemplateHolder
import org.mifek.vgl.wfc.MinecraftWfcAdapter
import org.mifek.vgl.wfc.MinecraftWfcAdapterOptions
import org.mifek.vgl.wfc.StreamOptions
import org.mifek.wfc.datatypes.Direction3D
import org.mifek.wfc.models.options.Cartesian3DModelOptions

@ExperimentalUnsignedTypes
class Replicate {
    fun execute(
        templateName: String,
        area: IArea,
        streamOptions: StreamOptions
    ): Array<Array<Array<Block>>>? {
        val template = TemplateHolder.templates[templateName] ?: throw Error("Could not find template $templateName.")

        val setBlocks =
            Iterable {
                iterator {
                    yield(
                        Pair(
                            Triple(area.width / 2, 0, area.depth / 2),
                            template[template.size / 2][0][template[0][0].size / 2]
                        )
                    )
                }
            }

        val setPlanes = HashSet<Direction3D>()
        setPlanes.add(Direction3D.UP)
        setPlanes.add(Direction3D.FORWARD)
        setPlanes.add(Direction3D.RIGHT)
        setPlanes.add(Direction3D.DOWN)
        setPlanes.add(Direction3D.BACKWARD)
        setPlanes.add(Direction3D.LEFT)

        return MinecraftWfcAdapter.imitate(
            template,
            Triple(area.width, area.height, area.depth),
            MinecraftWfcAdapterOptions(
                2,
                setBlocks,
                Cartesian3DModelOptions(
                    allowXRotations = false,
                    allowYRotations = true,
                    allowZRotations = false,
                    allowXFlips = true,
                    allowYFlips = false,
                    allowZFlips = true,
                    setPlanes = setPlanes,
                    banPlanesElsewhere = emptySet(),
                    periodicOutput = false,
                    periodicInput = false,
                    weightPower = 0.333333
                ),
                null,
                1,
                streamOptions,
                templateName
            ),
        )
    }
}