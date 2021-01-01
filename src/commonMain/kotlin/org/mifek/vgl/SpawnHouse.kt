package org.mifek.vgl

import org.mifek.vgl.implementations.Blocks
import org.mifek.vgl.interfaces.IArea
import org.mifek.vgl.interfaces.IBlockStream
import org.mifek.vgl.implementations.PlacedBlock
import org.mifek.vgl.utilities.TemplateHolder

class SpawnHouse {
    fun execute(area: IArea, stream: IBlockStream) {
        val house = TemplateHolder.templates["house"]!!
        val skips = ArrayList<Triple<Int, Int, Int>>()
        var skip = false

        for (y in (0 until area.height)) {
            for (x in (0 until area.width)) {
                for (z in (0 until area.depth)) {
                    if(skips.size > 0) { // If there are any blocks to skip
                        for(triple in skips) { // Check whether this one is included
                            if(triple.first == x && triple.second == y && triple.third == z) { // If so, skip it
                                skips.remove(triple)
                                skip = true
                                break
                            }
                        }
                    }

                    if (!skip && x < house.size && y < house[x].size && z < house[x][y].size) {
                        if(house[x][y][z].block.id == Blocks.TORCH.id && house[x][y][z].props.size > 0) {
                            if(x < house.size - 1) {
                                stream.add(PlacedBlock(area.x + house.size - 2 - x, area.y + y, area.z - z, house[x+1][y][z].block, house[x+1][y][z].props))

                                // Skip the upper door placement
                                skips.add(Triple(x+1, y, z))
                            }
                            if(z < house[x][y].size) {
                                stream.add(PlacedBlock(area.x + house.size - 1 - x, area.y + y, area.z - z - 1, house[x][y][z+1].block, house[x][y][z+1].props))

                                // Skip the upper door placement
                                skips.add(Triple(x, y, z+1))
                            }
                        }

                        stream.add(PlacedBlock(area.x + house.size - 1 - x, area.y + y, area.z - z, house[x][y][z].block, house[x][y][z].props))

                        // If doors, place upper half as well
                        if(house[x][y][z].block.id == Blocks.WOODEN_DOOR.id && y + 1 < house[x].size) {
                            stream.add(PlacedBlock(area.x +  house.size - 1 - x, area.y + y + 1, area.z - z, house[x][y+1][z].block, house[x][y+1][z].props))

                            // Skip the upper door placement
                            skips.add(Triple(x, y+1, z))
                        }
                    }

                    skip = false
                }
            }
        }
    }
}
