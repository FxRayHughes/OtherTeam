package io.izzel.taboolib.other.team.gui

import io.izzel.taboolib.internal.xseries.XMaterial
import io.izzel.taboolib.module.locale.chatcolor.TColor
import io.izzel.taboolib.other.team.OtherTeam
import io.izzel.taboolib.other.team.utils.Helper
import io.izzel.taboolib.util.item.ItemBuilder
import io.izzel.taboolib.util.item.Items
import io.izzel.taboolib.util.item.inventory.MenuBuilder
import org.bukkit.Material
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import java.io.File

interface GUIInterface : Helper {

    /**
     * 页面ID
     */
    val id: String


    val config: YamlConfiguration
        get() = YamlConfiguration.loadConfiguration(File(OtherTeam.plugin.dataFolder, "guis/${id}.yml"))

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
        GUI.menus.add(this)
    }

    companion object : Helper {
        fun getMenuBuilder(player: Player, data: GUIInterface): MenuBuilder {
            return MenuBuilder.builder().also { menu ->
                menu.title(data.title)
                menu.items(*data.slots.toTypedArray())
                data.config.getConfigurationSection("info")?.getKeys(false)?.forEach {
                    menu.put(it.toCharArray()[0], itemCreate(it, player, data))
                }
            }
        }

        fun itemCreate(slot: String, player: Player, data: GUIInterface): ItemStack {
            val config = data.config.getConfigurationSection("info.$slot") ?: return ItemStack(Material.AIR)
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
}