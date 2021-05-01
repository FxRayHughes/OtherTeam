package ray.mintcat.otherteam.utils

import io.izzel.taboolib.TabooLibAPI
import io.izzel.taboolib.module.i18n.I18n
import io.izzel.taboolib.module.inject.TInject
import io.izzel.taboolib.module.locale.chatcolor.TColor
import io.izzel.taboolib.util.Coerce
import io.izzel.taboolib.util.item.ItemBuilder
import io.izzel.taboolib.util.item.Items
import io.izzel.taboolib.util.lite.cooldown.Cooldown
import org.bukkit.Bukkit
import org.bukkit.Sound
import org.bukkit.attribute.Attribute
import org.bukkit.command.CommandSender
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import ray.mintcat.otherteam.OtherTeam
import java.util.*

/**
 * @Author sky
 * @Since 2020-01-05 23:19
 */
interface Helper {

    fun String.toPlayer(): Player? {
        return player(this)
    }

    fun ItemStack.replace(oldLore: String, newLore: List<String>): ItemStack {
        if (!Items.hasLore(this, oldLore)) {
            return this
        }
        val lore = this.itemMeta?.lore ?: return this
        val new = mutableListOf<String>()
        for (s in lore) {
            if (s != oldLore) {
                new.add(s)
                break
            }
            new.addAll(newLore)
        }
        val item = ItemBuilder(this)
        item.lore(new)
        item.colored()
        return item.build()
    }

    fun Entity.getCName(): String {
        if (this is Player) {
            return this.name
        }
        if (this.customName != null) {
            return this.customName!!
        }
        return I18n.get().getName(this)
    }

    fun UUID.toPlayer(): Player? {
        return Bukkit.getPlayer(this)
    }

    fun String.toPapi(player: Player): String {
        return TabooLibAPI.getPluginBridge().setPlaceholders(player, this)
    }

    fun List<String>.toPapi(player: Player): List<String> {
        return TabooLibAPI.getPluginBridge().setPlaceholders(player, this)
    }

    fun Double.toTwo(): Double {
        return Coerce.format(this)
    }

    fun String.screen(): String {
        return this.replace("[^A-Za-z0-9\\u4e00-\\u9fa5_]".toRegex(), "")
    }

    fun String.process(): String {
        return TColor.translate(this).replace("true", "§a开启§7").replace("false", "§c关闭§7").replace("null", "空")
    }

    fun List<String>.process(): List<String> {
        return this.map { it.process() }
    }

    fun heal(player: Player) {
        player.health = player.getAttribute(Attribute.GENERIC_MAX_HEALTH)?.value ?: 20.0
        player.foodLevel = 20
        player.fireTicks = 0
        player.activePotionEffects.forEach { player.removePotionEffect(it.type) }
    }

    fun player(name: String): Player? {
        return Bukkit.getPlayerExact(name)
    }

    fun toConsole(message: String) {
        Bukkit.getConsoleSender().sendMessage("§8[§c Other §8] §7${message.replace("&", "§")}")
    }

    fun run(runnable: () -> (Unit)) {
        Bukkit.getScheduler().runTask(OtherTeam.plugin, Runnable { runnable.invoke() })
    }

    fun runAsync(runnable: () -> (Unit)) {
        Bukkit.getScheduler().runTaskAsynchronously(OtherTeam.plugin, Runnable { runnable.invoke() })
    }

    companion object {

        @TInject
        val cooldown = Cooldown("piv:sound", 100)
    }
}