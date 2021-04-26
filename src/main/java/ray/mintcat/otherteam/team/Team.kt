package ray.mintcat.otherteam.team

import io.izzel.taboolib.module.db.local.LocalFile
import io.izzel.taboolib.module.inject.TFunction
import io.izzel.taboolib.module.inject.TSchedule
import io.izzel.taboolib.util.item.Items
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.entity.Player
import ray.mintcat.otherteam.team.ui.TeamListUI
import ray.mintcat.otherteam.utils.Helper
import java.util.*
import kotlin.collections.HashMap

object Team : Helper {

    @LocalFile("module/teams.yml")
    lateinit var data: FileConfiguration
        private set

    val itemAccept = HashMap<UUID, Player>()

    val teams = LinkedList<TeamData>()

    fun isEnable(): Boolean {
        return true
    }

    fun getTeam(member: UUID): TeamData? {
        return teams.firstOrNull { it.isMember(member) }
    }

    fun create(data: TeamData) {
        teams.add(data)
        data.member.remove(data.admin)
        save()
    }

    fun openListTeam(player: Player) {
        TeamListUI.openListTeam(player).open(player)
    }

    @TSchedule
    fun load() {
        teams.clear()
        data.getKeys(false).forEach { key ->
            val data = TeamData(
                UUID.fromString(key),
                data.getString("${key}.name")!!,
                data.getStringList("${key}.tell"),
                data.getBoolean("${key}.pvp"),
                data.getStringList("${key}.member").map { UUID.fromString(it) }.toMutableList(),
                data.getDouble("${key}.money"),
                data.getStringList("${key}.items").map { TeamItemsData.createData(Items.fromJson(it)!!) }.toMutableList(),
                data.getBoolean("${key}.itemType"),
                data.getString("${key}.chatRoom")!!,
                data.getStringList("${key}.joinList").map { UUID.fromString(it) }.toMutableList()
            )
            teams.add(data)
            save()
        }
    }


    fun remove(admin: UUID) {
        data.set(admin.toString(), null)
        load()
    }

    @TFunction.Cancel
    fun save() {
        data.getKeys(false).forEach { data.set(it, null) }
        teams.forEach { teamData ->
            data.set("${teamData.admin}.name", teamData.name)
            data.set("${teamData.admin}.tell", teamData.tell)
            data.set("${teamData.admin}.pvp", teamData.pvp)
            data.set("${teamData.admin}.member", teamData.member.map { it.toString() })
            data.set("${teamData.admin}.money", teamData.money)
            data.set("${teamData.admin}.items", teamData.items.map { Items.toJson(it.item) })
            data.set("${teamData.admin}.itemType", teamData.itemType)
            data.set("${teamData.admin}.chatRoom", teamData.chatRoom)
            data.set("${teamData.admin}.joinList", teamData.joinList.map { it.toString() })
        }
    }

}