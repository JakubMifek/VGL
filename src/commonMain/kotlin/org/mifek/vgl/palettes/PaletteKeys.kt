package org.mifek.vgl.palettes

import org.mifek.wfc.utils.rgbToInt

@ExperimentalUnsignedTypes
enum class PaletteKeys(val id: Int) {
    GROUND(rgbToInt(255u, 255u, 255u)),
    FLOOR(rgbToInt(112u, 112u, 112u)),
    WALL(rgbToInt(0u, 0u, 0u)),
    DOORS(rgbToInt(213u, 213u, 213u));

    companion object {
        private val map = HashMap<Int, PaletteKeys>()

        @Throws(Error::class)
        fun getById(id: Int): PaletteKeys {
            if (id !in map.keys) {
                map[id] = values().find { it.id == id } ?: throw Error("PaletteKey with ID $id was not found.")
            }

            return map[id]!!
        }
    }
}