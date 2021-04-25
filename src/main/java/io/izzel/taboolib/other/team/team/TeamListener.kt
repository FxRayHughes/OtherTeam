package io.izzel.taboolib.other.team.team

import io.izzel.taboolib.module.inject.TListener
import io.izzel.taboolib.other.team.utils.Helper
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityPickupItemEvent

@TListener
class TeamListener : Listener, Helper {

    @EventHandler
    fun onEntityDamageEntity(event: EntityDamageByEntityEvent) {
        if (Team.isEnable()) {
            val damagerTeam = Team.getTeam(event.damager.uniqueId) ?: return
            val entityTeam = Team.getTeam(event.entity.uniqueId) ?: return
            if (damagerTeam == entityTeam) {
                if (!damagerTeam.pvp) {
                    event.isCancelled = true
                }
            }
        }
    }

    @EventHandler
    fun onPickItem(event: EntityPickupItemEvent) {
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