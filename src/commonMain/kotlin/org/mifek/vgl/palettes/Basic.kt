package org.mifek.vgl.palettes

import org.mifek.vgl.implementations.Block
import org.mifek.vgl.implementations.Blocks

@ExperimentalUnsignedTypes
sealed class Basic : Palette(
    hashMapOf(
        Pair(PaletteKeys.WALL, Block(Blocks.STONE, hashMapOf(Pair("variant", "STONE")))),
        Pair(PaletteKeys.DOORS, Block(Blocks.OAK_DOOR)),
        Pair(PaletteKeys.GROUND, Block(Blocks.GRASS)),
        Pair(PaletteKeys.FLOOR, Block(Blocks.OAK_WOOD_PLANK, hashMapOf(Pair("variant", "DARK_OAK")))),
    )
)
