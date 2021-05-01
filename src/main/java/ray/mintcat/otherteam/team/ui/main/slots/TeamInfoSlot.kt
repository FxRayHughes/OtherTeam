package ray.mintcat.otherteam.team.ui.main.slots

import io.izzel.taboolib.module.inject.TListener
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import ray.mintcat.otherteam.team.Team
import ray.mintcat.otherteam.team.ui.main.TeamUI
import ray.mintcat.otherteam.ui.event.OtherTeamPutUIItemEvent
import ray.mintcat.otherteam.utils.Helper

@TListener
class TeamInfoSlot : Listener, Helper {

    @EventHandler
    fun e(event: OtherTeamPutUIItemEvent) {
        if (event.uiID != "main" || event.key != "A") {
            return
        }
        val team = Team.getTeam(event.player.uniqueId) ?: return
        val format = TeamUI.config.getString("format.tell", " §7- §f{info}")!!
        val item = event.itemStack.replace("{tell}", team.tell.map { format.replace("{info}", it) })
        event.itemStack = item
    }

}