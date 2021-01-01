package org.mifek.vgl.implementations

import org.mifek.vgl.interfaces.IArea

data class Area(
        override val x: Int,
        override val y: Int,
        override val z: Int,
        override val width: Int,
        override val height: Int,
        override val depth: Int
) : IArea