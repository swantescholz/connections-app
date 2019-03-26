package de.sscholz

import com.badlogic.gdx.backends.lwjgl.LwjglApplication
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration

object DesktopLauncher {

    @JvmStatic
    fun main(args: Array<String>) {
        val cfg = LwjglApplicationConfiguration()
        cfg.title = "Connections"
        cfg.height = 1000
        cfg.width = 800
        LwjglApplication(App(), cfg)
    }
}
