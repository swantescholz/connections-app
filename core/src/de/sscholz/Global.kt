package de.sscholz

import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import kotlinx.serialization.json.Json
import ktx.freetype.registerFreeTypeFontLoaders

var editMode: Boolean = true
val releaseMode: Boolean = true
var debugLogging: Boolean = true

object Global {

    val assetManager by lazy { AssetManager().apply { registerFreeTypeFontLoaders() } }
    val defaultSpriteBatch by lazy { SpriteBatch() }
    val shapeRenderer = ShapeRenderer()
    private var _settings: Settings? = null
    var settings: Settings
        get() {
            if (_settings == null) {
                _settings = Settings.reloadFromConfigFile(false)
            }
            return _settings!!
        }
        set(value) {
            _settings = value
        }

}

val myjson = Json(indented = true, indent = "\t", unquoted = true)