package org.mifek.vgl.utils

import org.mifek.vgl.implementations.Block
import org.mifek.wfc.datastructures.IntArray2D
import java.util.*
import kotlin.collections.HashMap

expect object TemplateHolder {
    val templates: HashMap<String, Array<Array<Array<Block>>>>
    val floorPlans: HashMap<String, Array<Array<Block>>>
    fun saveTemplate(template: Array<Array<Array<Block>>>, name: String = "template_${Date().time}.tmpl"): String
}