package ray.mintcat.otherteam.team

import io.izzel.taboolib.module.inject.TListener
import io.izzel.taboolib.util.lite.Servers
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityPickupItemEvent
import ray.mintcat.otherteam.utils.Helper

@TListener
class TeamListener : Listener, Helper {

    @EventHandler
    fun e(event: EntityDamageByEntityEvent) {
        if (Team.isEnable()) {
            val attacker = Servers.getAttackerInDamageEvent(event) ?: return
            val attackerTeam = Team.getTeam(attacker.uniqueId) ?: return
            val entityTeam = Team.getTeam(event.entity.uniqueId) ?: return
            if (attackerTeam == entityTeam && !attackerTeam.pvp) {
                event.isCancelled = true
            }
        }
    }

    @EventHandler
    fun e(event: EntityPickupItemEvent) {
        val team = Team.getTeam(event.entity.uniqueId) ?: return
        if (!team.itemType) {
            return
        }
        if (team.items.size >= 53){
            team.switchItemType()
            team.sendMessage("仓库已满,自动关闭收集系统! 请尽早分配!")
            return
        }
        event.isCancelled = true
        team.items.add(TeamItemsData.createData(event.item.itemStack))
        Team.save()
        event.item.remove()
    }

}