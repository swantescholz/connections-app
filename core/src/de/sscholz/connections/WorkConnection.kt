package de.sscholz.connections

import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.physics.box2d.Body
import com.badlogic.gdx.physics.box2d.BodyDef
import com.badlogic.gdx.physics.box2d.Joint
import de.sscholz.Global
import de.sscholz.Global.shapeRenderer
import de.sscholz.Person
import de.sscholz.Physics.world
import de.sscholz.extensions.*
import de.sscholz.util.defaultLinkHalfThickness
import de.sscholz.util.defaultMaximumForceStrength
import de.sscholz.util.toDegree
import de.sscholz.util.toRadian
import ktx.box2d.body
import ktx.box2d.revoluteJointWith
import ktx.graphics.use
import ktx.math.minus
import ktx.math.plus
import ktx.math.times
import ktx.math.vec2

class WorkConnection(personA: Person, personB: Person) : Connection(personA, personB) {
    override val type = Type.Work

    companion object {
        val linkDensity = Global.settings.defaultDensity
        val squareScalingFactor = 2.2f
    }

    private val linearForceStrength = defaultMaximumForceStrength * personA.reliability
    private val jointA: Joint
    private val jointB: Joint
    private val link: Body
    private val initialAngle: Float
    val distance = personA.worldCenter.dst(personB.worldCenter)

    init {
        val norDiff = (personB.worldCenter - personA.worldCenter).nor()

        initialAngle = norDiff.angle().toRadian()

        link = world.body {
            type = BodyDef.BodyType.DynamicBody
            box(position = (personA.worldCenter + personB.worldCenter) * 0.5f,
                    width = distance, height = 2f * defaultLinkHalfThickness, angle = initialAngle) {
                friction = Global.settings.defaultFriction
                restitution = Global.settings.defaultRestitution
                density = linkDensity
            }
        }
        link.gravityScale = 0f
        link.filterCategoryBits = 0b0000000000001000
        link.filterMaskBits = 0b0000000000000110

        jointA = personA.body.revoluteJointWith(link) {
            initialize(personA.body, link, personA.worldCenter)
        }
        jointB = personB.body.revoluteJointWith(link) {
            initialize(personB.body, link, personB.worldCenter)
        }
    }

    override fun innerUpdate(deltaTime: Float) {
        val diff = (personB.worldCenter - personA.worldCenter).nor()
        val force = vec2(-diff.y, diff.x) * linearForceStrength
        personA.body.applyAccelerationToCenter(Global.settings.gravity * -1f) // set gravity effectively to zero
        personA.body.applyForceToCenter(force, true)
    }

    override fun render() {
        with(Global.shapeRenderer) {
            use(ShapeRenderer.ShapeType.Filled) {
                this.color.set(this@WorkConnection.color)
                identity()
                translate(link.worldCenter.x, link.worldCenter.y, 0f)
                rotate(0f, 0f, 1f, link.angle.toDegree() + initialAngle.toDegree())
                rect(-distance / 2f, -defaultLinkHalfThickness, distance, 2f * defaultLinkHalfThickness)
            }
        }
        val diff = (personB.worldCenter - personA.worldCenter).nor()
        val angle = diff.angle() + 45f
        shapeRenderer.drawSquare(personB.worldCenter, squareScalingFactor * personB.size / Math.sqrt(2.0).toFloat(),
                angleInDegrees = angle, color = this.color)
    }

    override fun destroy() {
        jointA.destroy()
        jointB.destroy()
        link.destroy()
    }

}


