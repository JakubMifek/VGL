package org.mifek.vgl

import org.mifek.vgl.implementations.Blocks


const val VALUE_SEPARATOR = ','
const val PROPS_SEPARATOR = ':'
val hashRegex = Regex("^(-?\\d+)?($VALUE_SEPARATOR\\w+$PROPS_SEPARATOR\\w+)*$")

//val HOUSE =
//        arrayOf(
//                arrayOf(
//                        arrayOf(
//                                Blocks.AIR, Blocks.AIR, Blocks.AIR, Blocks.AIR, Blocks.AIR, Blocks.AIR, Blocks.AIR, Blocks.AIR
//                        ), arrayOf(
//                        Blocks.AIR, Blocks.AIR, Blocks.AIR, Blocks.AIR, Blocks.AIR, Blocks.AIR, Blocks.AIR, Blocks.AIR
//                ), arrayOf(
//                        Blocks.AIR, Blocks.AIR, Blocks.AIR, Blocks.AIR, Blocks.AIR, Blocks.AIR, Blocks.AIR, Blocks.AIR
//                ), arrayOf(
//                        Blocks.STAIRS, Blocks.STAIRS, Blocks.AIR, Blocks.AIR, Blocks.AIR, Blocks.AIR, Blocks.STAIRS, Blocks.STAIRS
//                ), arrayOf(
//                        Blocks.AIR, Blocks.STAIRS, Blocks.STAIRS, Blocks.AIR, Blocks.AIR, Blocks.STAIRS, Blocks.STAIRS, Blocks.AIR
//                ), arrayOf(
//                        Blocks.AIR, Blocks.AIR, Blocks.STAIRS, Blocks.STAIRS, Blocks.STAIRS, Blocks.STAIRS, Blocks.AIR, Blocks.AIR
//                ), arrayOf(
//                        Blocks.AIR, Blocks.AIR, Blocks.AIR, Blocks.STAIRS, Blocks.STAIRS, Blocks.AIR, Blocks.AIR, Blocks.AIR
//                )
//                ), arrayOf(
//                arrayOf(
//                        Blocks.AIR, Blocks.LOG, Blocks.AIR, Blocks.AIR, Blocks.AIR, Blocks.AIR, Blocks.LOG, Blocks.AIR
//                ), arrayOf(
//                Blocks.AIR, Blocks.LOG, Blocks.AIR, Blocks.AIR, Blocks.AIR, Blocks.AIR, Blocks.LOG, Blocks.AIR
//        ), arrayOf(
//                Blocks.AIR, Blocks.LOG, Blocks.AIR, Blocks.AIR, Blocks.AIR, Blocks.AIR, Blocks.LOG, Blocks.AIR
//        ), arrayOf(
//                Blocks.STAIRS, Blocks.LOG, Blocks.LOG, Blocks.LOG, Blocks.LOG, Blocks.LOG, Blocks.LOG, Blocks.STAIRS
//        ), arrayOf(
//                Blocks.AIR, Blocks.STAIRS, Blocks.PLANKS, Blocks.GLASS_PANE, Blocks.GLASS_PANE, Blocks.PLANKS, Blocks.STAIRS, Blocks.AIR
//        ), arrayOf(
//                Blocks.AIR, Blocks.AIR, Blocks.STAIRS, Blocks.PLANKS, Blocks.PLANKS, Blocks.STAIRS, Blocks.AIR, Blocks.AIR
//        ), arrayOf(
//                Blocks.AIR, Blocks.AIR, Blocks.AIR, Blocks.STAIRS, Blocks.STAIRS, Blocks.AIR, Blocks.AIR, Blocks.AIR
//        )
//        ), arrayOf(
//                arrayOf(
//                        Blocks.AIR, Blocks.AIR, Blocks.PLANKS, Blocks.PLANKS, Blocks.AIR, Blocks.PLANKS, Blocks.AIR, Blocks.AIR
//                ), arrayOf(
//                Blocks.AIR, Blocks.AIR, Blocks.PLANKS, Blocks.PLANKS, Blocks.AIR, Blocks.PLANKS, Blocks.AIR, Blocks.AIR
//        ), arrayOf(
//                Blocks.AIR, Blocks.AIR, Blocks.PLANKS, Blocks.PLANKS, Blocks.PLANKS, Blocks.PLANKS, Blocks.AIR, Blocks.AIR
//        ), arrayOf(
//                Blocks.STAIRS, Blocks.LOG, Blocks.AIR, Blocks.AIR, Blocks.AIR, Blocks.AIR, Blocks.LOG, Blocks.STAIRS
//        ), arrayOf(
//                Blocks.AIR, Blocks.STAIRS, Blocks.STAIRS, Blocks.AIR, Blocks.AIR, Blocks.STAIRS, Blocks.STAIRS, Blocks.AIR
//        ), arrayOf(
//                Blocks.AIR, Blocks.AIR, Blocks.STAIRS, Blocks.STAIRS, Blocks.STAIRS, Blocks.STAIRS, Blocks.AIR, Blocks.AIR
//        ), arrayOf(
//                Blocks.AIR, Blocks.AIR, Blocks.AIR, Blocks.STAIRS, Blocks.STAIRS, Blocks.AIR, Blocks.AIR, Blocks.AIR
//        )
//        ), arrayOf(
//                arrayOf(
//                        Blocks.AIR, Blocks.AIR, Blocks.PLANKS, Blocks.AIR, Blocks.AIR, Blocks.PLANKS, Blocks.AIR, Blocks.AIR
//                ), arrayOf(
//                Blocks.AIR, Blocks.AIR, Blocks.GLASS_PANE, Blocks.AIR, Blocks.AIR, Blocks.GLASS_PANE, Blocks.AIR, Blocks.AIR
//        ), arrayOf(
//                Blocks.AIR, Blocks.AIR, Blocks.PLANKS, Blocks.STAIRS, Blocks.AIR, Blocks.PLANKS, Blocks.AIR, Blocks.AIR
//        ), arrayOf(
//                Blocks.STAIRS, Blocks.LOG, Blocks.AIR, Blocks.AIR, Blocks.AIR, Blocks.AIR, Blocks.LOG, Blocks.STAIRS
//        ), arrayOf(
//                Blocks.AIR, Blocks.STAIRS, Blocks.STAIRS, Blocks.AIR, Blocks.AIR, Blocks.STAIRS, Blocks.STAIRS, Blocks.AIR
//        ), arrayOf(
//                Blocks.AIR, Blocks.AIR, Blocks.STAIRS, Blocks.STAIRS, Blocks.STAIRS, Blocks.STAIRS, Blocks.AIR, Blocks.AIR
//        ), arrayOf(
//                Blocks.AIR, Blocks.AIR, Blocks.AIR, Blocks.STAIRS, Blocks.STAIRS, Blocks.AIR, Blocks.AIR, Blocks.AIR
//        )
//        ), arrayOf(
//                arrayOf(
//                        Blocks.AIR, Blocks.AIR, Blocks.PLANKS, Blocks.AIR, Blocks.STAIRS, Blocks.PLANKS, Blocks.AIR, Blocks.AIR
//                ), arrayOf(
//                Blocks.AIR, Blocks.AIR, Blocks.PLANKS, Blocks.STAIRS, Blocks.AIR, Blocks.GLASS_PANE, Blocks.AIR, Blocks.AIR
//        ), arrayOf(
//                Blocks.AIR, Blocks.AIR, Blocks.PLANKS, Blocks.AIR, Blocks.AIR, Blocks.PLANKS, Blocks.AIR, Blocks.AIR
//        ), arrayOf(
//                Blocks.STAIRS, Blocks.LOG, Blocks.AIR, Blocks.AIR, Blocks.AIR, Blocks.AIR, Blocks.LOG, Blocks.STAIRS
//        ), arrayOf(
//                Blocks.AIR, Blocks.STAIRS, Blocks.STAIRS, Blocks.AIR, Blocks.AIR, Blocks.STAIRS, Blocks.STAIRS, Blocks.AIR
//        ), arrayOf(
//                Blocks.AIR, Blocks.AIR, Blocks.STAIRS, Blocks.STAIRS, Blocks.STAIRS, Blocks.STAIRS, Blocks.AIR, Blocks.AIR
//        ), arrayOf(
//                Blocks.AIR, Blocks.AIR, Blocks.AIR, Blocks.STAIRS, Blocks.STAIRS, Blocks.AIR, Blocks.AIR, Blocks.AIR
//        )
//        ), arrayOf(
//                arrayOf(
//                        Blocks.AIR, Blocks.AIR, Blocks.PLANKS, Blocks.PLANKS, Blocks.PLANKS, Blocks.PLANKS, Blocks.AIR, Blocks.AIR
//                ), arrayOf(
//                Blocks.AIR, Blocks.AIR, Blocks.PLANKS, Blocks.PLANKS, Blocks.PLANKS, Blocks.PLANKS, Blocks.AIR, Blocks.AIR
//        ), arrayOf(
//                Blocks.AIR, Blocks.AIR, Blocks.PLANKS, Blocks.PLANKS, Blocks.PLANKS, Blocks.PLANKS, Blocks.AIR, Blocks.AIR
//        ), arrayOf(
//                Blocks.STAIRS, Blocks.LOG, Blocks.AIR, Blocks.AIR, Blocks.AIR, Blocks.AIR, Blocks.LOG, Blocks.STAIRS
//        ), arrayOf(
//                Blocks.AIR, Blocks.STAIRS, Blocks.STAIRS, Blocks.AIR, Blocks.AIR, Blocks.STAIRS, Blocks.STAIRS, Blocks.AIR
//        ), arrayOf(
//                Blocks.AIR, Blocks.AIR, Blocks.STAIRS, Blocks.STAIRS, Blocks.STAIRS, Blocks.STAIRS, Blocks.AIR, Blocks.AIR
//        ), arrayOf(
//                Blocks.AIR, Blocks.AIR, Blocks.AIR, Blocks.STAIRS, Blocks.STAIRS, Blocks.AIR, Blocks.AIR, Blocks.AIR
//        )
//        ), arrayOf(
//                arrayOf(
//                        Blocks.AIR, Blocks.LOG, Blocks.AIR, Blocks.AIR, Blocks.AIR, Blocks.AIR, Blocks.LOG, Blocks.AIR
//                ), arrayOf(
//                Blocks.AIR, Blocks.LOG, Blocks.AIR, Blocks.AIR, Blocks.AIR, Blocks.AIR, Blocks.LOG, Blocks.AIR
//        ), arrayOf(
//                Blocks.AIR, Blocks.LOG, Blocks.AIR, Blocks.AIR, Blocks.AIR, Blocks.AIR, Blocks.LOG, Blocks.AIR
//        ), arrayOf(
//                Blocks.STAIRS, Blocks.LOG, Blocks.LOG, Blocks.LOG, Blocks.LOG, Blocks.LOG, Blocks.LOG, Blocks.STAIRS
//        ), arrayOf(
//                Blocks.AIR, Blocks.STAIRS, Blocks.PLANKS, Blocks.GLASS_PANE, Blocks.GLASS_PANE, Blocks.PLANKS, Blocks.STAIRS, Blocks.AIR
//        ), arrayOf(
//                Blocks.AIR, Blocks.AIR, Blocks.STAIRS, Blocks.PLANKS, Blocks.PLANKS, Blocks.STAIRS, Blocks.AIR, Blocks.AIR
//        ), arrayOf(
//                Blocks.AIR, Blocks.AIR, Blocks.AIR, Blocks.STAIRS, Blocks.STAIRS, Blocks.AIR, Blocks.AIR, Blocks.AIR
//        )
//        ), arrayOf(
//                arrayOf(
//                        Blocks.AIR, Blocks.AIR, Blocks.AIR, Blocks.AIR, Blocks.AIR, Blocks.AIR, Blocks.AIR, Blocks.AIR
//                ), arrayOf(
//                Blocks.AIR, Blocks.AIR, Blocks.AIR, Blocks.AIR, Blocks.AIR, Blocks.AIR, Blocks.AIR, Blocks.AIR
//        ), arrayOf(
//                Blocks.AIR, Blocks.AIR, Blocks.AIR, Blocks.AIR, Blocks.AIR, Blocks.AIR, Blocks.AIR, Blocks.AIR
//        ), arrayOf(
//                Blocks.STAIRS, Blocks.STAIRS, Blocks.AIR, Blocks.AIR, Blocks.AIR, Blocks.AIR, Blocks.STAIRS, Blocks.STAIRS
//        ), arrayOf(
//                Blocks.AIR, Blocks.STAIRS, Blocks.STAIRS, Blocks.AIR, Blocks.AIR, Blocks.STAIRS, Blocks.STAIRS, Blocks.AIR
//        ), arrayOf(
//                Blocks.AIR, Blocks.AIR, Blocks.STAIRS, Blocks.STAIRS, Blocks.STAIRS, Blocks.STAIRS, Blocks.AIR, Blocks.AIR
//        ), arrayOf(
//                Blocks.AIR, Blocks.AIR, Blocks.AIR, Blocks.STAIRS, Blocks.STAIRS, Blocks.AIR, Blocks.AIR, Blocks.AIR
//        )
//        )
//        )
