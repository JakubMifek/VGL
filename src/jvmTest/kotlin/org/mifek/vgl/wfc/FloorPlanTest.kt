package org.mifek.vgl.wfc

import org.mifek.vgl.palettes.PaletteKeys
import org.mifek.vgl.village.FloorPlan
import kotlin.test.Test
import kotlin.test.assertNotNull

@ExperimentalUnsignedTypes
class FloorPlanTest {
    val mapping = mapOf(
        Pair(PaletteKeys.DOORS, "\uD83D\uDD36"),
        Pair(PaletteKeys.WALL, "⬛"),
        Pair(PaletteKeys.FLOOR, "⬜"),
        Pair(PaletteKeys.GROUND, "⬜")
    )

    fun printFloorPlan(result: Array<Array<PaletteKeys>>) {
        for (x in result.indices) {
            for (y in result[x].indices) {
                print(mapping[result[x][y]] + " ")
            }
            println()
        }
    }

    @Test
    fun testFloorPlanGeneration() {
        val result = FloorPlan.generate(30, 20, 12345)
        assertNotNull(result)
//        printFloorPlan(result)
    }

}