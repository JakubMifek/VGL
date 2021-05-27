package org.mifek.vgl.utils

import org.mifek.vgl.implementations.Block
import org.mifek.vgl.implementations.Blocks
import org.mifek.wfc.adapters.utils.toIntArray2D
import org.mifek.wfc.datastructures.IntArray2D
import java.io.File
import java.io.FileWriter
import java.io.PrintWriter
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import javax.imageio.ImageIO
import kotlin.math.max
import kotlin.streams.toList

actual object TemplateHolder {
    actual val templates: HashMap<String, Array<Array<Array<Block>>>> = HashMap()
    actual val floorPlans: HashMap<String, Array<Array<Block>>> = HashMap()

    var fp: IntArray2D = IntArray2D(0, 0)
    val floorPlan: IntArray2D
        get() {
            return fp
        }

    private val resourcesPath: Path

    private val alphabet =
        ('A'.toInt()..'Z'.toInt()).plus('0'.toInt()..'9'.toInt()).plus('a'.toInt()..'z'.toInt())
            .map { it.toChar() }.toTypedArray()

    init {
        val projectDirAbsolutePath = Paths.get("").toAbsolutePath().toString()
        resourcesPath = Paths.get(projectDirAbsolutePath, "/templates")

        val staticPaths = Paths.get(resourcesPath.toAbsolutePath().toString(), "/static")
        if (!staticPaths.toFile().exists()) {
            staticPaths.toFile().mkdirs()
        }

        val stream = Files.walk(resourcesPath)
        val files = stream.filter { item ->
            Files.isRegularFile(item)
        }.toList()

        files
            .filter { item -> item.toString().endsWith(".tmpl") }
            .map { item ->
                Pair(
                    item,
                    item.toFile().readLines().filter { it.trim().isNotEmpty() && it.trim()[0] != '#' })
            }
            .forEach { (path, item) ->
                add(path.fileName.toString().split('.')[0], processFile(path.fileName.toString(), item))
            }

        files
            .filter { item -> item.toString().endsWith(".png") }
            .map { processImage(it) }
    }

    private fun processImage(path: Path) {
        if (path.fileName.toString() == "floor_plan.png") {
            fp = ImageIO.read(path.toFile()).toIntArray2D()
            println("Found the floor plan!")
        }
    }

    private fun processFile(path: String, lines: List<String>): Array<Array<Array<Block>>> {
        // println("Processing file '$path'")
        if (lines.isEmpty()) return emptyArray()

        var index = 0
        val buildingBlocks = HashMap<Char, Block>()

        // Init building blocks
        // <char>,<id>,<key>:<value>,<key2>:<value2>  # comment
        while (index < lines.size && lines[index].matches(Regex("^.\\s*,\\s*(-?\\d+)?\\s*(,\\w+:\\w+)*\\s*(#.*)?$"))) {
            val preComment = lines[index].split('#')[0].trim()
            val parts = preComment.split(',')

            val character = parts[0][0]
            val block = Block.deserialize(preComment.substring(2))

            buildingBlocks[character] = block
            index++
        }

        val dimLine = lines[index].replace(Regex("\\s"), "")
        if (!dimLine.matches(Regex("^\\d+x\\d+x\\d+\\s*(#.*)?$"))) {
            throw Error("Could not read dimensions of the template on line ${index + 1}")
        }
        val preComment = lines[index].split('#')[0].trim()
        val dimensions = preComment.split('x').map { it.toInt() }.toIntArray()
        val width = dimensions[0]
        val height = dimensions[1]
        val depth = dimensions[2]
        index++

        // Build the template
        val maxLines = height * depth + index
        if (lines.size < maxLines) {
            throw Error("Not enough lines for the whole template according to set dimensions. Expected at least $maxLines")
        }

        val allowedCharacters = Regex("^[" + String(buildingBlocks.keys.toCharArray()) + "\\s]*$")
        var h = 0
        var d = 0
        val ret = Array(width) { Array(height) { Array(depth) { Blocks.AIR.toBlock() } } }

        while (index < maxLines && lines[index].matches(allowedCharacters)) {
            val data = lines[index].replace(Regex("\\s"), "")
            // println(data)
            for (w in (data.indices)) {
                ret[width - w - 1][height - h - 1][d] = buildingBlocks[data[w]]!!
            }

            h++
            index++

            if (h % height == 0) {
                h = 0
                d++
            }
        }

        if (index < lines.size) {
            println(
                "WARNING: Skipping ${lines.size - index} lines. Line ${
                    index + 1
                } contains unrecognized characters or exceeds set dimensions."
            )
        }

        return ret
    }

    fun removeTemplateFile(name: String): Boolean {
        val file = File(resourcesPath.toFile(), name)
        return file.delete()
    }

    fun add(name: String, template: Array<Array<Array<Block>>>) {
        templates[name] = template
        floorPlans[name] = extractFloorPlan(name)
    }

    fun extractFloorPlan(name: String): Array<Array<Block>> {
        val template = templates[name]!!
        return Array(template.size) { x -> Array(template[x][0].size) { z -> template[x][0][z] } }
    }

    actual fun saveTemplate(template: Array<Array<Array<Block>>>, name: String): String {
        add(if (name.endsWith(".tmpl")) name.substring(0, name.length - 5) else name, template)

        val file = File(resourcesPath.toFile(), name)
        if (!file.exists()) file.createNewFile()

        val blockPalette = template.flatten().toTypedArray().flatten().distinctBy { it.serialize() }.toTypedArray()
        if (blockPalette.size > alphabet.size) {
            throw Error(
                "Unfortunately, we are able to support only ${
                    alphabet.size
                } blocks at the moment. Please scale down your block palette a bit."
            )
        }

        val mapping = blockPalette.zip(alphabet)
        val mapping2 = hashMapOf(*mapping.map { Pair(it.first.serialize(), it.second) }.toTypedArray())
        val w = template.size
        val h = template[0].size
        val d = template[0][0].size

        PrintWriter(FileWriter(file)).use { writer ->
            writer.println("# Building blocks")
            for (pair in mapping) {
                writer.println(
                    "${pair.second},${pair.first.serialize()}${
                        (0 until max(
                            1,
                            50 - "${pair.second},${pair.first.serialize()}".length
                        )).joinToString(" ") { "" }
                    }# ${pair.first.block.name}"
                )
            }
            writer.println()
            writer.println("${w}x${h}x${d} # Template dimensions")
            writer.println()

            writer.println("# Template data")
            for (z in 0 until d) {
                writer.println("# ${z}. slice")
                for (y in 0 until h) {
                    val line = template.indices.map {
                        if (template[it].size != h || template[it][y].size != d) {
                            throw Error(
                                "Template dimensions must be same in every array! Expected $w x $h x $d but found $w x ${
                                    template[it].size
                                } x ${template[it][y].size} for [$it,$y,$z]"
                            )
                        }
                        mapping2[template[w - it - 1][h - y - 1][z].serialize()]
                    }
                    writer.println(line.joinToString(" "))
                }
            }
        }
        return name
    }
}