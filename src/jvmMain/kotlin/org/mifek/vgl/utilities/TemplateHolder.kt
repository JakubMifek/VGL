package org.mifek.vgl.utilities

import org.mifek.vgl.implementations.Block
import org.mifek.vgl.implementations.Blocks
import java.nio.file.Files
import java.nio.file.Paths
import java.util.stream.Collectors.toList

actual object TemplateHolder {
    actual val templates: HashMap<String,Array<Array<Array<Block>>>> = HashMap()

    init {
        val projectDirAbsolutePath = Paths.get("").toAbsolutePath().toString()
        val resourcesPath = Paths.get(projectDirAbsolutePath, "/templates")
        Files.walk(resourcesPath)
                .filter { item -> Files.isRegularFile(item) }
                .filter { item -> item.toString().endsWith(".tmpl") }
                .map { item -> Pair(item, item.toFile().readLines().filter { it.trim().isNotEmpty() && it.trim()[0] != '#' }) }
                .forEach { (path, item) -> templates[path.fileName.toString().split('.')[0]] = processFile(path.fileName.toString(), item) }
    }

    private fun processFile(path: String, lines: List<String>): Array<Array<Array<Block>>> {
        println("Processing file '$path'")
        if (lines.isEmpty()) return emptyArray()

        var index = 0
        val buildingBlocks = HashMap<Char, Block>()

        // Init building blocks
        // <char>,<id>,<key>:<value>,<key2>:<value2>  # comment
        while (index < lines.size && lines[index].matches(Regex("^.,(\\w+(_\\w+)*)?(,\\w+:\\w+)*\\s*(#.*)?$"))) {
            val preComment = lines[index].split('#')[0].trim()
            val parts = preComment.split(',')

            val character = parts[0][0]
            val block = Block.deserialize(preComment.substring(2))

            buildingBlocks[character] = block
            index++
        }

        val dimLine = lines[index].replace(Regex("\\s"), "")
        if (!dimLine.matches(Regex("^\\d+x\\d+x\\d+$"))) {
            throw Error("Could not read dimensions of the template on line ${index + 1}")
        }

        val dimensions = dimLine.split('x').map { it.toInt() }.toIntArray()
        val width = dimensions[0]
        val height = dimensions[1]
        val depth = dimensions[2]
        index++

        // Build the template
        val maxLines = height*depth + index
        if(lines.size < maxLines) {
            throw Error("Not enough lines for the whole template according to set dimensions. Expected at least $maxLines")
        }

        val allowedCharacters = Regex("^[" + String(buildingBlocks.keys.toCharArray()) + "\\s]*$")
        var h = 0
        var d = 0
        val ret = Array(width) { Array(height) { Array(depth) { Blocks.AIR.toBlock() } } }

        while (index < maxLines && lines[index].matches(allowedCharacters)) {
            val data = lines[index].replace(Regex("\\s"), "")
            println(data)
            for(w in (data.indices)) {
                ret[width-w-1][height-h-1][d] = buildingBlocks[data[w]]!!
            }

            h++
            index++

            if(h % height == 0) {
                h = 0
                d++
            }
        }

        if (index < lines.size) {
            println("WARNING: Skipping ${lines.size - index} lines. Line ${index + 1} contains unrecognized characters or exceeds set dimensions.")
        }

        return ret
    }
}