package org.mifek.vgl.utilities

import org.mifek.vgl.implementations.Block


expect object TemplateHolder {
    val templates: HashMap<String,Array<Array<Array<Block>>>>
}