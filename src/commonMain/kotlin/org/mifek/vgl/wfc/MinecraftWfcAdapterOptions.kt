package org.mifek.vgl.wfc

import org.mifek.vgl.implementations.Block
import org.mifek.vgl.implementations.Blocks
import org.mifek.wfc.models.options.Cartesian3DModelOptions

data class MinecraftWfcAdapterOptions(
    val overlap: Int = 2,
    val fixedBlocks: Iterable<Pair<Triple<Int, Int, Int>, Block>>? = null,
    val modelOptions: Cartesian3DModelOptions = Cartesian3DModelOptions(),
    val debugOptions: DebugOptions? = null,
    val repeats: Int = 1,
    val streamOptions: StreamOptions? = null,
    val name: String? = null,
)
