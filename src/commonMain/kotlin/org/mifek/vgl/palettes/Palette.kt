package org.mifek.vgl.palettes

import org.mifek.vgl.implementations.Block

@ExperimentalUnsignedTypes
open class Palette(m: MutableMap<out PaletteKeys, out Iterable<Block>>?) : HashMap<PaletteKeys, Iterable<Block>>(m)