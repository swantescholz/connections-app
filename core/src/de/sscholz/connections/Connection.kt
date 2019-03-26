package de.sscholz.connections

import com.badlogic.gdx.graphics.Color
import de.sscholz.ConnectionData
import de.sscholz.Global.settings
import de.sscholz.Person

abstract class Connection(val personA: Person, val personB: Person) {
    abstract val type: Type
    open val color: Color = settings.defaultConnectionColor
    abstract fun render()
    open fun destroy() {}


    // deltaTime will be the physics step size taken (should be constant)
    fun update(deltaTime: Float) {
        innerUpdate(deltaTime)
        isFirstUpdate = false
    }

    protected open fun innerUpdate(deltaTime: Float) {}
    fun createConnectionData(): ConnectionData {
        return ConnectionData(personA.personIndex, personB.personIndex, type)
    }

    private var isFirstUpdate = true

    fun toNiceString(): String {
        return "${personA.shortName} -> ${personB.shortName}: ${type}"
    }
}