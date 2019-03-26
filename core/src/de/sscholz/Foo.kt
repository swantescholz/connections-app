package de.sscholz

import com.badlogic.gdx.graphics.OrthographicCamera
import de.sscholz.util.printl
import kotlinx.serialization.Serializable
import ktx.math.vec3


fun fooFun() {
    val orthoCam = OrthographicCamera(1000f, 865f)
    val x = orthoCam.unproject(vec3(1000f, 865f, 0f), 0f, 0f, 1000f, 865f)
    printl(x)
//    quit()
}

@Serializable
data class A(val x: Float)

fun main() {
//    fooFun()
    val a = A(1 / 10f)
    val s = myjson.stringify(A.serializer(), a)
    val b = myjson.parse(A.serializer(), s)
    printl(s)
    printl(a == b)
}
