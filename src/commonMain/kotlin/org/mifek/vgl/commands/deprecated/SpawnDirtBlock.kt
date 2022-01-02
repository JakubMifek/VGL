//package org.mifek.vgl.commands
//
//import org.mifek.vgl.implementations.Blocks
//import org.mifek.vgl.interfaces.IArea
//
//class SpawnDirtBlock {
//    fun execute(area: IArea): Array<Array<Array<Int>>> {
//        return Array(area.width) { Array(area.height) { Array(area.depth) { Blocks.DIRT.id } } }
//    }
//}