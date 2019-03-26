package de.sscholz

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.InputMultiplexer
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.Touchable
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable
import com.badlogic.gdx.utils.Align
import de.sscholz.Global.settings
import de.sscholz.util.*
import de.sscholz.util.UiUtil.myDialog
import ktx.actors.onClick
import ktx.app.KtxInputAdapter
import ktx.app.KtxScreen
import ktx.scene2d.*


class MainMenuScreen(private val application: App) : KtxScreen, KtxInputAdapter {
    companion object {
        val buttonPadding = 30f
    }

    init {
        Gdx.input.isCatchBackKey = true
    }

    private val uiStage = Stage()
    private val backgroundImage = Texture("bg-main-menu.png")
    private lateinit var root: Table
    private var cheatCounter = 0

    private fun createRootTable() {
        root = table {
            setFillParent(true)

            background = TextureRegionDrawable(TextureRegion(backgroundImage))
            touchable = Touchable.enabled

            align(Align.center)
            val muteStrings = arrayOf("Mute", "Unmute")
            textButton(muteStrings[Preferences.mute.get().i]) {
                pad(buttonPadding)
                this.color = settings.defaultButtonBgColor
                onClick {
                    Preferences.mute.set(!Preferences.mute.get())
                    cheatCounter += 1
                    if (cheatCounter == 10) {
                        Preferences.unlockedLevels.set(100)
                        this@MainMenuScreen.hide()
                        this@MainMenuScreen.show()
                    }
                    this@textButton.setText(muteStrings[Preferences.mute.get().i])
                }
            }.cell(row = true, align = Align.topRight, width = 200f)

            align(Align.center)
            label(text = "Connections - Menu\n", style = "decorative") {
                height = 100f
                color = Color.GOLD
            }.cell(row = true)
            table {
                textButton("How to play", style = "large") {
                    pad(buttonPadding)
                    this.color = settings.defaultButtonBgColor
                    onClick { showHowToPlayDialog() }
                }.cell()
                textButton("Credits", style = "large") {
                    pad(buttonPadding)
                    this.color = settings.defaultButtonBgColor
                    onClick { showCreditsDialog() }
                }.cell()
            }.cell(row = true, padBottom = 40f)
            table {
                align(Align.top)

                for (levelIndex in 1..numberOfLevels) {
                    val button = textButton("Level $levelIndex", style = "default") {
                        this.color = settings.defaultButtonBgColor
                        if (levelIndex > Preferences.unlockedLevels.get()) {
                            this.touchable = Touchable.disabled
                            this.color = disabledButtonColor
                        }
                        onClick {
                            try {
                                GameScreen.levelIndexToLoad = levelIndex
                                application.setScreen<GameScreen>()
                            } catch (t: Throwable) {
                                application.setScreen<MainMenuScreen>()
                                log(t)
                                t.printStackTrace()
                                Toasts.showToast("Level could not be loaded.")
                            }
                        }
                        pad(buttonPadding)
                    }
                    button.cell(row = levelIndex % 4 == 0, grow = true, expand = true, fill = true)
                }
            }.cell(padBottom = 50f, expand = true)
        }
    }

    private fun showHowToPlayDialog() {
        myDialog("How To Play", LongTexts.howToPlay, false, true, howToPlayScrollPaneHeight) { eventObject, dialog ->
            dialog.remove()
        }.apply {
            button("Back", 1)
        }.show(uiStage)
    }

    private fun showCreditsDialog() {
        myDialog("Credits", LongTexts.credits, false, true, howToPlayScrollPaneHeight) { eventObject, dialog ->
            dialog.remove()
        }.apply {
            button("Back", 1)
        }.show(uiStage)
    }

    private fun KDialog.tableWithTextAndBack(text: String): KTableWidget {
        return table {
            label(text = text).cell(
                    row = true, padBottom = 30f, align = Align.left)
            textButton(text = "Back", style = "large") {
                this.pad(buttonPadding)
            }.cell(align = Align.center, grow = true, expand = true, fill = true)
        }.cell(align = Align.center)
    }

    override fun keyDown(keycode: Int): Boolean {
        when (keycode) {
            Input.Keys.ESCAPE, Input.Keys.BACK -> quit()
        }
        return false
    }

    override fun show() {
        createRootTable()
        uiStage.addActor(root)
        val inputMultiplexer = InputMultiplexer()
        inputMultiplexer.addProcessor(uiStage)
        inputMultiplexer.addProcessor(this)
        Gdx.input.inputProcessor = inputMultiplexer
        cheatCounter = 0
    }

    override fun render(delta: Float) {
        uiStage.act(delta)
        uiStage.draw()
        Toasts.render()
    }

    override fun hide() {
        root.remove()
    }
}