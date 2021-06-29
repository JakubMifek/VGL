package org.mifek.vgl.wfc

import org.mifek.wfc.models.options.Cartesian2DModelOptions

data class MinecraftVillageAdapterOptions(
    val modelOptions: Cartesian2DModelOptions = Cartesian2DModelOptions(
        allowRotations = true,
        allowHorizontalFlips = true, allowVerticalFlips = true,
        grounded = true, roofed = true, leftSided = true, rightSided = true
    ),
    /**
     * name of template - [weight, {minWidth, minHeight, minDepth}]
     */
    val templateOptions: Map<String, Pair<Float, Triple<Int, Int, Int>>> = emptyMap(),
    /**
     * Sets the weight of free space in the village. One house has default collective weight of 57.
     * This value is going to be multiplied by number of desired houses in the village.
     *
     * What this means is that if the value is set to 49 and algorithm is to generate 9 houses in the given area,
     * each of size 400 pixels, there will be together about 3600 pixels of free space in the village between the
     * houses.
     */
    val emptySpaceWeight: Float = 57f,
    /**
     * WARNING: This number is only orientational for the algorithm, the algorithm has no obligation to generate exactly
     * the given number of houses.
     */
    val desiredNumberOfHouses: Int = 12,
    val seed: Int? = null,
)