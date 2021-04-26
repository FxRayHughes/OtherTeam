package ray.mintcat.otherteam.team

import io.izzel.taboolib.kotlin.sendLocale
import io.izzel.taboolib.module.command.base.*
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import ray.mintcat.otherteam.utils.Helper
import ray.mintcat.otherteam.utils.Money
import ray.mintcat.wizardfix.Data
import java.util.*

@BaseCommand(name = "team", permission = "team.use")
class TeamCommand : BaseMainCommand(), Helper {

    @SubCommand(description = "创建队伍")
    var create: BaseSubCommand = object : BaseSubCommand() {

        override fun getArguments(): Array<Argument> {
            return arrayOf(Argument("队伍名称"))
        }

        override fun onCommand(sender: CommandSender, command: Command, s: String, args: Array<String>) {
            val player = sender as? Player ?: return
            val teamData = Team.getTeam(player.uniqueId)
            if (teamData != null) {
                player.sendLocale("command-team-already-in")
                return
            }
            Team.create(TeamData(player.uniqueId, args[0].screen()))
            player.sendLocale("command-team-successfully-created", args[0].screen())
        }
    }

    @SubCommand(description = "打开队伍面板")
    var gui: BaseSubCommand = object : BaseSubCommand() {

        override fun onCommand(sender: CommandSender, command: Command, s: String, args: Array<String>) {
            val player = sender as? Player ?: return
            val teamData = Team.getTeam(player.uniqueId)
            if (teamData == null) {
                player.sendLocale("command-nonteamer-command-execute-error")
                return
            }
            teamData.openGUI(player)
        }
    }

    @SubCommand(description = "接受队伍邀请")
    var accept: BaseSubCommand = object : BaseSubCommand() {

        override fun onCommand(sender: CommandSender, command: Command, s: String, args: Array<String>) {
            val player = sender as? Player ?: return
            val teamData = Team.getTeam(player.uniqueId)
            if (teamData != null) {
                player.sendLocale("command-team-already-in")
                return
            }
            val accepts = Data(player).get("TeamAccept", "no")
            if (accepts == "no") {
                player.sendLocale("command-team-no-pending-applications")
                return
            }
            val team = Team.getTeam(UUID.fromString(accepts))
            if (team == null) {
                player.sendLocale("command-team-does-not-exists")
                return
            }
            if (team.member.size >= 4) {
                player.sendLocale("command-team-size-fulled")
                return
            }
            team.addMember(player.uniqueId)
            player.sendLocale("command-team-player-joined-message", player.name)
            Data(player).edit("TeamAccept", "=", "no")
            Team.save()
        }
    }

    @SubCommand(description = "打开队伍列表")
    var list: BaseSubCommand = object : BaseSubCommand() {

        override fun onCommand(sender: CommandSender, command: Command, s: String, args: Array<String>) {
            val player = sender as? Player ?: return
            Team.openListTeam(player)
        }
    }

    @SubCommand(description = "Roll点")
    var roll: BaseSubCommand = object : BaseSubCommand() {

        override fun onCommand(sender: CommandSender, command: Command, s: String, args: Array<String>) {
            val player = sender as? Player ?: return
            val teamData = Team.getTeam(player.uniqueId)
            if (teamData == null) {
                player.sendLocale("command-nonteamer-command-execute-error")
                return
            }

            val rollNumber = (0..999).random();

            teamData.getAllMember().forEach { uuid ->
                val player = Bukkit.getPlayer(uuid.uuid)
                if (player != null && player.isOnline) {
                    player.sendLocale("command-team-player-rolled-number", player.name, rollNumber)
                }
            }
        }
    }

    @SubCommand(description = "接受分配的物品")
    var item: BaseSubCommand = object : BaseSubCommand() {
        override fun getArguments(): Array<Argument> {
            return arrayOf(Argument("物品ID"))
        }

        override fun onCommand(sender: CommandSender, command: Command, s: String, args: Array<String>) {
            val player = sender as? Player ?: return
            val teamData = Team.getTeam(player.uniqueId)
            if (teamData == null) {
                player.sendLocale("command-nonteamer-command-execute-error")
                return
            }
            val uuid = UUID.fromString(args[0])
            val accepts = Team.itemAccept[uuid]
            if (accepts != player) {
                player.sendLocale("command-team-item-doesnt-belong-player")
                return
            }
            val item = teamData.items.firstOrNull { it.id == uuid }
            if (item == null) {
                player.sendLocale("command-team-item-doesnt-exists")
                return
            }
            Money(player).take(item.money)
            teamData.giveItem(item.item, player.uniqueId, item.money)
            teamData.items.remove(item)
            teamData.money += item.money
            Team.save()
            teamData.openLib(teamData.admin.toPlayer()!!)
        }
    }

}