package ray.mintcat.otherteam.team

import io.izzel.taboolib.module.command.base.*
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
                player.error("您已经身处一个队伍中了,无法再创建一个队伍,请退当前队伍!")
                return
            }
            Team.create(TeamData(player.uniqueId, args[0].screen()))
            player.info("成功创建了一个队伍(${args[0].screen()}).")
            player.info("快输入&f /team gui&7 打开队伍面板吧")
        }
    }

    @SubCommand(description = "打开队伍面板")
    var gui: BaseSubCommand = object : BaseSubCommand() {

        override fun onCommand(sender: CommandSender, command: Command, s: String, args: Array<String>) {
            val player = sender as? Player ?: return
            val teamData = Team.getTeam(player.uniqueId)
            if (teamData == null) {
                player.error("您不在队伍中无法打开,请加入或创建一个队伍!")
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
                player.error("您已经在一个队伍中了!")
                return
            }
            val accepts = Data(player).get("TeamAccept", "no")
            if (accepts == "no") {
                player.error("没有待处理的申请!")
                return
            }
            val team = Team.getTeam(UUID.fromString(accepts))
            if (team == null) {
                player.error("对方的队伍不存在了!")
                return
            }
            if (team.member.size >= 4) {
                player.error("对方的队伍人数已满")
                return
            }
            team.addMember(player.uniqueId)
            team.sendMessage("玩家 §f${player.name} §7加入了队伍!")
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
                player.error("您不在队伍中无法打开,请加入或创建一个队伍!")
                return
            }
            teamData.sendMessage("队员 §f${player.name}§7 Roll了一次骰子摇出了 §e${(0..999).random()}§8 点")
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
                player.error("您不在队伍中")
                return
            }
            val uuid = UUID.fromString(args[0])
            val accepts = Team.itemAccept[uuid]
            if (accepts != player) {
                player.error("该物品不属于你,无法获取!")
                return
            }
            val item = teamData.items.firstOrNull { it.id == uuid }
            if (item == null) {
                player.error("物品不存在了!")
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