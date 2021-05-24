package org.mifek.vgl.utils

import org.mifek.vgl.implementations.Block
import java.util.*

expect object TemplateHolder {
    val templates: HashMap<String, Array<Array<Array<Block>>>>

    fun saveTemplate(template: Array<Array<Array<Block>>>, name: String = "template_${Date().time}.tmpl"): String
}