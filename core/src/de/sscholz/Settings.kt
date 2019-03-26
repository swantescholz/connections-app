package de.sscholz

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.Vector2
import de.sscholz.util.MyDatabase
import de.sscholz.util.f
import kotlinx.serialization.*
import kotlinx.serialization.internal.StringDescriptor


@Serializer(forClass = Color::class)
object ColorSerializer : KSerializer<Color> {

    override val descriptor: SerialDescriptor =
            StringDescriptor.withName("WithCustomDefault")

    override fun serialize(encoder: Encoder, obj: Color) {
        encoder.encodeString("${obj.r} ${obj.g} ${obj.b} ${obj.a}")
    }

    override fun deserialize(decoder: Decoder): Color {
        val parts = decoder.decodeString().split(" ").map { it.f }
        return Color(parts[0]!!, parts[1]!!, parts[2]!!, parts[3]!!)
    }
}

@Serializer(forClass = Vector2::class)
object Vector2Serializer : KSerializer<Vector2> {

    override val descriptor: SerialDescriptor =
            StringDescriptor.withName("WithCustomDefault")

    override fun serialize(encoder: Encoder, obj: Vector2) {
        encoder.encodeString("${obj.x} ${obj.y}")
    }

    override fun deserialize(decoder: Decoder): Vector2 {
        val parts = decoder.decodeString().split(" ").map { it.f }
        return Vector2(parts[0]!!, parts[1]!!)
    }
}

@Serializable
data class Settings( // values should all be overridden in settings.txt

        @Serializable(with = Vector2Serializer::class)
        val gravity: Vector2,

        val defaultCircleSegments: Int,
        val relativeSelectionHalfSize: Float, // relative to screen width
        val maxRelativeViewportWidthVsLevelWidth: Float,
        val relativeCharacterHeight: Float, // relative size of person initial VS its size
        val magicImpulseStrength: Float,
        val defaultFloat2StringDecimalPlaces: Int, // for "%.20f".format(f) without loss of precision
        val defaultLineWidth: Float,
        val thickLineWidth: Float,
        val defaultFriction: Float,
        val defaultDensity: Float,
        val defaultRestitution: Float,
        val defaultViewportWidthInUnits: Float,
        val defaultLevelWidth: Float,
        val defaultLevelHeight: Float,
        val defaultTimeStep: Float,
        val defaultLongClickInterval: Float,
        @Serializable(with = ColorSerializer::class)
        val defaultSelectionColor: Color,
        @Serializable(with = ColorSerializer::class)
        val defaultConnectionColor: Color,
        @Serializable(with = ColorSerializer::class)
        val defaultStaticLevelColor: Color,
        @Serializable(with = ColorSerializer::class)
        val defaultBorderColor: Color,
        @Serializable(with = ColorSerializer::class)
        val defaultButtonBgColor: Color,
        @Serializable(with = ColorSerializer::class)
        val defaultButtonTextColor: Color
) {


    companion object {

        fun reloadFromConfigFile(setGlobalSettingsReference: Boolean = true): Settings {
            val newSettings = myjson.parse(Settings.serializer(),
                    MyDatabase.readLocalFileOrElseInternalFile("settings.txt"))
//            log("Settings loaded:\n${myjson.stringify(serializer(), newSettings)}")
            if (setGlobalSettingsReference) {
                Global.settings = newSettings
            }
            return newSettings
        }
    }
}