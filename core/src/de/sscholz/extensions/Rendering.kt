package de.sscholz.extensions

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.Body
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.utils.Drawable
import de.sscholz.Global
import de.sscholz.TransformData
import de.sscholz.util.GdxUtil
import de.sscholz.util.alof
import de.sscholz.util.toDegree
import ktx.graphics.circle
import ktx.graphics.use
import ktx.math.minus
import ktx.math.plus
import ktx.math.times
import ktx.math.vec2

// crops texture, so that screen is completely filled, without scaling
fun Texture.asFittingBackground(): TextureRegion {
    val screenWidth = Gdx.graphics.width
    val screenHeight = Gdx.graphics.height
    if (screenHeight / screenWidth.toFloat() < this.height.toFloat() / this.width) {
        // screen has small height, relative to texture -> crop top/bottom
        val h = this.width * screenHeight / screenWidth
        val y = (this.height - h) / 2
        return TextureRegion(this, 0, y, this.width, h)
    } else {
        // screen has small width, relative to texture -> crop left/right
        val w = this.height * screenWidth / screenHeight
        val x = (this.width - w) / 2
        return TextureRegion(this, x, 0, w, this.height)
    }
}

fun Color.asDrawable(): Drawable {
    val labelColor = Pixmap(1, 1, Pixmap.Format.RGB888)
    labelColor.setColor(this)
    labelColor.fill()
    return Image(Texture(labelColor)).drawable
}

fun SpriteBatch.beginEndBlock(function: SpriteBatch.() -> Unit) {
    begin()
    function()
    end()
}

fun ShapeRenderer.loadTransformOfBody(body: Body) {
    identity()
    translate(body.position.x, body.position.y, 0f)
    rotate(0f, 0f, 1f, body.angle.toDegree())
}

fun ShapeRenderer.loadTransformData(transformData: TransformData) {
    identity()
    translate(transformData.dx, transformData.dy, 0f)
    rotate(0f, 0f, 1f, transformData.angle)
    scale(transformData.scalex, transformData.scaley, 1f)
}


fun ShapeRenderer.drawPointingTriangle(baseCenter: Vector2, end: Vector2, baseHalfWidth: Float, color: Color) {
    identity()
    Gdx.gl.glLineWidth(Global.settings.defaultLineWidth)
    use(ShapeRenderer.ShapeType.Line) {
        this.color.set(color)
        val diff = (end - baseCenter).nor()
        val perp = vec2(diff.y, -diff.x)
        val a = perp * baseHalfWidth + baseCenter
        val b = perp * (-baseHalfWidth) + baseCenter
        line(a, b)
        line(b, end)
        line(end, a)
    }
}


fun ShapeRenderer.loadIdentityAndDefaults() {
    identity()
    Gdx.gl.glLineWidth(Global.settings.defaultLineWidth)
}

fun ShapeRenderer.drawSquare(center: Vector2, halfSize: Float, angleInDegrees: Float, color: Color) {
    loadIdentityAndDefaults()
    use(ShapeRenderer.ShapeType.Line) {
        this.color.set(color)
        translate(center.x, center.y, 0f)
        rotate(0f, 0f, 1f, angleInDegrees)
        this.rect(-halfSize, -halfSize, 2 * halfSize, 2 * halfSize)
    }
}

private val triangleCoordinateFloats = GdxUtil.vector2sToFloatArray(alof(
        vec2(0f, 1f), vec2(-0.866025401f, -0.5f), vec2(0.86602540f, -0.5f)
))

fun ShapeRenderer.drawTriangle(center: Vector2, radius: Float, angleInDegrees: Float, color: Color) {
    loadIdentityAndDefaults()
    use(ShapeRenderer.ShapeType.Line) {
        this.color.set(color)
        translate(center.x, center.y, 0f)
        rotate(0f, 0f, 1f, angleInDegrees)
        scale(radius, radius, radius)
        polyline(triangleCoordinateFloats)
    }
}

private val heartCoordinateFloats = GdxUtil.vector2sToFloatArray(alof(
        vec2(0f, -0.8f), vec2(1f, 0.2f), vec2(0.4f, 0.8f),
        vec2(0f, 0.4f), vec2(-0.4f, 0.8f), vec2(-1f, 0.2f)
))

fun ShapeRenderer.drawHeart(center: Vector2, halfSize: Float, angleInDegrees: Float, color: Color) {
    loadIdentityAndDefaults()
    use(ShapeRenderer.ShapeType.Line) {
        this.color.set(color)
        translate(center.x, center.y, 0f)
        rotate(0f, 0f, 1f, angleInDegrees)
        scale(halfSize, halfSize, halfSize)
        polyline(heartCoordinateFloats)
    }
}

fun ShapeRenderer.drawSelectionArrow(start: Vector2, end: Vector2,
                                     color: Color, width: Float = 8f) {
    loadIdentityAndDefaults()
    use(ShapeRenderer.ShapeType.Filled) {
        this.color.set(color)
        rectLine(start, end, width)
        circle(position = start, radius = width * 1.2f, segments = Global.settings.defaultCircleSegments)
        circle(position = end, radius = width * 1.2f, segments = Global.settings.defaultCircleSegments)
    }
}

fun ShapeRenderer.drawRectLine(start: Vector2, end: Vector2,
                               color: Color, width: Float) {
    loadIdentityAndDefaults()
    use(ShapeRenderer.ShapeType.Filled) {
        this.color.set(color)
        rectLine(start, end, width)
    }
}

fun ShapeRenderer.drawSimpleLine(start: Vector2, end: Vector2, color: Color) {
    loadIdentityAndDefaults()
    use(ShapeRenderer.ShapeType.Line) {
        this.color.set(color)
        line(start, end)
    }
}

fun ShapeRenderer.drawCircleBoundary(center: Vector2, radius: Float, color: Color) {
    loadIdentityAndDefaults()
    use(ShapeRenderer.ShapeType.Line) {
        this.color.set(color)
        circle(position = center, radius = radius, segments = Global.settings.defaultCircleSegments)
    }
}

fun ShapeRenderer.drawEllipseBoundary(center: Vector2, radiusWidth: Float,
                                      radiusHeight: Float, angle: Float, color: Color) {
    loadIdentityAndDefaults()
    translate(center.x, center.y, 0f)
    rotate(0f, 0f, 1f, angle)
    use(ShapeRenderer.ShapeType.Line) {
        this.color.set(color)
        ellipse(-radiusWidth * 0.5f, -radiusHeight * 0.5f, radiusWidth, radiusHeight, Global.settings.defaultCircleSegments)
    }
}