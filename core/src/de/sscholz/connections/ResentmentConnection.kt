package de.sscholz.connections

import de.sscholz.Global.shapeRenderer
import de.sscholz.Person
import de.sscholz.extensions.drawCircleBoundary
import de.sscholz.extensions.drawSimpleLine
import de.sscholz.util.defaultMaximumForceStrength
import ktx.math.div
import ktx.math.minus
import ktx.math.plus
import ktx.math.times

class ResentmentConnection(personA: Person, personB: Person) : Connection(personA, personB) {
    override val type = Type.Resentment
    private val radius = maxRadius * (personA.ego)
    private val linearForceStrength = defaultMaximumForceStrength * (1 - personA.empathy)

    companion object {
        private val maxRadius = 50f
    }


    override fun innerUpdate(deltaTime: Float) {
        val diff = personB.worldCenter - personA.worldCenter
        if (diff.len() > radius) {
            return
        }
        val direction = diff.nor() * linearForceStrength
        personB.body.applyForceToCenter(direction, true)
    }

    override fun render() {
        shapeRenderer.drawCircleBoundary(personA.worldCenter, radius, color)
        val diff = (personB.worldCenter - personA.worldCenter).nor()
        val lineStart = personA.worldCenter + diff * radius
        val lineEnd = lineStart + diff * radius / 2f
        shapeRenderer.drawSimpleLine(lineStart, lineEnd, color)
    }

}


