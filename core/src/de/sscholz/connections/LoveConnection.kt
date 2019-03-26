package de.sscholz.connections

import de.sscholz.Global
import de.sscholz.Global.settings
import de.sscholz.Person
import de.sscholz.extensions.applyAccelerationToCenter
import de.sscholz.extensions.drawHeart
import de.sscholz.util.defaultMaximumForceStrength
import ktx.math.minus
import ktx.math.plus
import ktx.math.times
import ktx.math.unaryMinus

class LoveConnection(personA: Person, personB: Person) : Connection(personA, personB) {
    override val type = Type.Love
    private val linearForceStrength = defaultMaximumForceStrength

    init {
//        personA.body.filterCategoryBits = 0b0000000000000100
//        personB.body.filterCategoryBits = 0b0000000000000100
//        personA.body.filterMaskBits = 0b0111111111111011
//        personB.body.filterMaskBits = 0b0111111111111011
    }

    override fun innerUpdate(deltaTime: Float) {
        val diff = personB.worldCenter - personA.worldCenter
        val direction = diff.nor() * linearForceStrength
        personA.body.applyAccelerationToCenter(settings.gravity * antiGravityFactor) // make persons float
        personB.body.applyAccelerationToCenter(settings.gravity * antiGravityFactor)
        personA.body.applyForceToCenter(direction, true)
        personB.body.applyForceToCenter(-direction, true)
    }

    override fun render() {
        val sizeFactor = 0.8f
        val diff = (personB.worldCenter - personA.worldCenter).nor()
        val angle = diff.angle()
        Global.shapeRenderer.drawHeart(personA.worldCenter + diff * personA.size,
                personA.size * sizeFactor, angle + 90f, color)
        Global.shapeRenderer.drawHeart(personB.worldCenter - diff * personB.size,
                personB.size * sizeFactor, angle - 90f, color)
    }

    companion object {
        const val antiGravityFactor = -1.2f
    }

}





