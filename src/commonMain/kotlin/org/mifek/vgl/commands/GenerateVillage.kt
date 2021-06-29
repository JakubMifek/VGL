//package org.mifek.vgl.commands
//
//import org.mifek.vgl.implementations.Block
//import org.mifek.vgl.implementations.Blocks
//import org.mifek.vgl.implementations.PlacedBlock
//import org.mifek.vgl.interfaces.IArea
//import org.mifek.vgl.interfaces.IBlockStream
//import org.mifek.vgl.utils.TemplateHolder
//import org.mifek.vgl.wfc.MinecraftWfcAdapter
//import org.mifek.vgl.wfc.MinecraftWfcAdapterOptions
//import org.mifek.wfc.datastructures.IntArray3D
//import org.mifek.wfc.datatypes.Direction3D
//import org.mifek.wfc.models.options.Cartesian2DModelOptions
//import org.mifek.wfc.utils.toCoordinates
//import kotlin.math.max
//import kotlin.math.min
//import kotlin.random.Random
//
//@ExperimentalUnsignedTypes
//class GenerateVillage {
//    companion object {
//        val modelOptions = Cartesian2DModelOptions(
//            allowRotations = true,
//            allowHorizontalFlips = true, allowVerticalFlips = true,
//            grounded = true, roofed = true, leftSided = true, rightSided = true
//        )
//    }
//
//    fun execute(
//        templateName: String,
//        area: IArea,
//        options: MinecraftWfcAdapterOptions = defaultOptions
//    ): Array<Array<Array<Block>>>? {
//        val template = TemplateHolder.templates[templateName] ?: throw Error("Could not find template $templateName.")
//
//        for (x in template.indices) {
//            for (y in template[x].indices) {
//                for (z in template[x][y].indices) {
//                    if (template[x][y][z].block in TRANSPARENT) template[x][y][z] = AIR_BLOCK
//                }
//            }
//        }
//
//        var adapterOptions = options
//
//        if (options.setBlocks == null) {
//            adapterOptions = adapterOptions.copy(setBlocks = Iterable { iterator { } })
//        }
//
//        val streamOptions = adapterOptions.streamOptions
//        adapterOptions = adapterOptions.copy(
//            setBlocks = adapterOptions.setBlocks!!.plus(
//                Pair(
//                    Triple(area.width / 2, 0, area.depth / 2),
//                    template[template.size / 2][0][template[0][0].size / 2]
//                )
//            ),
//            modelOptions = options.modelOptions.copy(setPlanes = Direction3D.values().toSet()),
//            streamOptions = null,
//        )
//
//        val house = MinecraftWfcAdapter.imitate(
//            template,
//            Triple(area.width, area.height, area.depth),
//            adapterOptions,
//        ) ?: return null
//
//        try {
//            val result = postprocess(house, Random(options.debugOptions?.seed ?: Random.nextInt()))
//
//            if (streamOptions == null) return result
//
//            stream(streamOptions.stream, area, result, noFilter = BREAKABLE)
//            stream(streamOptions.stream, area, result, yesFilter = BREAKABLE)
//        } catch (error: Error) {
//            println("Error")
//            println(error.message)
//            println(error.stackTrace)
//            throw error
//        }
//
//        return null
//    }
//
//    fun postprocess(house: Array<Array<Array<Block>>>, random: Random): Array<Array<Array<Block>>> {
//        // Mark walls and free space
//        val blackWhiteColored = IntArray3D(house.size, house[0].size, house[0][0].size) { BlockType.WALL.id }
//        for (x in house.indices) for (y in house[x].indices) for (z in house[x][y].indices) {
//            if (house[x][y][z].block in TRANSPARENT) {
//                blackWhiteColored[x, y, z] = BlockType.TRANSPARENT.id
//            }
//        }
//
//        // Locate where doors are supposed to go
//        val doorLocations = locateDoors(blackWhiteColored)
//
//        // Mark doors as taken space
//        for (location in doorLocations) {
//            blackWhiteColored[location.first, location.second, location.third] = BlockType.WALL.id
//            blackWhiteColored[location.first, location.second + 1, location.third] = BlockType.WALL.id
//        }
//
//        // Color in all rooms
//        val colored = IntArray3D(house.size, house[0].size, house[0][0].size) { -1 }
//        var totalColors = 0
//        for (x in 0 until colored.width) for (y in 0 until colored.height) for (z in 0 until colored.depth) {
//            if (blackWhiteColored[x, y, z] == BlockType.TRANSPARENT.id && colored[x, y, z] == -1) {
//                color(colored, blackWhiteColored, totalColors++, x, y, z)
//            }
//        }
//
//        // Color the doors
//        val coloredDoors = colorDoors(colored, doorLocations)
//        val surroundings = findSurroundings(house, colored, coloredDoors)
//
///*
//        println("Doors: ${coloredDoors.size}")
//        println(coloredDoors.joinToString("\n") { it.toString() })
//*/
//
//        // Remove duplicitous doors
//        val doorsToRemove = doorsToRemove(coloredDoors, totalColors, random)
//
///*
//        println("Doors2Remove: ${doorsToRemove.count()}")
//        println(doorsToRemove.joinToString("\n") { it.toString() })
//*/
//
//        removeDoors(doorsToRemove, coloredDoors, colored, blackWhiteColored, doorLocations, house)
//
//        // Now we want to create doors to unreachable rooms
//
//        // First we decompose the output into components based on connected colors - we merge colors until we can't anymore
//        mergeColors(colored, coloredDoors)
//
//        // On the merged colors we try to find good positions for doors
//        val doorsToCreate = doorsToCreate(colored, random)
//
///*
//        println("Doors2Create: ${doorsToCreate.count()}")
//        println(doorsToCreate.joinToString("\n") { it.toString() })
//*/
//
//        try {
//            // Finally create the doors
//            createDoors(doorsToCreate, coloredDoors, doorLocations)
//            placeInDoors(coloredDoors, surroundings, house, random)
//        } catch (error: Error) {
//            println("nani")
//            println(error.message)
//            println(error.stackTrace)
//        }
//        return house
//    }
//
//    private fun findSurroundings(
//        house: Array<Array<Array<Block>>>,
//        colored: IntArray3D,
//        coloredDoors: MutableSet<Pair<Pair<Int, Int>, Triple<Int, Int, Int>>>
//    ): Array<Triple<Set<Direction3D>, Boolean, Array<Array<Array<Block?>>>>> {
//        return coloredDoors.map {
//            Iterable {
//                iterator {
//                    val horizontal: Boolean =
//                        (it.second.first == 0 || house[it.second.first - 1][it.second.second][it.second.third].block !in TRANSPARENT)
//                                && (it.second.first == house.size - 1 || house[it.second.first + 1][it.second.second][it.second.third].block !in TRANSPARENT)
//
//                    val directions = Iterable {
//                        iterator {
//                            if (it.second.second < colored.height - 2) yield(Direction3D.UP)
//                            if (it.second.third < colored.depth - 1) yield(Direction3D.FORWARD)
//                            if (it.second.first < colored.width - 1) yield(Direction3D.RIGHT)
//                            if (it.second.second > 0) yield(Direction3D.DOWN)
//                            if (it.second.third > 0) yield(Direction3D.BACKWARD)
//                            if (it.second.first > 0) yield(Direction3D.LEFT)
//                        }
//                    }.toSet()
//
//                    val arr = Array(3) { x ->
//                        Array(4) { y ->
//                            Array(3) Arr@{ z ->
//                                if (x == 1 && (y == 1 || y == 2) && z == 1) return@Arr null
//                                val X = it.second.first - 1 + x
//                                if (X < 0 || X > house.size) return@Arr null
//                                val Y = it.second.second - 1 + y
//                                if (Y < 0 || Y > house[0].size) return@Arr null
//                                val Z = it.second.third - 1 + z
//                                if (Z < 0 || Z > house[0][0].size) return@Arr null
//
//                                house[X][Y][Z]
//                            }
//                        }
//                    }
//
//                    yield(Triple(directions, horizontal, arr))
//                }
//            }.toList().toTypedArray()
//        }.toTypedArray().flatten().toTypedArray()
//    }
//
//    private fun placeInDoors(
//        coloredDoors: MutableSet<Pair<Pair<Int, Int>, Triple<Int, Int, Int>>>,
//        surroundings: Array<Triple<Set<Direction3D>, Boolean, Array<Array<Array<Block?>>>>>,
//        house: Array<Array<Array<Block>>>,
//        random: Random
//    ) {
//        val doors =
//            house.flatten().toTypedArray().flatten().map { it.block }.filter { it in DOORS }.distinct().firstOrNull()
//                ?: Blocks.OAK_DOOR
//
//        for (door in coloredDoors) {
//            val x = door.second.first
//            val y = door.second.second
//            val z = door.second.third
//
//            val horizontal = house[x - 1][y][z].block !in TRANSPARENT && house[x + 1][y][z].block !in TRANSPARENT
//            val props =
//                if (horizontal) if (z > house[0][0].size / 2) posZDoors else negZDoors else if (x > house.size / 2) posXDoors else negXDoors
//
//            house[door.second.first][door.second.second][door.second.third] = Block(doors, props.first)
//            house[door.second.first][door.second.second + 1][door.second.third] = Block(doors, props.second)
//
//            val requiredDirections: Set<Direction3D> = setOf(*(Iterable {
//                iterator {
//                    // -2 for y coz we are bottom block of the doors
//                    if (y < house[0].size - 2) yield(Direction3D.UP)
//                    if (z < house[0][0].size - 1) yield(Direction3D.FORWARD)
//                    if (x < house.size - 1) yield(Direction3D.RIGHT)
//                    if (y > 0) yield(Direction3D.DOWN)
//                    if (z > 0) yield(Direction3D.BACKWARD)
//                    if (x > 0) yield(Direction3D.LEFT)
//
//                }
//            }.toList().toTypedArray()))
//
//            val usableSurroundings = surroundings.filter { surround ->
//                requiredDirections.all { it in surround.first }
//            }
//
//            if (usableSurroundings.isNotEmpty()) {
//                val neighbourhood = usableSurroundings[random.nextInt(usableSurroundings.size)]
//
//                println("Position ${door.second}")
//                println("Our horizontal $horizontal")
//                println("Neighbourhood horizontal ${neighbourhood.second}")
//
//                for (X in 0 until 3) {
//                    for (Y in 0 until 4) {
//                        for (Z in 0 until 3) {
//                            val X1 = if (horizontal != neighbourhood.second) 2 - Z else X
//                            val Z1 = if (horizontal != neighbourhood.second) X else Z
//
//                            if (neighbourhood.third[X1][Y][Z1] == null
//                                || door.second.first - 1 + X < 0 || door.second.first - 1 + X > house.size - 1
//                                || door.second.second - 1 + Y < 0 || door.second.second - 1 + Y > house[0].size - 1
//                                || door.second.third - 1 + Z < 0 || door.second.third - 1 + Z > house[0][0].size - 1
//                                || X1 == 2 && Z1 == 2
//                                || X1 == 0 && Z1 == 0
//                                || X1 == 2 && Z1 == 0
//                                || X1 == 0 && Z1 == 2
//                                || Y == 0 && (X1 != 1 || Z1 != 1)
//                                || Y == 3 && (X1 != 1 || Z1 != 1)
//                            ) continue
//
//                            house[door.second.first - 1 + X][door.second.second - 1 + Y][door.second.third - 1 + Z] =
//                                neighbourhood.third[X1][Y][Z1]!!
//                        }
//                    }
//                }
//            }
//        }
//    }
//
//    private fun createDoors(
//        doorsToCreate: Iterable<Triple<Int, Int, Int>>,
//        coloredDoors: MutableSet<Pair<Pair<Int, Int>, Triple<Int, Int, Int>>>,
//        doorLocations: MutableSet<Triple<Int, Int, Int>>
//    ) {
//        for (door in doorsToCreate) {
//            coloredDoors.add(Pair(Pair(0, 0), door))
//            doorLocations.add(door)
//        }
//    }
//
//    private fun doorsToCreate(
//        colored: IntArray3D,
//        random: Random,
//    ): Iterable<Triple<Int, Int, Int>> {
//        val walls: MutableMap<Pair<Int, Int>, MutableList<Triple<Int, Int, Int>>> = hashMapOf()
//
//        // We limit x and z in the search 'coz there must be air and grass around the house, we do not allow close neighbours since it could create double walls with no entrance
//        for (x in 1 until colored.width - 1) for (y in 0 until colored.height - 1) for (z in 1 until colored.depth - 1) {
//            if (
//            // Cant place lower door
//                colored[x, y, z] != -1 ||
//                // Cant place upper door
//                colored[x, y + 1, z] != -1 ||
//                // Block below
//                (y > 0 && colored[x, y - 1, z] != -1) ||
//                // Block above
//                (y < colored.height - 2 && colored[x, y + 2, z] != -1)
//            ) continue
//
//            // There are two different colors in x axis and two walls in z axis
//            if (
//                colored[x - 1, y, z] != -1 && colored[x + 1, y, z] != -1 && colored[x - 1, y, z] != colored[x + 1, y, z] &&
//                colored[x - 1, y + 1, z] != -1 && colored[x + 1, y + 1, z] != -1 &&
//
//                // There should be floor diagonally
//                (y == 0 || colored[x - 1, y - 1, z] == -1 && colored[x + 1, y - 1, z] == -1) &&
//
//                colored[x, y, z - 1] == -1 && colored[x, y, z + 1] == -1 &&
//                colored[x, y + 1, z - 1] == -1 && colored[x, y + 1, z + 1] == -1
//            ) {
//                val id = Pair(colored[x - 1, y, z], colored[x + 1, y, z])
//                if (id !in walls) walls[id] = mutableListOf()
//
//                walls[id]!!.add(Triple(x, y, z))
//            }
//
//            // There are two different colors in z axis and two walls in x axis
//            if (
//                colored[x, y, z - 1] != -1 && colored[x, y, z + 1] != -1 && colored[x, y, z - 1] != colored[x, y, z + 1] &&
//                colored[x, y + 1, z - 1] != -1 && colored[x, y + 1, z + 1] != -1 &&
//
//                // There should be floor diagonally
//                (y == 0 || colored[x, y - 1, z - 1] == -1 && colored[x, y - 1, z + 1] == -1) &&
//
//                colored[x - 1, y, z] == -1 && colored[x + 1, y, z] == -1 &&
//                colored[x - 1, y + 1, z] == -1 && colored[x + 1, y + 1, z] == -1
//            ) {
//                val id = Pair(colored[x, y, z - 1], colored[x, y, z + 1])
//                if (id !in walls) walls[id] = mutableListOf()
//
//                walls[id]!!.add(Triple(x, y, z))
//            }
//        }
//
//        return Iterable {
//            iterator {
//                val processedColors: MutableSet<Int> = mutableSetOf()
//                for (entry in walls) {
//                    if (entry.key.first in processedColors && entry.key.second in processedColors) continue
//
//                    val chosen = random.nextInt(entry.value.size)
//                    yield(entry.value[chosen])
//
//                    processedColors.add(entry.key.first)
//                    processedColors.add(entry.key.second)
//                }
//            }
//        }.toList()
//    }
//
//    private fun mergeColors(
//        colored: IntArray3D,
//        coloredDoors: MutableSet<Pair<Pair<Int, Int>, Triple<Int, Int, Int>>>
//    ): List<Int> {
//        for (door in coloredDoors) {
//            val m = min(door.first.first, door.first.second)
//            val s = max(door.first.first, door.first.second)
//
//            // Doors could lead to a wall / end of map
//            if (m == -1 || s == -1) continue
//
//            for (i in colored.indices) {
//                if (colored[i] == s) {
//                    colored[i] = m
//                }
//            }
//        }
//
//        // -1 are walls
//        return colored.distinct().filter { it != -1 }
//    }
//
//    /**
//     * FIXME: This function could be optimized, but it won't help much
//     */
//    private fun removeDoors(
//        doorsToRemove: Iterable<Triple<Int, Int, Int>>,
//        coloredDoors: MutableSet<Pair<Pair<Int, Int>, Triple<Int, Int, Int>>>,
//        colored: IntArray3D,
//        blackWhiteColored: IntArray3D,
//        doorLocations: MutableSet<Triple<Int, Int, Int>>,
//        house: Array<Array<Array<Block>>>
//    ) {
//        removeHouseDoors(doorsToRemove, house)
//
//        // From maps
//        for (door in doorsToRemove) {
//            // Put in wall
//            colored[door.first, door.second, door.third] = -1
//            // Also to the upper block (we receive only lower parts of doors)
//            colored[door.first, door.second + 1, door.third] = -1
//
//            blackWhiteColored[door.first, door.second, door.third] = BlockType.WALL.id
//            blackWhiteColored[door.first, door.second + 1, door.third] = BlockType.WALL.id
//        }
//
//        // From door locations
//        doorLocations.removeAll { it in doorsToRemove }
//
//        // From colored doors
//        coloredDoors.removeAll { it.second in doorsToRemove }
//    }
//
//    private fun removeHouseDoors(doorsToRemove: Iterable<Triple<Int, Int, Int>>, house: Array<Array<Array<Block>>>) {
//        println("Removing from the house..? ${doorsToRemove.count()}")
//        // From the house
//        for (door in doorsToRemove) {
//            println("Removing $door")
//
//            val lowerSurroundingBlock = getSurroundingBlock(house, door)
//            val upperSurroundingBlock = getSurroundingBlock(house, door.copy(second = door.second + 1))
//
//            println("Replacing with '${lowerSurroundingBlock.serialize()}' and '${upperSurroundingBlock.serialize()}'")
//
//            house[door.first][door.second][door.third] = lowerSurroundingBlock
//            house[door.first][door.second + 1][door.third] = upperSurroundingBlock
//        }
//    }
//
//    private fun getSurroundingBlock(house: Array<Array<Array<Block>>>, position: Triple<Int, Int, Int>): Block {
//        return if (position.first > 0 && house[position.first - 1][position.second][position.third].block !in TRANSPARENT)
//            house[position.first - 1][position.second][position.third]
//        else if (position.first < house.size - 1 && house[position.first + 1][position.second][position.third].block !in TRANSPARENT)
//            house[position.first + 1][position.second][position.third]
//        else if (position.third > 0 && house[position.first][position.second][position.third - 1].block !in TRANSPARENT)
//            house[position.first][position.second][position.third - 1]
//        else if (position.third < house[0][0].size - 1 && house[position.first][position.second][position.third + 1].block !in TRANSPARENT)
//            house[position.first][position.second][position.third + 1]
//        else throw Error("The door block was missing surroundings O.o")
//    }
//
//    private fun doorsToRemove(
//        coloredDoors: MutableSet<Pair<Pair<Int, Int>, Triple<Int, Int, Int>>>,
//        totalColors: Int,
//        random: Random
//    ): Iterable<Triple<Int, Int, Int>> {
//        return Iterable {
//            iterator {
//                if (coloredDoors.size == 0) return@iterator
//
//                val sortedDoors = coloredDoors
//                    .map {
//                        Pair(
//                            Pair(
//                                min(it.first.first, it.first.second), max(it.first.first, it.first.second)
//                            ),
//                            it.second
//                        )
//                    }
//                    .sortedBy {
//                        it.first.first * totalColors + it.first.second
//                    }
//
//                var j = 0
//                for (i in sortedDoors.indices) {
//                    if (
//                        sortedDoors[i].first.first == sortedDoors[j].first.first
//                        && sortedDoors[i].first.second == sortedDoors[j].first.second
//                    ) continue
//
//                    val keep = random.nextInt(j, i)
//                    for (k in j until i) if (k != keep) yield(sortedDoors[k].second)
//
//                    j = i
//                }
//
//                val keep = random.nextInt(j, sortedDoors.size)
//                for (k in j until sortedDoors.size) if (k != keep) yield(sortedDoors[k].second)
//            }
//        }.toList()
//    }
//
//    private fun colorDoors(
//        colored: IntArray3D,
//        doorLocations: MutableSet<Triple<Int, Int, Int>>
//    ): MutableSet<Pair<Pair<Int, Int>, Triple<Int, Int, Int>>> {
//        return doorLocations
//            .map {
//                val x = it.first
//                val y = it.second
//                val z = it.third
//
//                val color1 =
//                    if (x > 0 && colored[x - 1, y, z] != -1) colored[x - 1, y, z] else if (z > 0) colored[x, y, z - 1] else -1
//                val color2 =
//                    if (x < colored.width - 1 && colored[x + 1, y, z] != -1) colored[x + 1, y, z] else if (z < colored.depth - 1) colored[x, y, z + 1] else -1
//
//                Pair(Pair(color1, color2), it)
//            }
//            .toMutableSet()
//    }
//
//    private fun locateDoors(colored: IntArray3D): MutableSet<Triple<Int, Int, Int>> {
//        val sizes = intArrayOf(colored.width, colored.height, colored.width)
//
//        // Go through bottom door indices
//        return colored.indices
//            .filter { colored[it] == BlockType.TRANSPARENT.id }
//            .map {
//                val (x, y, z) = it.toCoordinates(sizes)
//                Triple(x, y, z)
//            }
//            .filter {
//                val x = it.first
//                val y = it.second
//                val z = it.third
//                // There should be a door block above, we are working only with bottom blocks
//                y + 1 < colored.height && colored[x, y + 1, z] == BlockType.TRANSPARENT.id
//                        // Also, there should be a wall below and above the door (or end of the area)
//                        && (y == 0 || colored[x, y - 1, z] == BlockType.WALL.id)
//                        && (y + 1 == colored.height - 1 || colored[x, y + 2, z] == BlockType.WALL.id)
//                        // Also, there should be walls around in x coordinates for the both door blocks
//                        && (
//                        // bottom
//                        ((x == 0 || colored[x - 1, y, z] == BlockType.WALL.id)
//                                && (x == colored.width - 1 || colored[x + 1, y, z] == BlockType.WALL.id)
//                                // top
//                                && (x == 0 || colored[x - 1, y + 1, z] == BlockType.WALL.id)
//                                && (x == colored.width - 1 || colored[x + 1, y + 1, z] == BlockType.WALL.id))
//                                // Or in z coordinates
//                                || (
//                                // bottom
//                                (z == 0 || colored[x, y, z - 1] == BlockType.WALL.id)
//                                        && (z == colored.depth - 1 || colored[x, y, z + 1] == BlockType.WALL.id)
//                                        // top
//                                        && (z == 0 || colored[x, y + 1, z - 1] == BlockType.WALL.id)
//                                        && (z == colored.depth - 1 || colored[x, y + 1, z + 1] == BlockType.WALL.id))
//                        )
//            }
//            .toMutableSet()
//    }
//
//    private fun color(
//        colored: IntArray3D,
//        blackWhiteColored: IntArray3D,
//        latestColor: Int,
//        x: Int,
//        y: Int,
//        z: Int
//    ) {
//        colored[x, y, z] = latestColor
//
//        if (x + 1 < colored.width && colored[x + 1, y, z] == -1 && blackWhiteColored[x + 1, y, z] == BlockType.TRANSPARENT.id) {
//            color(colored, blackWhiteColored, latestColor, x + 1, y, z)
//        }
//        if (x > 0 && colored[x - 1, y, z] == -1 && blackWhiteColored[x - 1, y, z] == BlockType.TRANSPARENT.id) {
//            color(colored, blackWhiteColored, latestColor, x - 1, y, z)
//        }
//        if (y + 1 < colored.height && colored[x, y + 1, z] == -1 && blackWhiteColored[x, y + 1, z] == BlockType.TRANSPARENT.id) {
//            color(colored, blackWhiteColored, latestColor, x, y + 1, z)
//        }
//        if (y > 0 && colored[x, y - 1, z] == -1 && blackWhiteColored[x, y - 1, z] == BlockType.TRANSPARENT.id) {
//            color(colored, blackWhiteColored, latestColor, x, y - 1, z)
//        }
//        if (z + 1 < colored.depth && colored[x, y, z + 1] == -1 && blackWhiteColored[x, y, z + 1] == BlockType.TRANSPARENT.id) {
//            color(colored, blackWhiteColored, latestColor, x, y, z + 1)
//        }
//        if (z > 0 && colored[x, y, z - 1] == -1 && blackWhiteColored[x, y, z - 1] == BlockType.TRANSPARENT.id) {
//            color(colored, blackWhiteColored, latestColor, x, y, z - 1)
//        }
//    }
//}