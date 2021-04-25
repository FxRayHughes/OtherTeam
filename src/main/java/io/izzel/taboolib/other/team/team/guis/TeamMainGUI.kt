package io.izzel.taboolib.other.team.team.guis

import io.izzel.taboolib.module.inject.TFunction
import io.izzel.taboolib.module.tellraw.TellrawJson
import io.izzel.taboolib.other.team.team.Team
import io.izzel.taboolib.other.team.team.TeamData
import io.izzel.taboolib.other.team.utils.Helper
import io.izzel.taboolib.other.team.utils.Money
import io.izzel.taboolib.other.team.OtherTeam
import io.izzel.taboolib.other.team.gui.GUIInterface
import io.izzel.taboolib.util.Features
import io.izzel.taboolib.util.item.ItemBuilder
import io.izzel.taboolib.util.item.Items
import io.izzel.taboolib.util.item.inventory.MenuBuilder
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemFlag
import ray.mintcat.wizardfix.Data
import ray.mintcat.wizardfix.PlayerUtil
import java.text.DecimalFormat

object TeamMainGUI : Helper, GUIInterface {

    @TFunction.Init
    private fun init() {
        register()
    }

    override val id: String
        get() = "main"

    fun openGUINew(player: Player, data: TeamData) {
        val menu = GUIInterface.getMenuBuilder(player, this)
        menu.keys
        menu.click { event ->
            when (event.slot) {

            }
        }
    }

