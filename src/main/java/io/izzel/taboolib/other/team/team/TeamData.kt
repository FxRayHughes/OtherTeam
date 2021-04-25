package io.izzel.taboolib.other.team.team

import io.izzel.taboolib.cronus.CronusUtils
import io.izzel.taboolib.module.tellraw.TellrawJson
import io.izzel.taboolib.other.team.utils.Helper
import io.izzel.taboolib.other.team.utils.Money
import io.izzel.taboolib.other.team.OtherTeam
import io.izzel.taboolib.other.team.team.guis.TeamMainGUI
import io.izzel.taboolib.util.Features
import io.izzel.taboolib.util.item.ItemBuilder
import io.izzel.taboolib.util.item.Items
import io.izzel.taboolib.util.item.inventory.MenuBuilder
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.OfflinePlayer
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import ray.mintcat.wizardfix.PlayerUtil
import java.util.*
import kotlin.collections.ArrayList

class TeamData(
    val admin: UUID,
    var name: String = "",
    var tell: MutableList<String> = mutableListOf(),
    var pvp: Boolean = false,
    val member: ArrayList<UUID> = arrayListOf(),
    var money: Double = 0.0,
    val items: MutableList<TeamItemsData> = mutableListOf(),
    var itemType: Boolean = false,
    var chatRoom: String = "0",
    val joinList: ArrayList<UUID> = arrayListOf(),
) : Helper {

    val members = arrayListOf<TeamMemberData>()

    init {
        init()
    }

    fun init() {
        member.forEach { uuid ->
            if (!members.map { it.uuid }.contains(uuid)) {
                members.add(TeamMemberData(member.size, uuid))
            }
        }
        Team.save()
    }

    fun addMember(uuid: UUID) {
        member.add(uuid)
        members.add(TeamMemberData(member.size, uuid))
        Team.save()
    }

    fun getAllMember(): ArrayList<TeamMemberData> {
        val list = arrayListOf<TeamMemberData>()
        members.forEach {
            list.add(it)
        }
        list.add(TeamMemberData(0, admin))
        return list
    }

    fun isMember(uuid: UUID): Boolean {
        return getAllMember().map { it.uuid }.contains(uuid)
    }

    fun getFlag(int: Int): ItemStack {
        return when (int) {
            0 -> ItemStack(Material.RED_BANNER)
            1 -> ItemStack(Material.ORANGE_BANNER)
            2 -> ItemStack(Material.YELLOW_BANNER)
            3 -> ItemStack(Material.LIGHT_BLUE_BANNER)
            4 -> ItemStack(Material.WHITE_BANNER)
            else -> ItemStack(Material.WHITE_BANNER)
        }
    }

    fun switchPVP() {
        pvp = !pvp
        Team.save()
    }

    fun switchItemType() {
        itemType = !itemType
        Team.save()
    }

    fun isOnlineInfo(player: OfflinePlayer): String {
        if (player.isOnline) {
            return "§a在线"
        }
        return "§c离线"
    }

    fun leaveTeam(uuid: UUID) {
        if (uuid == admin) {
            val list = member
            if (list.size >= 1) {
                val new = member[0]
                Team.create(TeamData(new, name, tell, pvp, member, money, items))
                Team.remove(admin)
                return
            }
            giveMoney(admin, money)
            items.map { giveItem(it.item, admin) }
            Team.remove(admin)
            return
        }
        member.remove(uuid)
        members.remove(members.firstOrNull { it.uuid == uuid })
        Team.save()
    }

    fun giveMoney(uuid: UUID, value: Double) {
        Money(uuid.toPlayer()!!).give(value)
        uuid.toPlayer()!!.info("获得了分配的 §f$value &7金")
    }

    fun giveItem(itemStack: ItemStack, player: UUID) {
        CronusUtils.addItem(player.toPlayer()!!, itemStack)
        getAllMember().forEach { uuid ->
            val players = Bukkit.getPlayer(uuid.uuid)
            if (players != null && players.isOnline) {
                players.playSound(players.location, Sound.UI_BUTTON_CLICK, 1f, (1..2).random().toFloat())
                TellrawJson.create()
                    .append("§8[§c Other §8] §7队员 §f${player.toPlayer()!!.name} §7获得了战利品 §8[§f${Items.getName(itemStack)}§8]")
                    .hoverItem(itemStack)
                    .send(players)
            }
        }
    }

    fun giveItem(itemStack: ItemStack, player: UUID, money: Double) {
        CronusUtils.addItem(player.toPlayer()!!, itemStack)
        getAllMember().forEach { uuid ->
            val players = Bukkit.getPlayer(uuid.uuid)
            if (players != null && players.isOnline) {
                players.playSound(players.location, Sound.UI_BUTTON_CLICK, 1f, (1..2).random().toFloat())
                TellrawJson.create()
                    .append("§8[§c Other §8] §7队员 §f${player.toPlayer()!!.name} §7以 §f${money}金的价格 §7获得了战利品 §8[§f${
                        Items.getName(itemStack)
                    }§8]")
                    .hoverItem(itemStack)
                    .send(players)
            }
        }
    }

    fun sendMessage(info: String) {
        getAllMember().forEach { uuid ->
            val player = Bukkit.getPlayer(uuid.uuid)
            if (player != null && player.isOnline) {
                player.info(info)
            }
        }
    }


    fun isAdmin(player: Player): Boolean {
        return player.uniqueId == admin
    }

    fun openGUI(player: Player) {
        TeamMainGUI.openGUI(player,this)
    }

    fun openLib(player: Player) {
        val menu = MenuBuilder.builder(OtherTeam.plugin)
        menu.title("§8[§f $name §8] &0战利品面板".process())
            .rows(6)
            .build { inv ->
                inv.setItem(0,
                    ItemBuilder(Material.CAMPFIRE)
                        .name("  ")
                        .lore("§7<- 点击返回队伍面板", "")
                        .colored()
                        .build()
                )
                items.forEach { item ->
                    inv.addItem(ItemBuilder(item.item).lore("",
                        "§fTeamItemID: ${item.id}",
                        "§8左键:§7金币分配物品§f | §8右键:§7Roll点分配物品")
                        .colored().build())
                }
            }
            .event { event ->
                event.isCancelled = true
                val isRight = event.castClick().isRightClick
                val isLeft = event.castClick().isLeftClick
                if (event.rawSlot == 0) {
                    player.closeInventory()
                    openGUI(player)
                    return@event
                }
                if (Items.isNull(event.currentItem)) {
                    return@event
                }
                if (!isAdmin(player)) {
                    player.error("你不是队长,无法进行操作!")
                    return@event
                }
                var id = ""
                event.currentItem!!.itemMeta!!.lore!!.forEach {
                    if (it.startsWith("§fTeamItemID: ")) {
                        id = it.split(": ")[1]
                    }
                }
                val itemData = items.firstOrNull { it.id == UUID.fromString(id) }
                val item = itemData?.item ?: ItemStack(Material.AIR)
                if (isLeft) {
                    Features.inputSign(player, arrayOf("", "§6↑设置总价")) { les ->
                        val value = les[0].replace("[^0-9.]".toRegex(), "").toDoubleOrNull() ?: 0.0
                        Features.inputSign(player, arrayOf("", "§6↑分配目标[成员ID]")) { lea ->
                            val number = lea[0].replace("[^0-9]".toRegex(), "").toIntOrNull() ?: 0
                            val players = getAllMember().firstOrNull { it.teamID == number }!!.uuid.toPlayer()!!
                            items.firstOrNull { it.id == UUID.fromString(id) }?.money = value

                            if (players.isOnline) {
                                if (Money(players).has(value)) {
                                    //询问
                                    players.info("队长给你以 &f$value §7金的价格分配了 §f${item.amount} §7个 §f${Items.getName(item)}")
                                    TellrawJson.create().append("§8[§c Other §8] §7点击接受 §a[接受]")
                                        .hoverItem(item)
                                        .clickCommand("/team item ${itemData?.id.toString()}")
                                        .send(players)
                                    Team.itemAccept[itemData!!.id] = players

                                } else {
                                    player.error("目标的余额不足!")
                                    openLib(player)
                                }
                                Team.save()
                                openLib(player)
                            } else {
                                player.error("该玩家不在线!")
                            }

                        }
                    }
                }
                if (isRight) {
                    val map = mutableListOf<RollData>()
                    getAllMember().forEach {
                        TellrawJson.create().append("§8[§c Other §8] §7队长对 §f${Items.getName(item)} §7发起了Roll点分配!")
                            .hoverItem(item)
                            .send(it.uuid.toPlayer())
                    }
                    val list = mutableListOf<TeamMemberData>()
                    getAllMember().forEach {
                        if (it.uuid.toPlayer()!!.isOnline) {
                            list.add(it)
                        }
                    }
                    list.forEach { target ->
                        val roll = (0..999).random()
                        sendMessage("队员 §f${target.uuid.toPlayer()!!.name} §7对§f ${Items.getName(item)} §7Roll出了 §e$roll§7 点")
                        map.add(RollData(target.uuid, roll))
                    }
                    map.sortByDescending { it.roll }
                    sendMessage("当前最高点数为§e ${map[0].roll} §7掷出者: §f${map[0].uuid.toPlayer()!!.name}")
                    giveItem(item, map[0].uuid)
                    items.remove(itemData)
                    Team.save()
                    openLib(player)
                }
            }
            .open(player)
    }

    fun openJoinGUI(player: Player) {
        val menu = MenuBuilder.builder(OtherTeam.plugin)
        menu.rows(6)
        menu.title("队伍申请列表")
        menu.build { inv ->
            inv.setItem(0,
                ItemBuilder(Material.CAMPFIRE)
                    .name("  ")
                    .lore("§7<- 点击返回队伍面板", "")
                    .colored()
                    .build()
            )
            joinList.forEach { uuid ->
                val target = uuid.toPlayer() ?: return@forEach
                val item = ItemBuilder(Material.BLUE_BANNER)
                item.name("&7申请者: §f${uuid.toPlayer()!!.name}")
                item.lore(OtherTeam.settings.getStringListColored("Team.joinPlayer.info").toPapi(target))
                item.colored()
                inv.addItem(item.build())
            }
        }
        menu.event { event ->
            event.isCancelled = true
            if (event.rawSlot == 0) {
                player.closeInventory()
                openGUI(player)
                return@event
            }
            if (!Items.hasLore(event.currentItem)) {
                return@event
            }
            val name = Items.getName(event.currentItem).replace("§7申请者: §f", "")
            val target = PlayerUtil.getOfflinePlayer(name)
            if (target == null) {
                player.error("对方不在线!")
                return@event
            }
            if (Team.getTeam(target.uniqueId) != null) {
                player.error("对方已经拥有一个队伍了!")
                return@event
            }
            if (member.size >= 4) {
                player.error("队伍已满员!")
                return@event
            }
            addMember(target.uniqueId)
            sendMessage("玩家 &f${target.name} §7已经成功加入了队伍!")
            joinList.remove(target.uniqueId)
            Team.save()
            openJoinGUI(player)
        }

        menu.open(player)
    }

    class RollData(
        val uuid: UUID,
        val roll: Int,
    )

}