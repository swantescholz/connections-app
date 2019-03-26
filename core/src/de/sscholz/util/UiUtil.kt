package de.sscholz.util

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.scenes.scene2d.ui.Dialog
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.utils.Align
import de.sscholz.Global.settings
import de.sscholz.extensions.asDrawable
import ktx.actors.onClick
import ktx.actors.onKey
import ktx.actors.onKeyDown
import ktx.assets.toInternalFile
import ktx.scene2d.Scene2DSkin
import ktx.style.*


object UiUtil {

    fun quitIfEscapeIsPressed() {
        if (Gdx.input.isKeyPressed(Input.Keys.ESCAPE)) {
            quit()
        }
    }


    // creates my default dialog, with wrapped message label
    // can also be scrollable
    // callback gets the object associated with the button, and the dialog
    fun myDialog(title: String, longWrappableMessage: String,
                 closeOnClickAnywhere: Boolean = false,
                 scrollable: Boolean = false, scrollPaneHeight: Float = howToPlayScrollPaneHeight,
                 onResult: (Any?, Dialog) -> Unit = { _, _ -> }): Dialog {
        return object : Dialog(title, Scene2DSkin.defaultSkin) {
            override fun result(`object`: Any?) {
                onResult(`object`, this)
            }
        }.apply {
            this.contentTable.align(Align.left)
            this.buttonTable.align(Align.center)

            this.buttonTable.defaults().height(dialogButtonHeight)
            this.buttonTable.defaults().width(dialogButtonMinWidth)
            this.buttonTable.defaults().padTop(dialogButtonTopPadding)
            onKeyDown { key ->
                if (key == Input.Keys.BACK) {
                    this.remove()
                }
            }
            onKey {
                if (Gdx.input.isKeyPressed(Input.Keys.ESCAPE)) {
                    this.remove()
                }
            }
            if (closeOnClickAnywhere) {
                onClick {
                    this.remove()
                }
            }
            pad(dialogPaddingNormal)
            padTop(dialogPaddingTop)

            val label = Label(longWrappableMessage, Scene2DSkin.defaultSkin)
            label.setWrap(true)
            label.width = 10f
            if (scrollable) {
                val scrollPane = ScrollPane(label)
                contentTable.add(scrollPane).height(scrollPaneHeight).width(maxRelativeDialogWidth * screenWidth)
            } else {
                contentTable.add(label).width(maxRelativeDialogWidth * screenWidth)
            }
        }
    }

    fun createMyDefaultSkin(): Skin {
        val skin = Skin("uiskin.json".toInternalFile())
        skin.add("play", Texture("play.png"))
        skin.add("pause", Texture("pause.png"))
        skin.add("reset", Texture("reset.png"))
        skin.label("default", extend = "default") {
            font = FontFactory.Default.getOrLoadFontWithSize(24)
        }
        skin.label("withbg", extend = "default") {
            background = Color.SALMON.asDrawable()
        }
        skin.label("decorative") {
            font = FontFactory.Decorative.getOrLoadFontWithSize(64)
        }
        skin.textButton("default", extend = "default") {
            font = FontFactory.Default.getOrLoadFontWithSize(24)
            this.fontColor = settings.defaultButtonTextColor
        }
        skin.textButton("large", extend = "default") {
            font = FontFactory.Default.getOrLoadFontWithSize(36)
        }
        skin.selectBox("default", extend = "default") {
            font = FontFactory.Default.getOrLoadFontWithSize(32)
            background.leftWidth = 10f
            listStyle.font = FontFactory.Default.getOrLoadFontWithSize(30)
            this.fontColor = settings.defaultButtonTextColor
            this.listStyle.fontColorUnselected = settings.defaultButtonTextColor
            listStyle.selection.leftWidth = 10f
            listStyle.selection.topHeight = selectBoxListExtraTopBottomHeight
            listStyle.selection.bottomHeight = selectBoxListExtraTopBottomHeight

        }
        skin.textField("default", extend = "default") {
            this.fontColor = Color.SALMON
        }
        skin.window("default", extend = "default") {
            titleFont = FontFactory.Default.getOrLoadFontWithSize(24)
            titleFontColor = Color.GOLD
            this.background = Color.DARK_GRAY.asDrawable()
        }
        return skin
    }

    fun playMusic(internalFilePath: String) {
        Gdx.audio.newMusic(internalFilePath.toInternalFile()).apply {
            volume = 0.3f
            setOnCompletionListener { play() }
        }.play()
    }
}