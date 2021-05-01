package ray.mintcat.otherteam.ui

import io.izzel.taboolib.internal.xseries.XMaterial
import io.izzel.taboolib.util.item.ItemBuilder
import io.izzel.taboolib.util.item.inventory.MenuBuilder
import org.bukkit.Material
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import ray.mintcat.otherteam.OtherTeam
import ray.mintcat.otherteam.ui.event.OtherTeamPutUIItemEvent
import ray.mintcat.otherteam.utils.Helper
import java.io.File

interface UIInterface : Helper {

    /**
     * 页面ID
     */
    val id: String

    val config: YamlConfiguration
        get() = YamlConfiguration.loadConfiguration(File(OtherTeam.plugin.dataFolder, "ui/${id}.yml"))

    /**
     * 页面名称
     */
    val title: String
        get() = config.getString("title", id)!!

    val slots: List<String>
        get() = config.getStringList("slots")

    val size: Int
        get() = slots.size

    fun register() {
        UI.menus.add(this)
    }

    fun actions(){

    }

    fun getMenuBuilder(player: Player): MenuBuilder {
        return MenuBuilder.builder().also { menu ->
            menu.title(this.title)
            menu.rows(size)
            menu.items(*this.slots.toTypedArray())
            this.config.getConfigurationSection("info")?.getKeys(false)?.forEach {
                val event = OtherTeamPutUIItemEvent(id, it, itemCreate(it, player),player)
                event.ifCancelled {
                    menu.put(it.toCharArray()[0], ItemStack(Material.AIR))
                }
                menu.put(event.key.toCharArray()[0], event.itemStack)
            }
        }
    }

    fun itemCreate(slot: String, player: Player): ItemStack {
        val config = this.config.getConfigurationSection("info.$slot") ?: return ItemStack(Material.AIR)
        return ItemBuilder(XMaterial.valueOf(config.getString("type", "APPLE")!!)).also { itemBuilder ->
            itemBuilder.amount(config.getInt("amount", 0))
            itemBuilder.name(config.getString("name", " ")?.toPapi(player))
            itemBuilder.lore(config.getStringList("lore").toPapi(player))
            itemBuilder.damage(config.getInt("damage", 0))
            itemBuilder.flags(*config.getStringList("flags").map { ItemFlag.valueOf(it) }.toTypedArray())
            itemBuilder.skullOwner(config.getString("owner"))
            itemBuilder.colored()
        }.build()
    }
}