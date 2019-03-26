package de.sscholz

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.EarClippingTriangulator
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.Body
import com.badlogic.gdx.physics.box2d.BodyDef
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.utils.ShortArray
import de.sscholz.Global.defaultSpriteBatch
import de.sscholz.Global.settings
import de.sscholz.Physics.world
import de.sscholz.extensions.*
import de.sscholz.util.*
import ktx.box2d.body
import ktx.graphics.use
import ktx.math.vec2
import kotlin.math.roundToInt


// represents a person with character traits visualized as physical properties (e.g. ego->size)
// character traits are normalized to [0,1], and physical properties will be computed from
// those appropriately
// person index determines shape/color/name, must be < 12
class Person(personData: PersonData) {

    companion object {
        const val maxNumberOfPersons = 26
        val sizeBounds = 1.5f..4f
        val densityBounds = 1f..5f
        val frictionBounds = 0.1f..0.8f
        val restitutionBounds = 0.0f..0.5f
        const val defaultEgo = 0.3f
        const val defaultReliability: Float = 0.3f
        const val defaultEmpathy: Float = 0.5f
        const val fontSize: Int = 42
        const val shadowAlpha = 0.3f
        const val defaultAlpha = 1.0f
        private val personColorList = listOf(
                Color.RED, Color.CYAN, Color.ORANGE, Color.SALMON,
                Color.BLUE, Color.GRAY, Color.SLATE, Color.PURPLE,
                Color.BROWN, Color.CORAL, Color.FIREBRICK, Color.FOREST)
    }

    val personIndex: Int = personData.index
    val numberOfCorners: Int = personData.corners
    private val startPosition = vec2(personData.x, personData.y)
    val ego: Float = personData.ego
    val reliability: Float = personData.reliability
    val empathy: Float = personData.empathy
    private val initialRotationInDegrees = personData.angle
    val size = sizeBounds.interpolate(ego)
    private val density = densityBounds.interpolate(reliability)
    private val friction = frictionBounds.interpolate(reliability)
    private val restitution = restitutionBounds.interpolate(1 - reliability)
    val shortName = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"[personIndex].toString()
    val longName = listOf("Alice", "Bob", "Claire", "David", "Eve", "Felix",
            "Grace", "Henry", "Ivy", "Jack", "Kevin", "Lilly",
            "Mia", "Noelle", "Oliver", "Peter", "Quentin", "Rita", "Sarah",
            "Tim", "Umar", "Violet", "Walda", "Xavier", "Yaqub", "Zac")[personIndex]

    private val originalColor: Color = personColorList[personIndex % personColorList.size]
    var color: Color = originalColor
    private val nameTextureNormal = FontFactory.Bold.renderToTexture(shortName, fontSize)
    private val nameTextureShadow = FontFactory.Bold.renderToTexture(shortName, fontSize)
    private val corners = ArrayList<Vector2>()
    val body: Body
    private val cornerFloats: FloatArray
    private val triangleIndices: ShortArray
    var isSelected: Boolean = false
    val worldCenter: Vector2
        get() = body.worldCenter

    init {
        if (personIndex !in 0..maxNumberOfPersons - 1) {
            throw RuntimeException("too large/small personIndex $personIndex")
        }
        val radius = size
        corners.addAll(when (numberOfCorners) {
            0 -> GdxUtil.createRegularPolygonCorners(settings.defaultCircleSegments, radius)
            in 3..8 -> GdxUtil.createRegularPolygonCorners(numberOfCorners, radius)
            else -> throw RuntimeException("invalid number of corners $numberOfCorners, must be zero or in 3..8")
        })
        body = world.body {
            type = BodyDef.BodyType.DynamicBody
            when (numberOfCorners) {
                0 -> circle(radius)
                else -> polygon(*corners.map { it }.toTypedArray())
            }.apply {
                density = this@Person.density
                friction = this@Person.friction
                restitution = this@Person.restitution
            }
            userData = this@Person
        }
        body.setTransform(startPosition, initialRotationInDegrees.toRadian())
        body.filterCategoryBits = 0b0000000000000010
        body.filterMaskBits = 0b0111111111111111
        cornerFloats = GdxUtil.vector2sToFloatArray(corners, true)
        triangleIndices = EarClippingTriangulator().computeTriangles(cornerFloats)
    }

