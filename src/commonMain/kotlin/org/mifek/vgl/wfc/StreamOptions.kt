package org.mifek.vgl.wfc

import org.mifek.vgl.implementations.PlacementStyle
import org.mifek.vgl.interfaces.IArea
import org.mifek.vgl.interfaces.IBlockStream

data class StreamOptions(
    val stream: IBlockStream,
    val area: IArea,
    val placementStyle: PlacementStyle = PlacementStyle.ON_FINISH
)
