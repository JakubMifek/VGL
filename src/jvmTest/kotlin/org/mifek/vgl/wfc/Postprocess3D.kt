package org.mifek.vgl.wfc

import org.mifek.vgl.commands.Generate
import org.mifek.vgl.implementations.Area
import org.mifek.wfc.datatypes.Direction3D
import org.mifek.wfc.models.options.Cartesian3DModelOptions
import kotlin.test.Test
import kotlin.test.assertNotNull

@ExperimentalUnsignedTypes
class Postprocess3D {
    @Test
    fun testCrossPostprocess() {
        val command = Generate()
        val result = command.execute(
            "cross",
            Area(0, 0, 0, 40, 7, 40),
            MinecraftWfcAdapterOptions(
                overlap = 2,
                modelOptions = Cartesian3DModelOptions(
                    allowYRotations = true,
                    allowXFlips = true,
                    allowZFlips = true,
                    setPlanes = Direction3D.values().toSet(),
                    weightPower = 0.333333
                ),
                repeats = 1,
                debugOptions = DebugOptions(seed = 123456)
            )
        )

        assertNotNull(result)

        println(Generate())
    }
}