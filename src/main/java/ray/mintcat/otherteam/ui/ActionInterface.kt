package ray.mintcat.otherteam.ui

import org.bukkit.entity.Player

interface ActionInterface {

    fun register() {
        UI.actions.add(this)
    }

    /**
     * 所属菜单的ID
     */
    val ui: String

    /**
     * 按钮名称
     */
    val key: String

    /**
     * 执行动作
     */
    fun <T> run(player: Player, vararg other: T)
}