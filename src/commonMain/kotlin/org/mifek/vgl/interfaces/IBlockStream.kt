package org.mifek.vgl.interfaces

import org.mifek.vgl.implementations.PlacedBlock

interface IBlockStream {
    fun add(block: PlacedBlock)
}