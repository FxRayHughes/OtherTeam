package ray.mintcat.otherteam.utils

import io.izzel.taboolib.TabooLibAPI
import org.bukkit.OfflinePlayer

class Money(val player: OfflinePlayer) {

    val now = TabooLibAPI.getPluginBridge().economyLook(player)

    fun give(value: Double) {
        TabooLibAPI.getPluginBridge().economyGive(player,value)
    }

    fun take(value: Double): Boolean {
        if (has(value)) {
            TabooLibAPI.getPluginBridge().economyTake(player,value)
            return true
        }
        return false
    }

    fun set(value: Double) {
        TabooLibAPI.getPluginBridge().economyTake(player,now)
        TabooLibAPI.getPluginBridge().economyGive(player,value)
    }

    fun has(value: Double): Boolean {
        return now >= value
    }

}