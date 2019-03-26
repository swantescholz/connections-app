package de.sscholz.extensions

import com.badlogic.gdx.input.GestureDetector
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.math.Vector3
import de.sscholz.Global
import ktx.math.vec2
import ktx.math.vec3

fun Vector3.toVector2() = vec2(x, y)
fun Vector2.toVector3() = vec3(x, y, 0f)

fun GestureDetector.GestureAdapter.asGestureDetector(): GestureDetector {
    return GestureDetector(20f, 0.4f, Global.settings.defaultLongClickInterval, 0.15f, this)
}

//fun Actor.fireClick() { // maybe breaks mouse touch up events after used?
//    fire(InputEvent().apply { type = InputEvent.Type.touchDown })
//    fire(InputEvent().apply { type = InputEvent.Type.touchUp })
//}
