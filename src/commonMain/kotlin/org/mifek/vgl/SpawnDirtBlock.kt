package org.mifek.vgl

import org.mifek.vgl.implementations.Blocks
import org.mifek.vgl.interfaces.IArea
import org.mifek.vgl.utilities.TemplateHolder

class SpawnDirtBlock {
    fun execute(area: IArea): Array<Array<Array<String>>> {
        return Array(area.width) { _ -> Array(area.height) { _ -> Array(area.depth) { _ -> Blocks.DIRT.id } } }
    }
}