    fun moveTo(newPosition: Vector2) {
        body.moveTo(newPosition)
    }

    fun renderFutureShadow(transformData: TransformData) {
        draw(transformData, true)
    }

    fun render() {
        draw(body.transformData(), false)
    }

    // no scaling transform allowed
    private fun draw(transformData: TransformData, isShadow: Boolean) {
        val alphaFactor: Float = if (isShadow) shadowAlpha else defaultAlpha
        GdxUtil.withGlBlendingEnabled {
            with(Global.shapeRenderer) {
                loadTransformOfBody(body)
                this.loadTransformData(transformData)

                color = this@Person.color
                color.a = reliability * alphaFactor
                drawFilledPolygon()
                color.a = 1f * alphaFactor
                Gdx.gl.glLineWidth(settings.defaultLineWidth)
                drawPolyline()
                if (isSelected && !isShadow) {
                    color = settings.defaultSelectionColor
                    scale(1.1f, 1.1f, 1.0f)
                    Gdx.gl.glLineWidth(settings.thickLineWidth)
                    drawPolyline()
                }
                Gdx.gl.glLineWidth(settings.defaultLineWidth)

                defaultSpriteBatch.beginEndBlock {
                    this.projectionMatrix = camera.combinedMatrix
                    val height = size * settings.relativeCharacterHeight
                    val texture = if (isShadow) nameTextureShadow else nameTextureNormal
                    val width = height / texture.regionHeight * texture.regionWidth.toFloat()
                    draw(texture, transformData.dx - width / 2f, transformData.dy - height / 2f,
                            width / 2f, height / 2f,
                            width, height, 1f, 1f, transformData.angle)
                }
            }
        }
    }

    private fun ShapeRenderer.drawPolyline() {
        use(ShapeRenderer.ShapeType.Line) {
            polyline(cornerFloats)
        }
    }

    private fun ShapeRenderer.drawFilledPolygon() {
        use(ShapeRenderer.ShapeType.Filled) {

            var i = 0
            while (i < triangleIndices.size - 2) {
                val x1 = cornerFloats[triangleIndices.get(i) * 2]
                val y1 = cornerFloats[triangleIndices.get(i) * 2 + 1]

                val x2 = cornerFloats[triangleIndices.get(i + 1) * 2]
                val y2 = cornerFloats[triangleIndices.get(i + 1) * 2 + 1]

                val x3 = cornerFloats[triangleIndices.get(i + 2) * 2]
                val y3 = cornerFloats[triangleIndices.get(i + 2) * 2 + 1]

                this.triangle(x1, y1, x2, y2, x3, y3)
                i += 3
            }
        }
    }

    fun destroy() {
        world.destroyBody(body)
    }

    fun showNormalInfoDialog(uiStage: Stage) {
        val text = """
            This is $longName:

            Ego${traitPercent(ego)}
            Reliability${traitPercent(reliability)}
            Empathy${traitPercent(empathy)}

            In order to connect $longName to another person,
            tap on her and draw a line to that other person.

            Your goal is to make all persons reach their
            desired final destinations indicated in light
            outlines, influenced by the connections you create.
        """.trimIndent()
        UiUtil.myDialog("Traits of $longName", text, closeOnClickAnywhere = true) { _, dialog ->
            dialog.remove()
        }.show(uiStage)
    }

    private fun traitPercent(unitFloat: Float): String {
        return ": ${(100f * unitFloat).roundToInt()}%"
    }

    fun createCurrentPersonData(): PersonData {
        return PersonData(index = personIndex, corners = numberOfCorners,
                x = worldCenter.x, y = worldCenter.y, angle = body.angle.toDegree(),
                ego = ego, empathy = empathy, reliability = reliability
        )
    }


}