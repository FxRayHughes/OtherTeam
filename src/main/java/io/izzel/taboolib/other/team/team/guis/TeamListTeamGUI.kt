package io.izzel.taboolib.other.team.team.guis

import io.izzel.taboolib.other.team.utils.Helper
import io.izzel.taboolib.other.team.team.Team
import io.izzel.taboolib.other.team.OtherTeam
import io.izzel.taboolib.util.item.ItemBuilder
import io.izzel.taboolib.util.item.Items
import io.izzel.taboolib.util.item.inventory.MenuBuilder
import org.bukkit.Material
import org.bukkit.entity.Player
import ray.mintcat.wizardfix.PlayerUtil

object TeamListTeamGUI: Helper {

    fun openListTeam(player: Player): MenuBuilder {
        val menu = MenuBuilder.builder(OtherTeam.plugin)
        menu.rows(6).title("队伍列表")
            .build { inv ->
                Team.teams.forEach { teamData ->
                    val item = ItemBuilder(Material.SKULL_BANNER_PATTERN)
                    item.name("&7队伍名称: &f${teamData.name}")
                    val lore = mutableListOf<String>()
                    lore.add("§7队长: §f${teamData.admin.toPlayer()!!.name}")
                    lore.add("§7队伍成员:")
                    lore.addAll(teamData.member.map { "§7 -§f ${it.toPlayer()!!.name}" })
                    lore.add("")
                    lore.add("§7队伍宣言:")
                    lore.addAll(teamData.tell.map { " §7- §f$it" })
                    lore.add("")
                    lore.add("§8点击申请加入队伍!")
                    item.lore(lore)
                    item.colored()
                    inv.addItem(item.build())
                }

            }
        menu.event { event ->
            event.isCancelled = true
            if (!Items.hasLore(event.currentItem, "点击申请加入队伍!")) {
                return@event
            }
            if (Team.getTeam(player.uniqueId) != null) {
                player.error("你已经在一个队伍当中了!")
                return@event
            }
            val item = event.currentItem ?: return@event
            val adminName =
                item.itemMeta?.lore?.firstOrNull { it.contains("§7队长: §f") }?.replace("§7队长: §f", "") ?: return@event
            val adminUUID = PlayerUtil.getOfflinePlayer(adminName)?.uniqueId ?: return@event
            val team = Team.getTeam(adminUUID)
            if (team == null) {
                player.error("该队伍不存在!")
                return@event
            }
            if (team.member.size >= 4) {
                player.error("该队伍以满员!")
                return@event
            }
            if (team.joinList.contains(player.uniqueId)) {
                player.error("您已经申请了该队伍!")
                return@event
            }
            team.joinList.add(player.uniqueId)
            team.admin.toPlayer()!!.info("玩家 &f${player.name} &7申请加入队伍!")
        }
        return menu
    }

}