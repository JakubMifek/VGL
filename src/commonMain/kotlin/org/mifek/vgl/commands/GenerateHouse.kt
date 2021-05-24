package org.mifek.vgl.commands

import org.mifek.vgl.implementations.Block
import org.mifek.vgl.implementations.Blocks
import org.mifek.vgl.interfaces.IArea
import org.mifek.vgl.interfaces.IBlockStream
import org.mifek.vgl.utils.TemplateHolder
import org.mifek.vgl.wfc.DebugOptions
import org.mifek.vgl.wfc.MinecraftWfcAdapter
import org.mifek.vgl.wfc.MinecraftWfcAdapterOptions
import org.mifek.vgl.wfc.StreamOptions
import org.mifek.wfc.models.options.Cartesian3DModelOptions

@ExperimentalUnsignedTypes
class GenerateHouse {
    fun execute(
        area: IArea,
        stream: IBlockStream,
        debugOptions: DebugOptions = DebugOptions()
    ): Array<Array<Array<Block>>>? {
        val house = TemplateHolder.templates["house"]!!
        return MinecraftWfcAdapter.imitate(
            house, Triple(area.width, area.height, area.depth),
            MinecraftWfcAdapterOptions(
                repeats = 10,
                modelOptions = Cartesian3DModelOptions(allowXFlips = true, allowZFlips = true),
                streamOptions = StreamOptions(stream, area),
                fixedBlocks = sequence {
                    yield(
                        Pair(
                            Triple(1, 0, 1),
                            Block(Blocks.OAK_WOOD)
                        )
                    )
                    yield(
                        Pair(
                            Triple(8, 0, 1),
                            Block(Blocks.OAK_WOOD)
                        )
                    )
                }.asIterable(),
                debugOptions = debugOptions
            ),
        )
    }
}