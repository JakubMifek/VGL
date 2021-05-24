package org.mifek.vgl.wfc

import org.mifek.vgl.implementations.Blocks

data class DebugOptions(
    val undeterminedBlock: Blocks = Blocks.BEACON,
    val verbose: Boolean = false,
    val seed: Int? = null,
)
