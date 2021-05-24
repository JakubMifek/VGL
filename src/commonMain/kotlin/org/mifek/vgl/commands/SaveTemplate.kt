package org.mifek.vgl.commands

import org.mifek.vgl.implementations.Block
import org.mifek.vgl.utils.TemplateHolder

class SaveTemplate {
    fun execute(blocks: Array<Array<Array<Block>>>, name: String?): String {
        if (name == null) return TemplateHolder.saveTemplate(blocks)

        return TemplateHolder.saveTemplate(blocks, name)
    }
}