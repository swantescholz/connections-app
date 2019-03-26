package de.sscholz

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.input.GestureDetector
import com.badlogic.gdx.math.Vector2
import de.sscholz.Global.settings
import de.sscholz.util.camera
import de.sscholz.util.minViewportWidth
import ktx.app.KtxInputAdapter
import ktx.math.minus
import ktx.math.plus
import ktx.math.times
import ktx.math.vec2
import java.lang.Math.max
import java.lang.Math.min

class CameraInputHandler(private val level: Level) : GestureDetector.GestureAdapter(), KtxInputAdapter {
    override fun scrolled(amount: Int): Boolean {
        val x = Gdx.input.x.toFloat()
        val y = Gdx.input.y.toFloat()
        val target = camera.screenToWorldCoordinates(x, y)
        safeZoomOut(Math.pow(1.1, amount.toDouble()).toFloat(), target)
        return true
    }

    private fun safeZoomOut(zoomFactor: Float, target: Vector2) {
        val originalCameraPosition = camera.position
        camera.zoomOut(zoomFactor, target)
        if (camera.viewportWidth > settings.maxRelativeViewportWidthVsLevelWidth * level.width) {
            camera.setNewViewportWorldWidth(originalCameraPosition,
                    settings.maxRelativeViewportWidthVsLevelWidth * level.width)
        }
        if (camera.viewportWidth < minViewportWidth) {
            camera.setNewViewportWorldWidth(originalCameraPosition, minViewportWidth)
        }
    }

    var lastPinchDistance: Float? = null

    override fun pinch(initialPointer1: Vector2?, initialPointer2: Vector2?, pointer1: Vector2?, pointer2: Vector2?): Boolean {
        if (pointer1 == null || pointer2 == null)
            return false
        val currentPinchDistance = (pointer1 - pointer2).len()
        if (lastPinchDistance != null) {
            val screenPinchCenter = (pointer1 + pointer2) * 0.5f
            val worldPinchCenter = camera.screenToWorldCoordinates(screenPinchCenter.x, screenPinchCenter.y)
            safeZoomOut(lastPinchDistance!! / currentPinchDistance, worldPinchCenter)
        }
        lastPinchDistance = currentPinchDistance
        return true
    }

    override fun pinchStop() {
        lastPinchDistance = null
    }


    var isPanning: Boolean = false
        private set

    private fun clampCameraCenterToLevelBoundaries(target: Vector2): Vector2 {
        val x = max(0f, min(level.width, target.x))
        val y = max(0f, min(level.height, target.y))
        return vec2(x, y)
    }

    private var panStartMouseWorldCoordinates: Vector2? = null
    override fun pan(x: Float, y: Float, deltaX: Float, deltaY: Float): Boolean {
        if (freezePanning || panStartMouseWorldCoordinates == null) {
            return false
        }
        isPanning = true
        val worldXy = camera.currentMouseWorldCoordinates()
        val worldMouseDeltaFromStart = worldXy - panStartMouseWorldCoordinates!!
        val target = camera.position - worldMouseDeltaFromStart
        camera.moveTo(clampCameraCenterToLevelBoundaries(target))
        return true
    }

    override fun touchDown(x: Float, y: Float, pointer: Int, button: Int): Boolean {
        if (button == Input.Buttons.LEFT && !freezePanning) {
            panStartMouseWorldCoordinates = camera.currentMouseWorldCoordinates()
        }
        return false
    }

    override fun touchUp(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
        panStartMouseWorldCoordinates = null
        freezePanning = false
        isPanning = false
        return false
    }

    private var freezePanning: Boolean = false

    // call during selection of elements
    fun freezePanningUntilTouchUp() {
        freezePanning = true
        panStartMouseWorldCoordinates = null
        isPanning = false
    }
}