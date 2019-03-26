package de.sscholz.util

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.math.Matrix4
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.viewport.FitViewport
import com.badlogic.gdx.utils.viewport.Viewport
import de.sscholz.Global
import de.sscholz.extensions.toVector2
import de.sscholz.extensions.toVector3
import ktx.math.plus
import ktx.math.vec2

object camera {

    val physicsScreenWidth by lazy { screenWidth }
    val physicsScreenHeight by lazy { screenHeight - hudTopTotalHeight }
    val physicsScreenDyBottom by lazy { 0f }
    val physicsScreenDx by lazy { 0f }
    private val orthoCam = OrthographicCamera(physicsScreenWidth, physicsScreenHeight)

    val heightToWidthRatio: Float
        get() = orthoCam.viewportHeight / orthoCam.viewportWidth
    val viewportWidth: Float get() = orthoCam.viewportWidth
    val viewportHeight: Float get() = orthoCam.viewportHeight
    val combinedMatrix: Matrix4 get() = orthoCam.combined
    val position: Vector2 get() = vec2(orthoCam.position.x, orthoCam.position.y)
    private lateinit var viewport: Viewport

    fun screenToWorldCoordinates(screenXInPx: Float, screenYInPx: Float): Vector2 {
        val xNormalized = (screenXInPx - physicsScreenDx) / physicsScreenWidth - 0.5f
        val yNormalized = (Gdx.graphics.height - screenYInPx - 1 -
                physicsScreenDyBottom) / physicsScreenHeight - 0.5f
        return vec2(xNormalized * viewportWidth, yNormalized * viewportHeight) + position
    }

    fun currentMouseWorldCoordinates(): Vector2 {
        return screenToWorldCoordinates(Gdx.input.x.toFloat(), Gdx.input.y.toFloat())
    }

    fun moveTo(newWorldXy: Vector2) {
        orthoCam.position.set(newWorldXy.x, newWorldXy.y, 0f)
        update()
    }

    // factor > 1.0 -> zoom out, factor < 1.0 -> zoom in
    fun zoomOut(factor: Float, targetPositionInWorldCoordinates: Vector2) {
        orthoCam.viewportWidth *= factor
        orthoCam.viewportHeight *= factor
        orthoCam.position.set(Vector2(targetPositionInWorldCoordinates).apply {
            lerp(orthoCam.position.toVector2(), factor)
        }.toVector3())
        update()
    }

    // updates camera matrix and assign viewport width/height info to viewport instance
    private fun update() {
        orthoCam.update()
        viewport.worldWidth = orthoCam.viewportWidth
        viewport.worldHeight = orthoCam.viewportHeight
    }

    fun apply() {
        update()
        viewport.apply()
        Global.shapeRenderer.projectionMatrix = orthoCam.combined
    }

    fun setNewViewportWorldWidth(newPosition: Vector2, newViewportWidthInUnits: Float) {
        log("camera. set viewport width")
        orthoCam.viewportHeight = newViewportWidthInUnits * 1f * heightToWidthRatio
        orthoCam.viewportWidth = newViewportWidthInUnits
        orthoCam.position.set(newPosition.x, newPosition.y, 0f)
        update()
    }

    fun initCamera(viewportWidthInUnits: Float) {
        orthoCam.viewportHeight = viewportWidthInUnits * 1f * physicsScreenHeight / physicsScreenWidth
        orthoCam.viewportWidth = viewportWidthInUnits
        viewport = FitViewport(orthoCam.viewportWidth, orthoCam.viewportHeight, orthoCam)
        viewport.setScreenBounds(physicsScreenDx.toInt(), physicsScreenDyBottom.toInt(),
                physicsScreenWidth.toInt(), physicsScreenHeight.toInt())
        update()
    }

}