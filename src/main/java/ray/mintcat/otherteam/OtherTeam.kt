package ray.mintcat.otherteam

import io.izzel.taboolib.loader.Plugin
import io.izzel.taboolib.module.config.TConfig
import io.izzel.taboolib.module.inject.TInject

object OtherTeam : Plugin() {

    @TInject("settings.yml")
    lateinit var settings: TConfig
        private set
}