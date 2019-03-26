package de.sscholz.util


import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.GlyphLayout
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter
import com.badlogic.gdx.graphics.glutils.FrameBuffer
import com.badlogic.gdx.math.Matrix4
import kotlin.math.abs


object FontFactory {

    private val spriteBatch = SpriteBatch()

    class MyFont(fontName: String) {
        private val characters = FreeTypeFontGenerator.DEFAULT_CHARS
        private val generator: FreeTypeFontGenerator = FreeTypeFontGenerator(Gdx.files.internal(fontName))
        private val cache = HashMap<Int, BitmapFont>()
        private val layout = GlyphLayout()

        fun getOrLoadFontWithSize(fontSize: Int = 24): BitmapFont {
            if (cache.containsKey(fontSize))
                return cache[fontSize]!!
            val parameter = FreeTypeFontParameter()
            parameter.characters = characters
            parameter.size = fontSize
            val font = generator.generateFont(parameter)
            cache[fontSize] = font
            return font
        }

        // should only be used for static text textures
        fun renderToTexture(text: String, fontSize: Int): TextureRegion {
            val font = getOrLoadFontWithSize(fontSize)
            layout.setText(font, text)
            val absDescent = abs(font.descent)
            val maxPossibleFontHeight = font.capHeight + absDescent

            val fbo = FrameBuffer(Pixmap.Format.RGBA8888, layout.width.toInt(), maxPossibleFontHeight.toInt(), false)

            // Set up an ortho projection matrix
            val projMat = Matrix4()
            projMat.setToOrtho2D(0f, 0f, fbo.width.toFloat(), fbo.height.toFloat())
            spriteBatch.projectionMatrix = projMat

            // Render the text onto an FBO
            fbo.begin()
            spriteBatch.begin()
            font.draw(spriteBatch, layout, 0f, maxPossibleFontHeight)
            spriteBatch.end()
            fbo.end()

            // Flip the texture, and return it
            val tex = TextureRegion(fbo.colorBufferTexture)
            tex.flip(false, true)
            return tex
        }
    }

    val Default = MyFont("OpenSans-Regular.ttf")
    val Light = MyFont("OpenSans-Light.ttf")
    //    val LightItalic = MyFont("OpenSans-LightItalic.ttf")
//    val Italic = MyFont("OpenSans-Light.ttf")
    val Bold = MyFont("OpenSans-Bold.ttf")
    val Decorative = MyFont("decorative.ttf")

}