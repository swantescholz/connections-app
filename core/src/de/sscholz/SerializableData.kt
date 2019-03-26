package de.sscholz

import com.badlogic.gdx.math.Vector2
import de.sscholz.Global.settings
import de.sscholz.connections.Type
import de.sscholz.util.GdxUtil
import de.sscholz.util.myAssert
import kotlinx.serialization.Optional
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import ktx.math.plus
import ktx.math.times
import ktx.math.vec2

@Serializable
data class PersonData(
        val index: Int,
        val x: Float,
        val y: Float,
        @Optional val corners: Int = 0, // 0 -> circle, 3 -> triangle, etc
        @Optional val angle: Float = 0f,
        @Optional val ego: Float = Person.defaultEgo,
        @Optional val reliability: Float = Person.defaultReliability,
        @Optional val empathy: Float = Person.defaultEmpathy
)


@Serializable
data class ShapeTemplateData( // shape is first normalized into unit [-1,1] square, then scale, then rotation is applied
        val name: String,
        val xs: List<Float>,
        val ys: List<Float>,
        @Optional val angle: Float = 0f,
        @Optional val scalex: Float = 1f,
        @Optional val scaley: Float = 1f) {
    @Transient
    val vertexList = ArrayList<Vector2>() // without repetition
    @Transient
    val vertexFloats: FloatArray // with closing loop

    init {
        myAssert(xs.size == ys.size) { "shape data list sizes don't match" }
        for (i in 0 until xs.size) {
            vertexList.add(vec2(xs[i], ys[i]))
        }
        val aabb = GdxUtil.computeAabbBoxForVectors(vertexList.asSequence())
        vertexList.forEach {
            it.set((it.x - aabb.center.x) / aabb.halfSize.x, (it.y - aabb.center.y) / aabb.halfSize.y)
            it.scl(scalex, scaley).rotate(angle)
        }
        val minDistance = 0.0051f // box2d chain shapes don't work otherwise
        var i = 0
        while (i < vertexList.size) { // remove vertices that are to close to another
            while (vertexList[i].dst2(vertexList[(i + 1) % vertexList.size]) < minDistance * minDistance) {
                vertexList.removeAt(i)
                if (i >= vertexList.size) {
                    break
                }
            }
            i += 1
        }
        vertexFloats = GdxUtil.vector2sToFloatArray(vertexList, closeLoop = true)
    }
}

@Serializable
data class ShapeInstanceData(
        val shape: String, // name of shape in shapeData
        @Optional val transform: TransformData = TransformData.Identity)

@Serializable
data class TransformData(
        @Optional val dx: Float = 0f,
        @Optional val dy: Float = 0f,
        @Optional val angle: Float = 0f, // in degrees
        @Optional val scalex: Float = 1f,
        @Optional val scaley: Float = 1f) {

    fun transformPoint(point: Vector2): Vector2 {
        return (point * vec2(scalex, scaley)).rotate(angle) + vec2(dx, dy)
    }

    companion object {
        val Identity = TransformData()
    }
}

@Serializable
data class ConnectionData(
        val from: Int,
        val to: Int,
        val type: Type) {
    fun normalized(): ConnectionData { // make sure that symmetric connections are well ordered, smaller person index first
        if (type.isSymmetric && from > to) {
            return ConnectionData(to, from, type)
        }
        return this
    }
}

@Serializable
data class GoalData( // defines end position of all persons
        val personIndex: Int,
        val x: Float,
        val y: Float,
        val angle: Float) {
    @Transient
    val center: Vector2
        get() = vec2(x, y)
}

@Serializable
data class StaticLevelData(
        @Optional val title: String = "<undefined title>",
        @Optional val introText: String = "<undefined intro text>",
        @Optional val width: Float = settings.defaultLevelWidth,
        @Optional val height: Float = settings.defaultLevelHeight,
        @Optional val shapes: List<ShapeTemplateData> = ArrayList(),
        @Optional val instances: List<ShapeInstanceData> = ArrayList()
)

@Serializable
data class DynamicLevelData(
        @Optional val persons: List<PersonData> = ArrayList(),
        @Optional val connections: List<ConnectionData> = ArrayList()
)

@Serializable
data class GoalLevelData(
        @Optional val timeToSimulate: Float = Level.defaultSimulationTime,
        @Optional val goals: List<GoalData> = ArrayList()) {

    fun clearEverythingExceptTime(): GoalLevelData {
        return GoalLevelData(timeToSimulate)
    }

    @Transient
    val isEmpty = goals.isEmpty()
}

@Serializable
data class LevelData(
        val static: StaticLevelData,
        val dynamic: DynamicLevelData,
        val goals: GoalLevelData)


