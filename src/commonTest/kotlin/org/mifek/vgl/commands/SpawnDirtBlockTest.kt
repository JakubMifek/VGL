package org.mifek.vgl.commands

import org.mifek.vgl.commands.SpawnDirtBlock
import org.mifek.vgl.implementations.Area
import org.mifek.vgl.implementations.Blocks
import kotlin.test.Test
import kotlin.test.assertEquals

class SpawnDirtBlockTest {
    @Test
    fun testExecution() {
        val sh = SpawnDirtBlock()
        val area = Area(-177,79,256,100,100,100)
        val result = sh.execute(area)
        assertEquals(result.size, 100)
        assertEquals(result[0].size, 100)
        assertEquals(result[0][0].size, 100)
        assertEquals(result[0][0][0], Blocks.DIRT.id)
    }
}