package ray.mintcat.otherteam.team.ui

import io.izzel.taboolib.kotlin.sendLocale
import io.izzel.taboolib.module.inject.TFunction
import io.izzel.taboolib.module.locale.TLocale
import io.izzel.taboolib.module.tellraw.TellrawJson
import io.izzel.taboolib.util.Features
import io.izzel.taboolib.util.item.ItemBuilder
import io.izzel.taboolib.util.item.Items
import io.izzel.taboolib.util.item.inventory.MenuBuilder
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemFlag
import ray.mintcat.otherteam.OtherTeam
import ray.mintcat.otherteam.team.Team
import ray.mintcat.otherteam.team.TeamData
import ray.mintcat.otherteam.ui.UIInterface
import ray.mintcat.otherteam.utils.Helper
import ray.mintcat.otherteam.utils.Money
import ray.mintcat.wizardfix.Data
import ray.mintcat.wizardfix.PlayerUtil
import java.text.DecimalFormat

object TeamUI : Helper, UIInterface {

    @TFunction.Init
    private fun init() {
        register()
    }

    override val id: String
        get() = "main"

    fun openGUINew(player: Player, data: TeamData) {
        val menu = UIInterface.getMenuBuilder(player, this)
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
                inv.setItem(
                    0,
                    ItemBuilder(Material.NAME_TAG)
                        .name("§f团队设置")
                        .lore(loreTell)
                        .colored()
                        .build()
                )
                inv.setItem(
                    10,
                    ItemBuilder(data.getFlag(0))
                        .name("&7队长:§f ${Bukkit.getOfflinePlayer(admin).name}")
                        .lore("§7当前状态: ${data.isOnlineInfo(Bukkit.getOfflinePlayer(admin))}", "", "§7成员ID: §f0")
                        .colored()
                        .build()
                )
                if (Bukkit.getPluginManager().isPluginEnabled("Other")) {
                    inv.setItem(
                        18,
                        ItemBuilder(Material.OAK_SIGN)
                            .name("§f队伍聊天频道")
                            .lore(
                                listOf(
                                    "§7当前聊天频道: §f$data.chatRoom",
                                    "",
                                    "&7队长可以进行更改",
                                    "§8左键:§7加入聊天频道 §f| §8右键:§7切换聊天频道",
                                    "§8下蹲+右键:§7强制所有成员加入队伍频道"
                                ).process()
                            )
                            .colored()
                            .build()
                    )
                }
                inv.setItem(
                    19,
                    ItemBuilder(Material.TRIPWIRE_HOOK)
                        .name("§f入队申请")
                        .lore(
                            listOf(
                                "§7当前等候区人数: §f${data.joinList.size}",
                                "",
                                "&7队长可以进行通过",
                                "§8点击:§7打开审核列表"
                            ).process()
                        )
                        .colored()
                        .build()
                )
                inv.setItem(
                    23,
                    ItemBuilder(Material.GOLD_INGOT)
                        .name("§f队伍金库")
                        .lore(listOf("§7当前金库: §f${data.money}金", "", "§8队长可以进行分配", "§8左键:§7添加货币 §f| §8右键:§7均分给成员"))
                        .colored()
                        .build()
                )
                inv.setItem(
                    24,
                    ItemBuilder(Material.CHEST)
                        .name("§f队伍战利品")
                        .lore(
                            listOf(
                                "§7战利品分配模式: $data.itemType",
                                "",
                                "&7队长可以进行分配",
                                "§8左键:§7打开仓库 §f| §8右键:§7切换模式"
                            ).process()
                        )
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
                inv.setItem(
                    25,
                    itemPVP.build()
                )
                inv.setItem(
                    26,
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
                        inv.setItem(
                            12 + a,
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
                    inv.setItem(
                        12 + data.member.size,
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
                            player.sendLocale("command-team-player-execute-error")
                            return@event
                        }
                        Features.inputSign(player, arrayOf("", "§6↑输入 确认 确认操作")) { les ->
                            if (les.isEmpty() || les[0] != "确认") {
                                player.sendLocale("command-team-player-operation-canceled")
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
                            player.sendLocale("command-team-player-execute-error")
                            return@event
                        }
                        Features.inputSign(player, arrayOf("", "§6↑输入 确认 确认操作")) { les ->
                            if (les.isEmpty() || les[0] != "确认") {
                                player.sendLocale("command-team-player-operation-canceled")
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
                            player.sendLocale("command-team-player-not-online")
                            return@inputSign
                        }
                        player.sendLocale("command-team-invitation-sent-successfully")
                        Data(players).edit("TeamAccept", "=", admin.toString())
                        player.sendLocale("command-team-player-got-invitation")
                        TellrawJson.create().append(TLocale.asString("command-team-player-clicked-to-accept"))
                            .hoverText("接受请求 [来自${admin.toPlayer()?.name}]")
                            .clickCommand("/team accept")
                            .send(players)
                    }
                    return@event
                }
                when (event.rawSlot) {
                    0 -> {
                        if (!data.isAdmin(player)) {
                            player.sendLocale("command-team-player-execute-error")
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
                            player.sendLocale("command-team-player-joined-team-chat-channel")
                            return@event
                        }
                        if (isShift && isRight) {
                            if (!data.isAdmin(player)) {
                                player.sendLocale("command-team-player-execute-error")
                                return@event
                            }
                            data.sendMessage("command-team-leader-forced-teamers-join-chat-channel", data.chatRoom)
                            data.getAllMember().forEach { players ->
                                Data(players.uuid.toPlayer()!!).edit("chat-room", "=", data.chatRoom)
                            }
                            return@event
                        }
                        if (isRight) {
                            if (!data.isAdmin(player)) {
                                player.sendLocale("command-team-player-execute-error")
                                return@event
                            }
                            Features.inputSign(player, arrayOf("", "§6↑设置频道")) { les ->
                                val info = les[0].screen()
                                data.getAllMember().forEach { players ->
                                    TellrawJson.create()
                                        .append(TLocale.asString("command-team-leader-setted-the-new-chat-channel"))
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
                            player.sendLocale("command-team-player-execute-error")
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
                                    player.sendLocale("command-team-player-dont-have-enough-money")
                                    openGUI(player, data)
                                    return@inputSign
                                }
                                data.sendMessage("command-team-player-added-money-to-the-team", player.name, moneys)
                                data.money += moneys
                                Team.save()
                                openGUI(player, data)
                            }
                        }
                        if (event.castClick().isRightClick) {
                            if (!data.isAdmin(player)) {
                                player.sendLocale("command-team-player-execute-error")
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
                                player.sendLocale("command-team-player-execute-error")
                                return@event
                            }
                            player.closeInventory()
                            data.switchItemType()
                            var string = TLocale.asString(
                                "command-team-leader-switched-the-trophy-allocation-strategy",
                                data.itemType
                            )
                            string = string.process()
                            data.getAllMember().forEach { uuid ->
                                val playerMember = Bukkit.getPlayer(uuid.uuid)
                                if (playerMember != null && playerMember.isOnline) {
                                    playerMember.sendMessage(string)
                                }
                            }
                            openGUI(player, data)
                            return@event
                        }
                        player.closeInventory()
                        data.openLib(player)

                    }
                    25 -> {
                        if (!data.isAdmin(player)) {
                            player.sendLocale("command-team-player-execute-error")
                            return@event
                        }
                        player.closeInventory()
                        data.switchPVP()
                        var string = TLocale.asString(
                            "command-team-leader-switched-the-pvp-mode",
                            data.pvp
                        )
                        string = string.process()
                        data.getAllMember().forEach { uuid ->
                            val playerMember = Bukkit.getPlayer(uuid.uuid)
                            if (playerMember != null && playerMember.isOnline) {
                                playerMember.sendMessage(string)
                            }
                        }
                        openGUI(player, data)
                    }
                    26 -> {
                        data.leaveTeam(player.uniqueId)
                        data.sendMessage("command-team-player-left-message-to-all", player.name)
                        player.closeInventory()
                        Data(player).edit("chat-room", "=", "0")
                        player.sendLocale("command-team-player-left-message-to-single")
                        player.sendLocale("command-team-player-auto-switching-channel-to-default")
                    }
                }
            }.close {
                Team.save()
            }.open(player)
    }
}