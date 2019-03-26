package de.sscholz.util

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import de.sscholz.Global.settings
import java.util.*

val numberOfLevels = 20
val maximumPhysicsStepsPerFrame = 50
val minViewportWidth = 10f
val defaultMaximumForceStrength = 1000f
val defaultLinkHalfThickness = 0.3f
val defaultPhysicsVelocityIterations = 6
val defaultPhysicsPositionIterations = 3
val disabledButtonColor = Color(0.1f, 0.1f, 0.1f, 0.5f)
val hudTopButtonHeight = 110f
val bottomUiHeight = 90f
val screenWidth by lazy { Gdx.graphics.width.toFloat() }
val screenHeight by lazy { Gdx.graphics.height.toFloat() }
val dialogButtonHeight by lazy { screenHeight / 15 }
val dialogButtonTopPadding by lazy { screenHeight / 30 }
val dialogButtonMinWidth by lazy { screenHeight / 5 }
val dialogPaddingNormal by lazy { screenHeight / 30 }
val dialogPaddingTop by lazy { 2 * dialogPaddingNormal }
val uiLabelBorderPadding by lazy { screenHeight / 200 }
val textButtonInnerPadding by lazy { screenHeight / 100 }
val defaultLocale by lazy { Locale.ENGLISH }
val selectBoxListExtraTopBottomHeight by lazy { 0.014f * screenHeight }
val howToPlayScrollPaneHeight by lazy { 0.7f * screenHeight }
val hudStatusBarHeight by lazy { screenHeight * 0.025f }
val hudTopTotalHeight = hudTopButtonHeight + hudStatusBarHeight
val maxRelativeDialogWidth = 0.8f
val selectionHalfSize: Float
    get() = camera.viewportWidth * settings.relativeSelectionHalfSize