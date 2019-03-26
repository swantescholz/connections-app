package de.sscholz

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.InputMultiplexer
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.Touchable.enabled
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable
import com.badlogic.gdx.utils.Align
import de.sscholz.extensions.asFittingBackground
import de.sscholz.util.Preferences
import de.sscholz.util.numberOfLevels
import de.sscholz.util.quit
import ktx.actors.onClick
import ktx.app.KtxInputAdapter
import ktx.app.KtxScreen
import ktx.scene2d.label
import ktx.scene2d.table

class IntroScreen(private val application: App) : KtxScreen, KtxInputAdapter {
    private val uiStage = Stage()
    private val backgroundImage = Texture("bg-intro.png").asFittingBackground()
    private lateinit var loadingLabel: Label
    private val root = table {
        setFillParent(true)

        background = TextureRegionDrawable(TextureRegion(backgroundImage))
        touchable = enabled
        onClick {
            if (Preferences.levelsHaveBeenLoaded.get()) {
                application.setScreen<MainMenuScreen>()
            }
        }
        align(Align.top)
        table {
            label(text = "CONNECTIONS", style = "decorative") {
                color = Color.GOLD
            }.cell(row = true, padTop = 15f)
            label(text = "A game about human interactions") {
                color = Color.GOLDENROD
            }.cell(row = true, pad = 0f)
            loadingLabel = label(text = "Loading") {
                color = Color.GOLDENROD
            }.cell(row = true)
        }.cell()
    }

    init {
        Gdx.input.isCatchBackKey = true
    }

    override fun keyDown(keycode: Int): Boolean {
        when (keycode) {
            Input.Keys.ESCAPE, Input.Keys.BACK -> quit()
        }
        return false
    }

    override fun show() {
        uiStage.addActor(root)
        val inputMultiplexer = InputMultiplexer()
        inputMultiplexer.addProcessor(uiStage)
        inputMultiplexer.addProcessor(this)
        Gdx.input.inputProcessor = inputMultiplexer
    }

    private var currentLevelIndexToLoad = 0
    private lateinit var level: Level

    override fun render(delta: Float) {
        uiStage.act(delta)
        uiStage.draw()
        if (currentLevelIndexToLoad <= numberOfLevels && !Preferences.levelsHaveBeenLoaded.get()) {
            if (currentLevelIndexToLoad >= 1) {
                level = Level(currentLevelIndexToLoad, uiStage, true) {}
                level.doSimulationAndSaveGoalDataIfNoLocalGoalDataExists()
                level.destroy()
            }
            loadingLabel.setText("Loading... ($currentLevelIndexToLoad/$numberOfLevels)")
            currentLevelIndexToLoad += 1
        } else {
            if (!Preferences.levelsHaveBeenLoaded.get()) {
                Preferences.levelsHaveBeenLoaded.set(true)
            }
            val toPlayText = "Tap to play"
            if (loadingLabel.text.toString() != toPlayText) {
                loadingLabel.setText(toPlayText)
            }
        }
    }

    override fun hide() {
        root.remove()
    }
}


