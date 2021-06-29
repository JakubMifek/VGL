package org.mifek.vgl.wfc

import org.mifek.vgl.commands.SaveTemplate
import org.mifek.vgl.implementations.Area
import org.mifek.vgl.implementations.Block
import org.mifek.vgl.implementations.PlacedBlock
import org.mifek.vgl.interfaces.IBlockStream
import org.mifek.vgl.utils.TemplateHolder
import org.mifek.wfc.models.options.Cartesian3DModelOptions
import kotlin.test.Test
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@ExperimentalUnsignedTypes
class MinecraftWfcAdapterTest {
    private val adapterOptions = MinecraftWfcAdapterOptions(
        overlap = 2,
        modelOptions = Cartesian3DModelOptions(
            periodicInput = false
        ),
        debugOptions = DebugOptions(seed = 123),
        repeats = 50
    )
    private val outputSize = Triple(11, 11, 11)
    private val testArea = Area(
        0,
        0,
        0,
        outputSize.first,
        outputSize.second,
        outputSize.third
    )
    private val templateHolder = TemplateHolder.templates

    class TestStream : IBlockStream {
        override fun add(block: PlacedBlock) {
            println("Placing block ${block.block.name} to [${block.x},${block.y},${block.z}]")
        }
    }

    fun printTemplate(template: Array<Array<Array<Block>>>) {
        println(template.joinToString("\n\n\n") { it.joinToString("\n") { it.joinToString(", ") { it.serialize() } } })
    }

    @Test
    fun testWoodBlock() {
        val result = MinecraftWfcAdapter.imitate(templateHolder["wood_block"]!!, outputSize, adapterOptions)
        assertNotNull(result)
        printTemplate(result)
    }

    @Test
    fun testImitateHouse() {
        val options = adapterOptions.copy()
        val result = MinecraftWfcAdapter.imitate(templateHolder["house"]!!, outputSize, options)
        assertNotNull(result)
        printTemplate(result)
    }

    @Test
    fun testOutputStream() {
        val options = adapterOptions.copy(
            streamOptions = StreamOptions(
                stream = TestStream(),
                area = testArea
            )
        )
        val result = MinecraftWfcAdapter.imitate(templateHolder["wood_block"]!!, outputSize, options)
//        assertNotNull(result)
//        printTemplate(result)
    }

    /*@Test
    fun testGenerateHouseCommand() {
        printTemplate(
            GenerateHouse().execute(
                testArea,
                TestStream(),
                DebugOptions(verbose = false)
            ) ?: emptyArray()
        )
    }*/

    @Test
    fun testSaveTemplateCommand() {
        val name = SaveTemplate().execute(
            TemplateHolder.templates["wood_block"]!!,
            "tmp"
        )
        assertNotEquals("wood_block", name)
        val one = TemplateHolder.templates[name]!!.flatten().toTypedArray().flatten().map { it.serialize() }
            .toTypedArray()
        val two = TemplateHolder.templates["wood_block"]!!.flatten().toTypedArray().flatten().map { it.serialize() }
            .toTypedArray()
        assertTrue(one.contentEquals(two))
        assertTrue(TemplateHolder.removeTemplateFile(name))
    }
}