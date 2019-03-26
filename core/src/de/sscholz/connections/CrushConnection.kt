package de.sscholz.connections

import de.sscholz.Global
import de.sscholz.Global.settings
import de.sscholz.Person
import de.sscholz.extensions.applyAccelerationToCenter
import de.sscholz.extensions.drawPointingTriangle
import de.sscholz.util.defaultMaximumForceStrength
import ktx.math.minus
import ktx.math.times

class CrushConnection(personA: Person, personB: Person) : Connection(personA, personB) {
    override val type = Type.Crush

    private val linearForceStrength = defaultMaximumForceStrength * (1 - personA.empathy)

    override fun innerUpdate(deltaTime: Float) {
        val diff = personB.worldCenter - personA.worldCenter
        val direction = diff.nor() * linearForceStrength
        personA.body.applyAccelerationToCenter(settings.gravity * -1f) // set gravity effectively to zero
        personA.body.applyForceToCenter(direction, true)
    }


    override fun render() {
        val baseHalfWidth = personA.size * 0.4f
        Global.shapeRenderer.drawPointingTriangle(personA.worldCenter, personB.worldCenter, baseHalfWidth, color)
    }

}

