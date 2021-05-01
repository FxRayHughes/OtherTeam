package ray.mintcat.otherteam.team.ui.main.actions

import io.izzel.taboolib.kotlin.sendLocale
import io.izzel.taboolib.module.inject.TFunction
import org.bukkit.entity.Player
import ray.mintcat.otherteam.team.TeamData
import ray.mintcat.otherteam.ui.ActionInterface
import ray.mintcat.wizardfix.Data

object LeaveTeamAction : ActionInterface {

    @TFunction.Init
    private fun init() {
        register()
    }

    override val ui: String
        get() = "main"

    override val key: String
        get() = "L"

    override fun <T> run(player: Player, vararg other: T) {
        val data = other[0]
        if (data is TeamData) {
            eval(player, data)
        }
    }

    private fun eval(player: Player, data: TeamData) {
        data.leaveTeam(player.uniqueId)
        data.sendMessage("command-team-player-left-message-to-all", player.name)
        player.closeInventory()
        Data(player).edit("chat-room", "=", "0")
        player.sendLocale("command-team-player-left-message-to-single")
        player.sendLocale("command-team-player-auto-switching-channel-to-default")
    }
}