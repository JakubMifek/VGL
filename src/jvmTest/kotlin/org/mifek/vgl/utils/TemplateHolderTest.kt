package org.mifek.vgl.utils

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class TemplateHolderTest {
    @Test
    fun houseTest() {
        val data = TemplateHolder.templates["house"]
        assertNotNull(data)
        val D = data.toIntArray3D()
        val array = D.first
        assertEquals(12, array.width)
        assertEquals(12, array.height)
        assertEquals(12, array.depth)
    }

    @Test
    fun woodBlockTest() {
        val data = TemplateHolder.templates["wood_block"]
        assertNotNull(data)
        val D = data.toIntArray3D()
        val array = D.first
        assertEquals(5, array.width)
        assertEquals(5, array.height)
        assertEquals(5, array.depth)
    }
}