package org.mifek.vgl.commands

import org.mifek.vgl.implementations.Block
import org.mifek.vgl.interfaces.IArea
import org.mifek.vgl.utils.TemplateHolder
import org.mifek.vgl.wfc.MinecraftWfcAdapter
import org.mifek.vgl.wfc.MinecraftWfcAdapterOptions
import javax.xml.transform.sax.TemplatesHandler

@ExperimentalUnsignedTypes
class Generate {
    fun execute(
        templateName: String,
        area: IArea,
        options: MinecraftWfcAdapterOptions
    ): Array<Array<Array<Block>>>? {
        val template = TemplateHolder.templates[templateName] ?: throw Error("Could not find template $templateName.")
        return MinecraftWfcAdapter.imitate(
            template,
            Triple(area.width, area.height, area.depth),
            options,
        )
    }
}