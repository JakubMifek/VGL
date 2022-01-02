package org.mifek.vgl

import org.mifek.vgl.implementations.Block
import org.mifek.vgl.implementations.Blocks


const val VALUE_SEPARATOR = ','
const val PROPS_SEPARATOR = ':'
val hashRegex = Regex("^(-?\\d+)?($VALUE_SEPARATOR\\w+$PROPS_SEPARATOR\\w+)*$")

val DOORS = setOf(
    Blocks.OAK_DOOR,
    Blocks.ACACIA_DOOR,
    Blocks.BIRCH_DOOR,
    Blocks.DARK_OAK_DOOR,
    Blocks.IRON_DOOR,
    Blocks.JUNGLE_DOOR,
    Blocks.SPRUCE_DOOR,
)

val TRANSPARENT = setOf(
    Blocks.AIR,
//            Blocks.IRON_TRAPDOOR,
//            Blocks.WOODEN_TRAPDOOR
).plus(DOORS)

val AIR_BLOCK = Block(Blocks.AIR)

val BREAKABLE = setOf(
    Blocks.TORCH,
    Blocks.GRASS,
    Blocks.RAIL,
    Blocks.ACTIVATOR_RAIL,
    Blocks.DETECTOR_RAIL,
    Blocks.POWERED_RAIL,
    Blocks.CACTUS,
    Blocks.BED
    // TODO: Finish
).plus(DOORS)
