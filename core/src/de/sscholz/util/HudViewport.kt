package de.sscholz.util

import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.utils.viewport.FitViewport

object hudViewport {

    private val orthoCam = OrthographicCamera(screenWidth, screenHeight)
    private val viewport = FitViewport(orthoCam.viewportWidth, orthoCam.viewportHeight, orthoCam)

    init {
        log("HUD init()")
        viewport.setScreenBounds(0, 0, screenWidth.toInt(), screenHeight.toInt())
    }

    fun apply() {
        viewport.apply()
    }
}