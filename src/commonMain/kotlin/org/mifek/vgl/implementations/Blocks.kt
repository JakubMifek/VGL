package org.mifek.vgl.implementations

enum class Blocks(val id: String) {
    NONE(""),
    AIR("air"),
    DIRT("dirt"),
    PLANKS("planks"),
    LOG("log"),
    OAK_STAIRS("oak_stairs"),
    GLASS_PANE("glass_pane"),
    WOODEN_DOOR("wooden_door"),
    WOODEN_SLAB("wooden_slab"),
    TORCH("torch");

    fun toBlock(): Block {
        return Block(this, HashMap())
    }

    companion object {
        private val map= HashMap<String, Blocks>()

        @Throws(Error::class)
        fun getById(id: String): Blocks {
            if (id !in map.keys) {
                map[id] = values().find { it.id.equals(id) } ?: throw Error("Block with ID $id was not found.")
            }

            return map[id]!!
        }
    }
}