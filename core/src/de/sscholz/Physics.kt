package de.sscholz

import de.sscholz.Global.settings
import ktx.box2d.createWorld


object Physics {
    var world = createWorld(gravity = settings.gravity)
        private set

    fun resetWorld() {

        world = createWorld(gravity = settings.gravity)
    }
}