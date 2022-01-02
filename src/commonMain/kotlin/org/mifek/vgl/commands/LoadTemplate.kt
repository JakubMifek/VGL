package org.mifek.vgl.commands

import org.mifek.vgl.implementations.PlacedBlock
import org.mifek.vgl.utils.TemplateHolder
import org.mifek.vgl.wfc.StreamOptions

class LoadTemplate {
    fun execute(name: String, streamOptions: StreamOptions) {
        val template = TemplateHolder.templates[name]
        if (template === null) return

        for (z in template.indices) {
            for (y in template[z].indices) {
                for (x in template[z][y].indices) {
                    streamOptions.stream.add(
                        PlacedBlock(
                            x + streamOptions.area.x,
                            y + streamOptions.area.y,
                            z + streamOptions.area.z,
                            template[z][y][x].block,
                            template[z][y][x].props
                        )
                    )
                }
            }
        }
    }
}