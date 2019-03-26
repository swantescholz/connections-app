package de.sscholz.util

import com.badlogic.gdx.Gdx
import de.sscholz.editMode


object Sounds {

    class Sound(filename: String) {
        private val gdxSound = Gdx.audio.newSound(Gdx.files.internal("${basePath}$filename"))
        fun play() {
            if (!Preferences.mute.get() && !editMode) {
                gdxSound.play()
            }
        }
    }

    fun load() {
        kick
        solved
        error
    }

    val basePath = ""
    val kick by lazy { Sound("kick.wav") }
    val solved by lazy { Sound("solved.mp3") }
    val error by lazy { Sound("error.mp3") }
}