    fun openGUI(player: Player, data: TeamData) {


        val menu = MenuBuilder.builder(OtherTeam.plugin)
        val name = data.name
        val tell = data.tell
        val admin = data.admin
        menu.rows(3)
            .title("§8[§f $name §8] &0管理面板".process())
            .build { inv ->
                val loreTell = mutableListOf<String>()
                loreTell.add("&7队伍名称:§f $name")
                loreTell.add("&7队伍宣言:")
                loreTell.addAll(tell.map { " §7- §f$it" })
                loreTell.add(" ")
                loreTell.add("§8左键:§7编辑名称 §f| §8右键:§7编辑宣言")
                inv.setItem(0,
                    ItemBuilder(Material.NAME_TAG)
                        .name("§f团队设置")
                        .lore(loreTell)
                        .colored()
                        .build()
                )
                inv.setItem(10,
                    ItemBuilder(data.getFlag(0))
                        .name("&7队长:§f ${Bukkit.getOfflinePlayer(admin).name}")
                        .lore("§7当前状态: ${data.isOnlineInfo(Bukkit.getOfflinePlayer(admin))}", "", "§7成员ID: §f0")
                        .colored()
                        .build()
                )
                if (Bukkit.getPluginManager().isPluginEnabled("Other")) {
                    inv.setItem(18,
                        ItemBuilder(Material.OAK_SIGN)
                            .name("§f队伍聊天频道")
                            .lore(listOf("§7当前聊天频道: §f$data.chatRoom",
                                "",
                                "&7队长可以进行更改",
                                "§8左键:§7加入聊天频道 §f| §8右键:§7切换聊天频道",
                                "§8下蹲+右键:§7强制所有成员加入队伍频道").process())
                            .colored()
                            .build()
                    )
                }
                inv.setItem(19,
                    ItemBuilder(Material.TRIPWIRE_HOOK)
                        .name("§f入队申请")
                        .lore(listOf("§7当前等候区人数: §f${data.joinList.size}",
                            "",
                            "&7队长可以进行通过",
                            "§8点击:§7打开审核列表").process())
                        .colored()
                        .build()
                )
                inv.setItem(23,
                    ItemBuilder(Material.GOLD_INGOT)
                        .name("§f队伍金库")
                        .lore(listOf("§7当前金库: §f${data.money}金", "", "§8队长可以进行分配", "§8左键:§7添加货币 §f| §8右键:§7均分给成员"))
                        .colored()
                        .build()
                )
                inv.setItem(24,
                    ItemBuilder(Material.CHEST)
                        .name("§f队伍战利品")
                        .lore(listOf("§7战利品分配模式: $data.itemType",
                            "",
                            "&7队长可以进行分配",
                            "§8左键:§7打开仓库 §f| §8右键:§7切换模式").process())
                        .colored()
                        .build()
                )
                val itemPVP = ItemBuilder(Material.IRON_SWORD)
                    .name("§fPVP状态")
                    .lore(listOf("§7当前状况: $data.pvp", "", "§8队长可以进行切换", "§8点击切换").process())
                    .colored()
                    .flags(ItemFlag.HIDE_ATTRIBUTES)
                if (data.pvp) {
                    itemPVP.enchant(Enchantment.LUCK, 10)
                }
                inv.setItem(25,
                    itemPVP.build()
                )
                inv.setItem(26,
                    ItemBuilder(Material.IRON_BOOTS)
                        .name("§f离开队伍")
                        .lore(listOf("&c点击离开队伍"))
                        .flags(ItemFlag.HIDE_ATTRIBUTES)
                        .colored()
                        .build()
                )

                var a = 0
                for (i in data.members) {
                    val players = Bukkit.getOfflinePlayer(i.uuid)
                    if (PlayerUtil.getOfflinePlayerList().contains(player)) {
                        inv.setItem(12 + a,
                            ItemBuilder(data.getFlag(i.teamID))
                                .name("&7成员:§f ${players.name}")
                                .lore(
                                    "§7当前状态: ${data.isOnlineInfo(players)}",
                                    "",
                                    "§7成员ID: §f${i.teamID}",
                                    "§8Shift+左键:§7提为队长§f | §8Shift+右键:§7请离队伍"
                                )
                                .colored()
                                .build()
                        )
                    }
                    a += 1
                }
                if (data.member.size < 4) {
                    inv.setItem(12 + data.member.size,
                        ItemBuilder(Material.MAP)
                            .name("  ")
                            .lore("&7+ 点击邀请玩家", "")
                            .colored()
                            .build()
                    )
                }
            }
            .event { event ->
                event.isCancelled = true
                val isShift = event.castClick().isShiftClick
                val isRight = event.castClick().isRightClick
                val isLeft = event.castClick().isLeftClick
                if (Items.hasLore(event.currentItem, "§8Shift+左键:§7提为队长§f")) {
                    val teamID = event.currentItem!!.itemMeta!!.lore!!.firstOrNull { it.indexOf("成员ID:") != -1 }!!
                        .split(": §f")[1].toInt()
                    if (isLeft && isShift) {
                        if (!data.isAdmin(player)) {
                            player.error("你不是队长,无法进行操作!")
                            return@event
                        }
                        Features.inputSign(player, arrayOf("", "§6↑输入 确认 确认操作")) { les ->
                            if (les.isEmpty() || les[0] != "确认") {
                                player.info("操作取消!")
                                openGUI(player, data)
                                return@inputSign
                            }
                            val target = data.members.firstOrNull { it.teamID == teamID }!!.uuid
                            data.sendMessage("队长被转让了,现在的队长是 §f${target.toPlayer()!!.name}")
                            Team.create(TeamData(target, name, tell, data.pvp, data.member, data.money, data.items))
                            Team.getTeam(target)!!.addMember(admin)
                            Team.remove(admin)
                        }
                    }
                    if (isRight && isShift) {
                        if (!data.isAdmin(player)) {
                            player.error("你不是队长,无法进行操作!")
                            return@event
                        }
                        Features.inputSign(player, arrayOf("", "§6↑输入 确认 确认操作")) { les ->
                            if (les.isEmpty() || les[0] != "确认") {
                                player.info("操作取消!")
                                openGUI(player, data)
                                return@inputSign
                            }
                            data.sendMessage("玩家 §f${data.members.firstOrNull { it.teamID == teamID }!!.uuid.toPlayer()!!.name} §7被队长请离了队伍!")
                            data.member.remove(data.members.firstOrNull { it.teamID == teamID }!!.uuid)
                            Team.save()
                        }
                    }

                    return@event
                }
                if (Items.hasLore(event.currentItem, "点击邀请玩家")) {
                    //邀请玩家
                    Features.inputSign(player, arrayOf("", "§6↑输入名称")) { les ->
                        val players = les[0].toPlayer()
                        if (players == null) {
                            player.error("玩家 [§f${les[0]}&7] 不存在或不在线请查询后再添加!")
                            return@inputSign
                        }
                        player.info("邀请发送成功! 等待对方回应...")
                        Data(players).edit("TeamAccept", "=", admin.toString())
                        players.info("您收到了一个来自${player.name}的队伍邀请:")
                        TellrawJson.create().append("§8[§c Other §8] §7点击接受 §a[接受]")
                            .hoverText("接受请求 [来自${admin.toPlayer()?.name}]")
                            .clickCommand("/team accept")
                            .send(players)
                    }
                    return@event
                }
                when (event.rawSlot) {
                    0 -> {
                        if (!data.isAdmin(player)) {
                            player.error("你不是队长,无法进行操作!")
                            return@event
                        }
                        if (event.castClick().isLeftClick) {
                            Features.inputSign(player, arrayOf("", "§6↑输入名称")) { les ->
                                data.name = les[0].screen()
                                Team.save()
                                openGUI(player, data)
                            }
                        }
                        if (event.castClick().isRightClick) {
                            val old = tell
                            old.add(0, "输入宣言:")
                            Features.inputBook(player, "编辑队伍宣言", false, old) { book ->
                                if (Items.hasName(player.inventory.itemInMainHand, "编辑队伍宣言")) {
                                    player.inventory.itemInMainHand.amount = 0
                                } else {
                                    player.inventory.itemInOffHand.amount = 0
                                }
                                val info = book.map { it.replace("输入宣言:", "").replace(" ", "") }
                                val over = mutableListOf<String>()
                                info.forEach { s: String ->
                                    if (s != "") {
                                        over.add(s)
                                    }
                                }
                                data.tell = over
                                Team.save()
                                openGUI(player, data)
                            }
                        }
                    }
                    18 -> {
                        if (!Bukkit.getPluginManager().isPluginEnabled("Other")) {
                            return@event
                        }
                        if (isLeft) {
                            Data(player).edit("chat-room", "=", data.chatRoom)
                            player.info("您已加入队伍聊天频道")
                            return@event
                        }
                        if (isShift && isRight) {
                            if (!data.isAdmin(player)) {
                                player.error("你不是队长,无法进行操作!")
                                return@event
                            }
                            data.sendMessage("队长强制队员加入了队伍聊天频道 &f$data.chatRoom &7请听队长指挥!")
                            data.getAllMember().forEach { players ->
                                Data(players.uuid.toPlayer()!!).edit("chat-room", "=", data.chatRoom)
                            }
                            return@event
                        }
                        if (isRight) {
                            if (!data.isAdmin(player)) {
                                player.error("你不是队长,无法进行操作!")
                                return@event
                            }
                            Features.inputSign(player, arrayOf("", "§6↑设置频道")) { les ->
                                val info = les[0].screen()
                                data.getAllMember().forEach { players ->
                                    TellrawJson.create()
                                        .append("§8[§c Other §8] §7队长设置了新的队伍频道 §f$info §7请队员们重新加入!  §a[点击快速加入]")
                                        .hoverText("§7新的频道: §f$info")
                                        .clickCommand("/other tjoin $info")
                                        .send(players.uuid.toPlayer()!!)
                                }
                                data.chatRoom = info
                                Team.save()
                                openGUI(player, data)
                            }
                        }
                    }
                    19 -> {
                        if (!data.isAdmin(player)) {
                            player.error("你不是队长,无法进行操作!")
                            return@event
                        }
                        data.openJoinGUI(player)
                    }
                    23 -> {
                        if (event.castClick().isLeftClick) {
                            Features.inputSign(player, arrayOf("", "§6↑添加数量")) { les ->
                                val moneys =
                                    les[0].replace("-", "").replace("[^0-9.]".toRegex(), "").toDoubleOrNull() ?: 0.0
                                if (!Money(player).take(moneys)) {
                                    player.error("你没有那么多金,你目前有 §f${Money(player).now}金")
                                    openGUI(player, data)
                                    return@inputSign
                                }
                                data.sendMessage("队员 §f${player.name} &7为团队金库添加了 §f${moneys}金!")
                                data.money += moneys
                                Team.save()
                                openGUI(player, data)
                            }
                        }
                        if (event.castClick().isRightClick) {
                            if (!data.isAdmin(player)) {
                                player.error("你不是队长,无法进行操作!")
                                return@event
                            }
                            val df = DecimalFormat("#.00")
                            val number = df.format(data.money / data.getAllMember().size)
                            data.getAllMember().forEach {
                                data.giveMoney(it.uuid, number.toDouble())
                            }
                            data.money = 0.0
                            openGUI(player, data)
                        }
                    }
                    24 -> {
                        if (event.castClick().isRightClick) {
                            if (!data.isAdmin(player)) {
                                player.error("你不是队长,无法进行操作!")
                                return@event
                            }
                            player.closeInventory()
                            data.switchItemType()
                            data.sendMessage("队长切换了战利品分配策略,当前为 $data.itemType 状态!".process())
                            openGUI(player, data)
                            return@event
                        }
                        player.closeInventory()
                        data.openLib(player)

                    }
                    25 -> {
                        if (!data.isAdmin(player)) {
                            player.error("你不是队长,无法进行操作!")
                            return@event
                        }
                        player.closeInventory()
                        data.switchPVP()
                        data.sendMessage("队长切换了PVP状态,当前为 $data.pvp 状态!".process())
                        openGUI(player, data)
                    }
                    26 -> {
                        data.leaveTeam(player.uniqueId)
                        data.sendMessage("玩家 &f${player.name}&7 离开了队伍")
                        player.closeInventory()
                        Data(player).edit("chat-room", "=", "0")
                        player.info("你离开了队伍 §f$name")
                        player.info("你已经成功加入了频道 &f默认频道")
                    }
                }
            }
            .close {
                Team.save()
            }
            .open(player)
    }

}