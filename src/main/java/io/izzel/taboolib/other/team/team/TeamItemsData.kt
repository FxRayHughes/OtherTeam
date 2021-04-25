package io.izzel.taboolib.other.team.team

import org.bukkit.inventory.ItemStack
import java.util.*

class TeamItemsData(
    val id: UUID,
    val item: ItemStack,
    var money: Double = 0.0,
) {

    companion object {
        fun createData(item: ItemStack): TeamItemsData {
            return TeamItemsData(UUID.randomUUID(), item)
        }
    }

}