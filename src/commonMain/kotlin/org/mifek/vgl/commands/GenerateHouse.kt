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
        options: MinecraftWfcAdapterOptions
    ): Array<Array<Array<Block>>>? {
        return null
    }
}