package de.sscholz

import com.badlogic.gdx.Screen
import de.sscholz.util.UiUtil
import ktx.app.KtxGame
import ktx.scene2d.Scene2DSkin

class App : KtxGame<Screen>() {

    override fun create() {
        Scene2DSkin.defaultSkin = UiUtil.createMyDefaultSkin()

        val game = GameScreen(this)
        val mainMenu = MainMenuScreen(this)
        val intro = IntroScreen(this)
        addScreen(game)
        addScreen(mainMenu)
        addScreen(intro)
        if (editMode) {
            setScreen<GameScreen>()
        } else {
            setScreen<IntroScreen>()
        }
    }

}
