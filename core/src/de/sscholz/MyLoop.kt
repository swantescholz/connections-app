package de.sscholz

import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.BodyDef
import com.badlogic.gdx.scenes.scene2d.Actor
import de.sscholz.Physics.world
import de.sscholz.util.GdxUtil
import de.sscholz.util.toDegree
import ktx.box2d.body
import ktx.graphics.use

class MyLoop(points: ArrayList<Vector2>) : Actor() {

    private val floatsPlus2 = GdxUtil.vector2sToFloatArray(points, true)

    val body = world.body {
        val pointsAsFloats = GdxUtil.vector2sToFloatArray(points, false)
        loop(pointsAsFloats) {
            density = Global.settings.defaultDensity
            friction = Global.settings.defaultFriction
            restitution = Global.settings.defaultRestitution
        }
        type = BodyDef.BodyType.StaticBody
    }

    override fun draw(batch: Batch, parentAlpha: Float) {
        with(Global.shapeRenderer) {
            use(ShapeRenderer.ShapeType.Line) {
                identity()
                translate(body.position.x, body.position.y, 0f)
                rotate(0f, 0f, 1f, body.angle.toDegree())
                color = super.getColor()
                polyline(floatsPlus2)
            }
        }
    }

    fun destroy() {
        remove()
        world.destroyBody(body)
    }
}