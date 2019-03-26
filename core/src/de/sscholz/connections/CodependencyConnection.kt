package de.sscholz.connections

import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.physics.box2d.Body
import com.badlogic.gdx.physics.box2d.BodyDef
import com.badlogic.gdx.physics.box2d.Joint
import de.sscholz.Global.settings
import de.sscholz.Global.shapeRenderer
import de.sscholz.Person
import de.sscholz.Physics.world
import de.sscholz.extensions.destroy
import de.sscholz.extensions.filterCategoryBits
import de.sscholz.extensions.filterMaskBits
import de.sscholz.util.defaultLinkHalfThickness
import de.sscholz.util.toDegree
import de.sscholz.util.toRadian
import ktx.box2d.body
import ktx.box2d.distanceJointWith
import ktx.box2d.revoluteJointWith
import ktx.graphics.use
import ktx.math.minus
import ktx.math.plus
import ktx.math.times

class CodependencyConnection(personA: Person, personB: Person) : Connection(personA, personB) {


    companion object {
        val linkDensity = settings.defaultDensity / 3f
    }

    override val type = Type.Codependency

    private val jointA: Joint
    private val jointB: Joint
    private val link: Body
    private val initialAngle: Float
    private val distanceJoint: Joint

    val distance = personA.worldCenter.dst(personB.worldCenter)

    init {
        val norDiff = (personB.worldCenter - personA.worldCenter).nor()

        initialAngle = norDiff.angle().toRadian()

        distanceJoint = personA.body.distanceJointWith(personB.body) {
            length = distance
            collideConnected = true
        }

        link = world.body {
            type = BodyDef.BodyType.DynamicBody
            box(position = (personA.worldCenter + personB.worldCenter) * 0.5f,
                    width = distance, height = 2f * defaultLinkHalfThickness, angle = initialAngle) {
                friction = settings.defaultFriction
                restitution = settings.defaultRestitution
                density = linkDensity
            }
        }
        link.filterCategoryBits = 0b0000000000001000
        link.filterMaskBits = 0b0000000000000110

        jointA = personA.body.revoluteJointWith(link) {
            initialize(personA.body, link, personA.worldCenter)
        }
        jointB = personB.body.revoluteJointWith(link) {
            initialize(personB.body, link, personB.worldCenter)
        }
    }

    override fun render() {
        with(shapeRenderer) {
            use(ShapeRenderer.ShapeType.Filled) {
                this.color.set(this@CodependencyConnection.color)
                identity()
                translate(link.worldCenter.x, link.worldCenter.y, 0f)
                rotate(0f, 0f, 1f, link.angle.toDegree() + initialAngle.toDegree())
                rect(-distance / 2f, -defaultLinkHalfThickness, distance, 2f * defaultLinkHalfThickness)
            }
        }
    }

    override fun destroy() {
        jointA.destroy()
        jointB.destroy()
        link.destroy()
        distanceJoint.destroy()
    }


}