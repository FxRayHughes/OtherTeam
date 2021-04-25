package io.izzel.taboolib.other.team

import io.izzel.taboolib.loader.Plugin
import io.izzel.taboolib.module.config.TConfig
import io.izzel.taboolib.module.inject.TInject

object OtherTeam : Plugin() {
    @TInject(value = ["settings.yml"], locale = "LOCALE-PRIORITY")
    lateinit var settings: TConfig
        private set
}