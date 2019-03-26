package de.sscholz.connections

import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.Body
import com.badlogic.gdx.physics.box2d.BodyDef
import com.badlogic.gdx.physics.box2d.Joint
import de.sscholz.Global.settings
import de.sscholz.Global.shapeRenderer
import de.sscholz.Person
import de.sscholz.Physics.world
import de.sscholz.extensions.drawCircleBoundary
import de.sscholz.extensions.filterCategoryBits
import de.sscholz.extensions.filterMaskBits
import de.sscholz.util.defaultLinkHalfThickness
import de.sscholz.util.toDegree
import de.sscholz.util.toRadian
import ktx.box2d.body
import ktx.box2d.distanceJointWith
import ktx.box2d.ropeJointWith
import ktx.graphics.use
import ktx.math.*

class FriendshipConnection(personA: Person, personB: Person) : Connection(personA, personB) {

    companion object {
        const val numberOfChainLinks = 10
        const val distanceJointOffsetFactor = 0.1f
        const val endAnchorFactor = 1.0f
        const val miniCircleRadius = 0.4f
        val linkDensity = settings.defaultDensity
    }

    override val type = Type.Friendship
    private var joints = ArrayList<Joint>()
    private var links = ArrayList<Body>()

    private val linkLength: Float
    private val initialAngle: Float

    init {
        val norDiff = (personB.worldCenter - personA.worldCenter).nor()
        val firstAnchor = norDiff * personA.size * endAnchorFactor + personA.worldCenter
        val lastAnchor = -norDiff * personB.size * endAnchorFactor + personB.worldCenter
        val linkDirection = (lastAnchor - firstAnchor) / numberOfChainLinks
        linkLength = linkDirection.len()
        initialAngle = (lastAnchor - firstAnchor).angle().toRadian()
        var previousBody = personA.body

        for (i in 1..numberOfChainLinks) {
            val position = Vector2(firstAnchor).lerp(lastAnchor, (i - 1f) / numberOfChainLinks)
            val link = world.body {
                type = BodyDef.BodyType.DynamicBody
                box(position = position + linkDirection / 2f, width = linkLength * 1.0f, height = 2f * defaultLinkHalfThickness,
                        angle = initialAngle) {
                    friction = settings.defaultFriction
                    restitution = settings.defaultRestitution
                    density = linkDensity
                }
            }
            link.filterCategoryBits = 0b0000000000001000
            link.filterMaskBits = 0b0000000000000110
            links.add(link)
            if (i > 1) {
                val joint = previousBody.ropeJointWith(link) {
                    val worldAnchorA = position - (linkDirection / 2f) * distanceJointOffsetFactor
                    val worldAnchorB = position + (linkDirection / 2f) * distanceJointOffsetFactor
//                    frequencyHz = 0.0f // todo: tweak for softness
//                    dampingRatio = 1.0f
                    this.maxLength = worldAnchorA.dst(worldAnchorB) * 0.99f
                    this.localAnchorA.set(worldAnchorA - previousBody.position)
                    this.localAnchorB.set(worldAnchorB - link.position)
//                    initialize(previousBody, link, worldAnchorA, worldAnchorB)
                }
                joints.add(joint)
            }
            previousBody = link
        }
        joints.add(0, personA.body.distanceJointWith(links.first()) {
            initialize(personA.body, links.first(), personA.worldCenter, firstAnchor)
        })
        joints.add(links.last().distanceJointWith(personB.body) {
            initialize(links.last(), personB.body, lastAnchor, personB.worldCenter)
        })
    }

    override fun render() {
        with(shapeRenderer) {
            use(ShapeRenderer.ShapeType.Filled) {
                links.forEach { link ->
                    this.color.set(this@FriendshipConnection.color)
                    identity()
                    translate(link.worldCenter.x, link.worldCenter.y, 0f)
                    rotate(0f, 0f, 1f, link.angle.toDegree() + initialAngle.toDegree())
                    rect(-linkLength / 2f, -defaultLinkHalfThickness, linkLength, 2f * defaultLinkHalfThickness)
                }
            }
        }
        for (i in 1 until joints.size - 1) {
            shapeRenderer.drawCircleBoundary(joints[i].anchorA, miniCircleRadius, color)
            shapeRenderer.drawCircleBoundary(joints[i].anchorB, miniCircleRadius, color)
        }
        shapeRenderer.drawCircleBoundary(joints.first().anchorB, miniCircleRadius * 2, color)
        shapeRenderer.drawCircleBoundary(joints.last().anchorA, miniCircleRadius * 2, color)
    }

    override fun destroy() {
        joints.forEach {
            world.destroyJoint(it)
        }
        links.forEach {
            world.destroyBody(it)
        }
    }


}