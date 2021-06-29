package org.mifek.vgl.wfc

import org.mifek.vgl.implementations.Block
import org.mifek.vgl.implementations.Blocks
import org.mifek.wfc.models.options.Cartesian2DModelOptions
import org.mifek.wfc.models.options.Cartesian3DModelOptions

data class FloorPlanOptions(
    val overlap: Int = 2,
    val setBlocks: Iterable<Pair<Pair<Int, Int>, Block>>? = null,
    val bannedBlocks: Iterable<Pair<Pair<Int, Int>, Block>>? = null,
    val modelOptions: Cartesian2DModelOptions = Cartesian2DModelOptions(),
    val debugOptions: DebugOptions? = null,
    val repeats: Int = 1,
    val streamOptions: StreamOptions? = null,
    val name: String? = null,
)
