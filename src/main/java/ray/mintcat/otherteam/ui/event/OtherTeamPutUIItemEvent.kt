package ray.mintcat.otherteam.ui.event

import io.izzel.taboolib.module.event.EventCancellable
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

class OtherTeamPutUIItemEvent(
    var uiID: String,
    var key: String,
    var itemStack: ItemStack,
    val player: Player,
) : EventCancellable<OtherTeamPutUIItemEvent>()