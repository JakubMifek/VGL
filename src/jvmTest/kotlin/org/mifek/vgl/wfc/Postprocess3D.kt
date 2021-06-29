package org.mifek.vgl.wfc

import org.mifek.vgl.commands.GenerateHouse
import org.mifek.vgl.formatForPrint
import org.mifek.vgl.implementations.Area
import kotlin.test.Test
import kotlin.test.assertNotNull

@ExperimentalUnsignedTypes
class Postprocess3D {
    @Test
    fun testCrossPostprocess() {
        val command = GenerateHouse()
        val result = command.execute(
            "cross",
            Area(0, 0, 0, 40, 7, 40),
            GenerateHouse.defaultOptions.copy(debugOptions = DebugOptions(seed = 123456))
        )

        assertNotNull(result)

        println(result.formatForPrint())
    }
}