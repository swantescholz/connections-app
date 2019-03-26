package de.sscholz.connections

import de.sscholz.Global.shapeRenderer
import de.sscholz.Person
import de.sscholz.extensions.drawPointingTriangle
import de.sscholz.util.defaultMaximumForceStrength
import ktx.math.minus
import ktx.math.plus
import ktx.math.times

class HugConnection(personA: Person, personB: Person) : Connection(personA, personB) {
    override val type = Type.Hug
    private var haveHugged = false // will turn true after bodies touch
    private val linearForceStrength = defaultMaximumForceStrength

    override fun innerUpdate(deltaTime: Float) {
        if (haveHugged) {
            return
        }
        val diff = personB.worldCenter - personA.worldCenter
        if (diff.len() <= (personA.size + personB.size) * 1.05) { // a very simple near collision check
            haveHugged = true
            return
        }
        val direction = diff.nor() * linearForceStrength
        personA.body.applyForceToCenter(direction, true)
        personB.body.applyForceToCenter(direction * -1f, true)
    }

    override fun render() {
        if (haveHugged) {
            return
        }
        val center = (personA.worldCenter + personB.worldCenter) * 0.5f
        val baseHalfWidth = (personA.size + personB.size) * 0.5f
        shapeRenderer.drawPointingTriangle(personA.worldCenter, center, baseHalfWidth, color)
        shapeRenderer.drawPointingTriangle(personB.worldCenter, center, baseHalfWidth, color)
    }

}


