package org.mifek.vgl.wfc

import org.mifek.vgl.implementations.Block
import org.mifek.vgl.implementations.Blocks
import org.mifek.vgl.utils.TemplateHolder
import org.mifek.wfc.models.options.Cartesian2DModelOptions
import org.mifek.wfc.models.options.Cartesian3DModelOptions
import kotlin.test.Test
import kotlin.test.assertNotNull

@ExperimentalUnsignedTypes
class FloorPlanTest {
    val mapping = mapOf(
        Pair(Blocks.OAK_WOOD_PLANK, "▣"),
        Pair(Blocks.STONE, "⬛"),
        Pair(Blocks.GRASS, "⬜")
    )

    fun printFloorPlan(result: Array<Array<Block>>) {
        for (x in result.indices) {
            for (y in result[x].indices) {
                print(mapping[result[x][y].block] + " ")
            }
            println()
        }
        println()
    }

    fun printTemplate(template: Array<Array<Array<Block>>>) {
        println(template.joinToString("\n\n\n") { it.joinToString("\n") { it.joinToString(", ") { it.serialize() } } })
    }

    @Test
    fun testFloorPlanGeneration() {
        printFloorPlan(TemplateHolder.floorPlans["cross"]!!)
        val result = FloorPlan.generate(
            TemplateHolder.floorPlans["cross"]!!, 20, 20, FloorPlanOptions(
                modelOptions = Cartesian2DModelOptions(
                    allowRotations = true,
                    allowHorizontalFlips = true,
                    allowVerticalFlips = true,
                    roofed = true,
                    rightSided = true,
                    grounded = true,
                    leftSided = true,
                    weightPower = 0.333333
                ),
                debugOptions = DebugOptions(seed = 123456)
            )
        )
        assertNotNull(result)
        printFloorPlan(result)
    }

    @Test
    fun testFloorPlanSetting() {
//        printFloorPlan(TemplateHolder.floorPlans["cross"]!!)
        val floorPlan = FloorPlan.generate(
            TemplateHolder.floorPlans["cross"]!!, 9, 9, FloorPlanOptions(
                modelOptions = Cartesian2DModelOptions(
                    allowRotations = true,
                    allowHorizontalFlips = true,
                    allowVerticalFlips = true,
                    roofed = true,
                    rightSided = true,
                    grounded = true,
                    leftSided = true,
                    weightPower = 0.333333
                ),
                debugOptions = DebugOptions(seed = 123456)
            )
        )

        assertNotNull(floorPlan)
        printFloorPlan(floorPlan)

        val result = MinecraftWfcAdapter.imitate(
            TemplateHolder.templates["cross"]!!,
            Triple(9, 6, 9),
            MinecraftWfcAdapterOptions(
                modelOptions = Cartesian3DModelOptions(
                    allowYRotations = true,
                    allowZFlips = true,
                    allowXFlips = true,
                ),
                name = "cross",
                setBlocks = Iterable {
                    iterator {
                        for (x in 0 until floorPlan.size) {
                            for (z in 0 until floorPlan[x].size) {
                                yield(Pair(Triple(x, 0, z), floorPlan[x][z]))
                            }
                        }
                    }
                },
                debugOptions = DebugOptions(
//                    verbose = true,
                    seed = 12345
                )
            )
        )
        assertNotNull(result)
//        printTemplate(result)
    }
}