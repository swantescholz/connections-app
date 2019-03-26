package de.sscholz.connections

import com.badlogic.gdx.physics.box2d.Joint
import de.sscholz.Global
import de.sscholz.Person
import de.sscholz.Physics.world
import de.sscholz.extensions.drawEllipseBoundary
import ktx.box2d.ropeJointWith
import ktx.math.minus
import ktx.math.plus
import ktx.math.times

class FamilyConnection(personA: Person, personB: Person) : Connection(personA, personB) {
    override val type = Type.Family
    private val joint: Joint
    private val maxLength: Float


    init {
        maxLength = (personA.worldCenter - personB.worldCenter).len() + Connections.epsilon
        joint = personA.body.ropeJointWith(personB.body) {
            this.maxLength = this@FamilyConnection.maxLength
            collideConnected = true
        }
    }

    override fun render() {
        val angle = (personA.worldCenter - personB.worldCenter).angle()
        Global.shapeRenderer.drawEllipseBoundary(personA.worldCenter * 0.5f + personB.worldCenter * 0.5f,
                maxLength, Math.max(personA.size, personB.size) * 4, angle, color)
    }

    override fun destroy() {
        world.destroyJoint(joint)
    }
}

