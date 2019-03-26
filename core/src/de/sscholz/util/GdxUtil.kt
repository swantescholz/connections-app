package de.sscholz.util

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.math.Vector2
import de.sscholz.Global.settings
import de.sscholz.Person
import de.sscholz.Physics.world
import ktx.box2d.query
import ktx.math.div
import ktx.math.minus
import ktx.math.plus
import ktx.math.vec2
import java.util.*
import kotlin.collections.ArrayList


object GdxUtil {
    data class AabbBox(val center: Vector2, val halfSize: Vector2)

    fun withGlBlendingEnabled(renderingLambda: () -> Unit) {
        Gdx.gl.glEnable(GL20.GL_BLEND)
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA)
        renderingLambda()
        Gdx.gl.glDisable(GL20.GL_BLEND)
    }

    fun computeAabbBoxForVectors(vectors: Sequence<Vector2>): AabbBox {
        val min = Vector2(vectors.first())
        val max = Vector2(min)
        vectors.forEach {
            min.x = Math.min(min.x, it.x)
            min.y = Math.min(min.y, it.y)
            max.x = Math.max(max.x, it.x)
            max.y = Math.max(max.y, it.y)
        }
        val center = (min + max) / 2f
        return AabbBox(center, max - center)
    }

    fun randomNormalizedVec2(): Vector2 {
        for (i in 1..100) {
            val x = (-1f..1f).random()
            val y = (-1f..1f).random()
            val len2 = x * x + y * y
            if (len2 < 1) {
                val len = Math.sqrt(len2.toDouble()).toFloat()
                return vec2(x / len, y / len)
            }
        }
        throw RuntimeException("Should not happen")
    }

    fun vector2sToFloatArray(vertices: List<Vector2>, closeLoop: Boolean = true): FloatArray {
        val floats = FloatArray(vertices.size * 2 + if (closeLoop) 2 else 0)
        for (i in 0 until vertices.size) {
            floats[i * 2 + 0] = vertices[i].x
            floats[i * 2 + 1] = vertices[i].y
        }
        if (closeLoop) {
            floats[vertices.size * 2 + 0] = vertices[0].x
            floats[vertices.size * 2 + 1] = vertices[0].y
        }
        return floats
    }

    // sets our desired preferences
    fun setupOpenGl() {
        Gdx.gl.glLineWidth(settings.defaultLineWidth)
    }

    fun getCurrentWorldCoordinatesOfMouse(): Vector2 {
        return camera.screenToWorldCoordinates(Gdx.input.x.toFloat(),
                Gdx.input.y.toFloat())
    }

    fun aabbQueryPerson(selectionAabbCenter: Vector2, selectionAabbHalfSize: Float = 0.01f): Person? {
        var result: Person? = null
        world.query(selectionAabbCenter.x - selectionAabbHalfSize, selectionAabbCenter.y - selectionAabbHalfSize,
                selectionAabbCenter.x + selectionAabbHalfSize, selectionAabbCenter.y + selectionAabbHalfSize) {
            (it.body.userData as? Person)?.let { person ->
                if (result == null || (person.worldCenter - selectionAabbCenter).len() <
                        (result!!.worldCenter - selectionAabbCenter).len()) {
                    result = person
                }
            }
            return@query true // false -> terminate query, true -> continue searching
        }
        return result
    }

    fun createRegularPolygonCorners(numberOfCorners: Int, radius: Float): ArrayList<Vector2> {
        val corners = ArrayList<Vector2>()
        val angleStep = 2 * Math.PI / numberOfCorners
        val offsetAngle = -Math.PI / 2 - angleStep / 2
        for (i in 0 until numberOfCorners) {
            val x = Math.cos(offsetAngle + i * angleStep) * radius
            val y = Math.sin(offsetAngle + i * angleStep) * radius
            corners.add(vec2(x.toFloat(), y.toFloat()))
        }
        return corners
    }


    object framerateComputer {
        private const val N = 10
        private val queue = LinkedList<Float>()
        private var deltaTimeSum = 0.0f

        fun addDeltaTimeOfCurrentFrame(deltaTime: Float) {
            deltaTimeSum += deltaTime
            queue.addLast(deltaTime)
            if (queue.size > N) {
                deltaTimeSum -= queue.pollFirst()
            }
        }

        fun computeFps(): Float {
            if (queue.size == 0) {
                return 0.0f
            }
            return N / deltaTimeSum
        }
    }
}