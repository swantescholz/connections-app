package de.sscholz.extensions

import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.Body
import com.badlogic.gdx.physics.box2d.Joint
import com.badlogic.gdx.physics.box2d.World
import de.sscholz.Level
import de.sscholz.Physics
import de.sscholz.TransformData
import de.sscholz.util.log
import de.sscholz.util.toDegree
import ktx.math.times

fun Body.destroy() {
    world.destroyBody(this)
}

fun Joint.destroy() {
    Physics.world.destroyJoint(this)
}

fun World.printDebugInfo(level: Level) {
    log("===== Box2D counts =====")
    log("#bodies: ${bodyCount}")
    log("#joints: ${jointCount}")
    log("#fixtures: ${fixtureCount}")
    log("#contacts: ${contactCount}")
    log("#connections: ${level.connections.connections.size}")
}

fun Body.transformData(): TransformData {
    return TransformData(worldCenter.x, worldCenter.y, angle.toDegree())
}

fun Body.moveTo(newPosition: Vector2) {
    isActive = true
    setTransform(newPosition, angle)
}

fun Body.applyAccelerationToCenter(additionalAcceleration: Vector2) {
    applyForceToCenter(additionalAcceleration * mass, true)
}

fun Body.applyVelocityToCenter(velocityChange: Vector2) {
    applyLinearImpulse(velocityChange * mass, worldCenter, true)
}

fun Body.applyLinearImpulseToCenter(impulse: Vector2) {
    applyLinearImpulse(impulse, worldCenter, true)
}

var Body.filterCategoryBits: Short
    get() = fixtureList[0].filterData.categoryBits
    set(value) {
        fixtureList.forEach {
            val fd = it.filterData
            fd.categoryBits = value
            it.filterData = fd
        }
    }
var Body.filterMaskBits: Short
    get() = fixtureList[0].filterData.maskBits
    set(value) {
        fixtureList.forEach {
            val fd = it.filterData
            fd.maskBits = value
            it.filterData = fd
        }
    }