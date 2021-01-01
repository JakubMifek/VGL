package org.mifek.vgl.implementations

import org.mifek.vgl.PROPS_SEPARATOR
import org.mifek.vgl.VALUE_SEPARATOR
import org.mifek.vgl.hashRegex

open class Block(val block: Blocks, val props: HashMap<String, Any>) {
    fun serialize(): String {
        return "${block.id}$VALUE_SEPARATOR${props.map { "${it.key}$PROPS_SEPARATOR${it.value}" }.joinToString("$VALUE_SEPARATOR")}"
    }

    companion object {
        @Throws(Error::class)
        fun deserialize(data: String): Block {
            if(!data.matches(hashRegex)) {
                throw Error("Passed data did not contain a serialized Block. Expected pattern: '${hashRegex.pattern}'. Received: '$data'.")
            }
            val parts = data.split(VALUE_SEPARATOR)
            val id = parts[0]
            val props = HashMap<String, Any>()
            parts.subList(1, parts.size)
                    .map { it.split(PROPS_SEPARATOR) }
                    .forEach { props[it[0]] = it[1] }

            return Block(Blocks.getById(id), props)
        }
    }
}