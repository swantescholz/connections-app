package de.sscholz.connections

import com.badlogic.gdx.physics.box2d.joints.RevoluteJoint
import de.sscholz.Global
import de.sscholz.Person
import de.sscholz.Physics.world
import de.sscholz.extensions.destroy
import de.sscholz.extensions.drawSimpleLine
import de.sscholz.extensions.drawTriangle
import de.sscholz.util.DefaultHashMap
import ktx.box2d.body
import ktx.box2d.revoluteJointWith
import ktx.math.div
import ktx.math.minus
import ktx.math.plus
import ktx.math.times

class AbuseConnection(personA: Person, personB: Person) : Connection(personA, personB) {
    override val type = Type.Abuse
    private val radius = maxRadius * (1 - personA.empathy)
    private var joint: RevoluteJoint? = null
    private val groundBody = world.body()

    companion object {
        private val maxRadius = 50f
        private val abuseSets = DefaultHashMap<Person, HashSet<Connection>>({ HashSet() })
    }


    override fun innerUpdate(deltaTime: Float) {
        val diff = personB.worldCenter - personA.worldCenter
        if (diff.len() > radius) {
            joint?.destroy()
            joint = null
            abuseSets[personB].remove(this)
            return
        }
        abuseSets[personB].add(this)
        if (joint == null && abuseSets[personB].size == 1) {
            joint = personB.body.revoluteJointWith(groundBody) {
                collideConnected = true
                initialize(personB.body, groundBody, personB.body.position)
            }
        }
    }

    override fun render() {
        val diff = (personB.worldCenter - personA.worldCenter).nor()
        val lineStart = personA.worldCenter + diff * radius
        val lineEnd = lineStart + diff * radius / 2f
        val angle = diff.angle() - 90f
        Global.shapeRenderer.drawTriangle(personA.worldCenter, radius, angle, color)
        Global.shapeRenderer.drawSimpleLine(lineStart, lineEnd, color)
    }

    override fun destroy() {
        joint?.destroy()
        groundBody.destroy()
        abuseSets[personB].remove(this)
    }

}