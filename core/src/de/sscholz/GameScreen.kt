package de.sscholz

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.InputMultiplexer
import com.badlogic.gdx.input.GestureDetector
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.Touchable
import com.badlogic.gdx.scenes.scene2d.ui.Cell
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener
import com.badlogic.gdx.utils.Align
import de.sscholz.Global.settings
import de.sscholz.Global.shapeRenderer
import de.sscholz.Physics.world
import de.sscholz.connections.Type
import de.sscholz.extensions.asGestureDetector
import de.sscholz.extensions.drawSelectionArrow
import de.sscholz.extensions.printDebugInfo
import de.sscholz.util.*
import de.sscholz.util.UiUtil.myDialog
import ktx.actors.onChangeEvent
import ktx.actors.onClick
import ktx.app.KtxInputAdapter
import ktx.app.KtxScreen
import ktx.math.vec2
import ktx.scene2d.*
import ktx.scene2d.Scene2DSkin.defaultSkin
import kotlin.system.measureNanoTime


class GameScreen(val application: App) : KtxScreen {

    companion object {
        var levelIndexToLoad = if (editMode) Preferences.lastPlayedLevelId.get() else 1
    }

    private lateinit var level: Level
    private val mainStage = Stage()
    private val uiStage = Stage()
    private lateinit var cameraInputHandler: CameraInputHandler
    private lateinit var titleBarLabel: Label
    private lateinit var timerLabel: Label
    private lateinit var fpsLabel: Label

    private var isPaused = true

    private var clickedPersonA: Person? = null
    private var hoveredPersonB: Person? = null

    private lateinit var currentConnectionType: Type
    private lateinit var deletionSelectBox: SelectBox<String>

    private lateinit var speedButton: KTextButton
    private lateinit var playButton: KButton
    private lateinit var resetButton: KButton
    private var isInLongPress = false

    private lateinit var playButtonImage: Image
    private val playButtonImageNames = listOf("pause", "play")

    private val veryFastFactor = if (editMode) 30 else 10
    private val speedTexts = alof("1x", "3x", "${veryFastFactor}x", "0.3x")
    private val speedFactors = alof(1f, 3f, veryFastFactor.toFloat(), 0.3f)
    private var physicsStepSpeedFactor: Float = speedFactors[0]


    private lateinit var availableConncetionTypesForLevel: Array<Type>
    private lateinit var connectionSelectBox: KSelectBox<String>

    private val viewTop = table {
        setFillParent(true)
        align(Align.topLeft)
        touchable = Touchable.enabled
        pad(0f)
        titleBarLabel = label("", style = "withbg") {
            this.setAlignment(Align.center)
        }.cell(row = true, growX = true, height = hudStatusBarHeight)
        table {
            playButton = button {
                color = settings.defaultButtonBgColor
                playButtonImage = image(playButtonImageNames[isPaused.i])
                onClick {
                    togglePlayPause()
                }
            }.cell(width = hudTopButtonHeight, height = hudTopButtonHeight, align = Align.left)
            speedButton = textButton(speedTexts[0], style = "large") {
                color = settings.defaultButtonBgColor
                onClick { toggleSpeedFactor() }
            }.cell(width = hudTopButtonHeight, height = hudTopButtonHeight)
            resetButton = button {
                color = settings.defaultButtonBgColor
                image("reset")
                onClick {
                    log("reset")
                    if (level.isAtBeginning()) {
                        Toasts.showToast("Nothing to be done.\nThe simulation is already at the beginning.")
                    } else {
                        this@GameScreen.reset()
                    }
                }
            }.cell(width = hudTopButtonHeight, height = hudTopButtonHeight)
            textButton("UNDO", style = "large") {
                color = settings.defaultButtonBgColor
                pad(textButtonInnerPadding)
                onClick {
                    if (!editMode && !level.isAtBeginning()) {
                        Toasts.showToast("Undo not possible while simulation is ongoing. You have to reset the simulation first.")
                    } else {
                        log("undo")
                        level.connections.tryToRemoveLastConnectionUserAdded()
                        updateDeletionSelectBox()
                    }
                }
            }.cell(height = hudTopButtonHeight)
            availableConncetionTypesForLevel = Type.values()
            connectionSelectBox = selectBox<String, Cell<*>> {
                onChangeEvent { _: ChangeListener.ChangeEvent, actor: KSelectBox<String> ->
                    if (actor.selectedIndex >= 0) {
                        currentConnectionType = availableConncetionTypesForLevel[actor.selectedIndex]
                    }
                }
            }.cell(/*height = hudTopButtonHeight*/ grow = true, row = true)
        }.cell(align = Align.left, row = true, growX = true)
    }

    fun toggleSpeedFactor() {
        val newIndex = (1 + speedTexts.indexOf(speedButton.text.toString())) % speedTexts.size
        physicsStepSpeedFactor = speedFactors[newIndex]
        speedButton.setText(speedTexts[newIndex])
    }

    private fun togglePlayPause() {
        isPaused = !isPaused
        updatePlayButtonImage()
        if (!isPaused && level.isAtBeginning()) {
            level.reinitializeEverythingOnNewStart()
        }
    }

    private val viewBottomLeft = table {
        setFillParent(true)
        align(Align.bottomLeft)
        table {
            pad(uiLabelBorderPadding)
            timerLabel = label("Time: ") {
            }.cell(width = 230f)
            fpsLabel = label("FPS: ") {
            }.cell(width = 130f)
        }.cell()
    }
    private val viewBottomRight = table {
        setFillParent(true)
        align(Align.bottomRight)
        table {
            deletionSelectBox = selectBox<String, Cell<*>> {
                onChangeEvent { _: ChangeListener.ChangeEvent, actor: KSelectBox<String> ->
                    when {
                        actor.selectedIndex < 0 -> return@onChangeEvent
                        actor.selectedIndex == 1 -> level.removeAllConnections()
                        actor.selectedIndex == this.items.size - 1 && editMode -> level.removeAllPersons()
                        actor.selectedIndex > 1 -> {
                            val connectionIndex = actor.selectedIndex - 2
                            level.connections.removeConnectionAtIndex(connectionIndex)
                        }
                        else -> return@onChangeEvent

                    }
                    updateDeletionSelectBox()
                }
            }.cell(height = bottomUiHeight)
        }.cell()
    }

    init {
        printl("windows size: $screenWidth x $screenHeight")
        fooFun()
        Gdx.input.isCatchBackKey = true // make sure we cleanly quit GDX when user presses "back"
        GdxUtil.setupOpenGl()
        camera.initCamera(settings.defaultViewportWidthInUnits)
        Sounds.load() // so that sounds are loaded already and first time works immediately
        if (editMode) {
            toggleSpeedFactor()
            toggleSpeedFactor()
        }
    }

    private fun updatePlayButtonImage() {
        playButtonImage.setDrawable(defaultSkin, playButtonImageNames[isPaused.i])
    }

    private fun updateLevelTitleBarLabel() {
        var text = "Level $levelIndexToLoad: ${level.title}"
        if (editMode) {
            text += " (Edit Mode)"
        }
        titleBarLabel.setText(text)
    }

    override fun show() {
        uiStage.addActor(viewTop)
        uiStage.addActor(viewBottomLeft)
        uiStage.addActor(viewBottomRight)
        level = Level(levelIndexToLoad, uiStage) { levelSolved ->
            if (!isPaused) {
                if (levelSolved) {
                    if (levelIndexToLoad == Preferences.unlockedLevels.get()) {
                        Preferences.unlockedLevels.set(levelIndexToLoad + 1)
                    }
                    Toasts.showToast("=== CORRECT ===")
                    if (!editMode) {
                        showLevelDoneDialog()
                    }
                } else {
                    Toasts.showToast("<<< Incorrect, try again >>>")
                }
                togglePlayPause()
            }
        }
        cameraInputHandler = CameraInputHandler(level)
        val inputMultiplexer = InputMultiplexer()
        inputMultiplexer.addProcessor(uiStage)
        inputMultiplexer.addProcessor(cameraInputHandler.asGestureDetector())
        inputMultiplexer.addProcessor(cameraInputHandler) // for scrolling events
        inputMultiplexer.addProcessor(inputProcessor.asGestureDetector())
        inputMultiplexer.addProcessor(inputProcessor)
        Gdx.input.inputProcessor = inputMultiplexer
        reset()
        if (!editMode) {
            showIntroDialog()
        }
    }


    private var personMovedByMouse: Person? = null

    // updates after physics step, but before rendering
    private fun myUpdate(delta: Float) {
        mainStage.act(delta)
        uiStage.act(delta)
        if (!isPaused) {
            val timeToSimulate = delta * physicsStepSpeedFactor
            level.updateAndDoPhysicsStep(timeToSimulate)
        }
        val worldXy = GdxUtil.getCurrentWorldCoordinatesOfMouse()
        personMovedByMouse?.moveTo(worldXy)
        if (clickedPersonA != null) {
            hoveredPersonB?.isSelected = false
            hoveredPersonB = GdxUtil.aabbQueryPerson(worldXy, selectionHalfSize)
            if (hoveredPersonB == clickedPersonA) {
                hoveredPersonB = null
            } else {
                hoveredPersonB?.isSelected = true
            }
        }
    }

    override fun render(delta: Float) {
        camera.apply()
        GdxUtil.framerateComputer.addDeltaTimeOfCurrentFrame(delta)
        doRegularly("set timer label to new values", 0.1, runFunctionOnFirstCall = true) {
            timerLabel.setText("Time: %.1f/%.1f".format(defaultLocale,
                    level.totalPhysicsTime, level.timeUntilGoalSituation))
        }
        doRegularly("set fps label to new values", 1.0, runFunctionOnFirstCall = false) {
            fpsLabel.setText("FPS: %.1f".format(defaultLocale,
                    GdxUtil.framerateComputer.computeFps()))
        }
        val updateSpeed = measureNanoTime {
            myUpdate(delta)
        }
        val renderSpeed = measureNanoTime {
            level.render()
            if (clickedPersonA != null) {
                shapeRenderer.drawSelectionArrow(clickedPersonA!!.worldCenter, GdxUtil.getCurrentWorldCoordinatesOfMouse(),
                        settings.defaultSelectionColor, width = camera.viewportWidth * 0.009f)
            }
            mainStage.draw()
            hudViewport.apply()
            uiStage.draw()
            Toasts.render()
        }
        doRegularly("render speed", 15.0, false) {
            log("update vs render time:",
                    updateSpeed / 1000f / 1000, renderSpeed / 1000f / 1000,
                    1f * updateSpeed / renderSpeed)
        }
    }

    private fun reset() {
        Settings.reloadFromConfigFile()
        level.resetLevel()
        isInLongPress = false
        isPaused = true
        updatePlayButtonImage()
        clickedPersonA = null
        hoveredPersonB = null
        updateDeletionSelectBox()
        updateLevelTitleBarLabel()
        updateConnectionSelectBox()
    }

    private fun updateConnectionSelectBox() {
        availableConncetionTypesForLevel = Type.values()
        if (!editMode) {
            availableConncetionTypesForLevel = level.getAllOringinallyUsedConnectionTypes()
        }
        if (availableConncetionTypesForLevel.isEmpty()) {
            availableConncetionTypesForLevel = arrayOf(Type.Codependency)
        }
        val items = availableConncetionTypesForLevel.map { it.toString() }.toTypedArray()
        val oldSelectedIndex = connectionSelectBox.selectedIndex
        connectionSelectBox.clearItems()
        connectionSelectBox.setItems(*items)
        if (oldSelectedIndex !in 0 until connectionSelectBox.items.size) {
            connectionSelectBox.selectedIndex = 0
        } else {
            connectionSelectBox.selectedIndex = oldSelectedIndex
        }
    }

    private val inputProcessor = object : GestureDetector.GestureAdapter(), KtxInputAdapter {


        override fun keyDown(keycode: Int): Boolean {
            val worldXy = camera.currentMouseWorldCoordinates()
            when (keycode) {
                Input.Keys.ESCAPE -> {
                    printl("====================EXIT (GDX) =================")
                    quit()
                }
                Input.Keys.BACK -> {
                    application.setScreen<MainMenuScreen>()
                }
                Input.Keys.A -> level.applyImpulseToAllPersons(vec2(-settings.magicImpulseStrength, 0f))
                Input.Keys.D -> level.applyImpulseToAllPersons(vec2(settings.magicImpulseStrength, 0f))
                Input.Keys.W -> level.applyImpulseToAllPersons(vec2(0f, settings.magicImpulseStrength))
                Input.Keys.S -> level.applyImpulseToAllPersons(vec2(0f, -settings.magicImpulseStrength))
                Input.Keys.E -> togglePlayPause()
                Input.Keys.Q -> toggleSpeedFactor()
                Input.Keys.R -> reset()
                Input.Keys.I -> {
                    level.saveCurrentStateAsDynamicData()
                    Toasts.showToast("Dynamical elements for level '$levelIndexToLoad' saved.")
                }
                Input.Keys.O -> {
                    level.saveCurrentStateAsGoalsData()
                    Toasts.showToast("Goals for level '$levelIndexToLoad' saved.")
                }
                Input.Keys.F -> world.printDebugInfo(level)
                Input.Keys.M -> {
                    if (editMode) {
                        if (personMovedByMouse == null) {
                            personMovedByMouse = GdxUtil.aabbQueryPerson(worldXy, selectionHalfSize)
                        } else {
                            personMovedByMouse = null
                        }
                    }
                }
                Input.Keys.TAB -> {
                    editMode = !editMode
                    updateLevelTitleBarLabel()
                    updateConnectionSelectBox()
                }
                Input.Keys.T -> {
                    application.setScreen<MainMenuScreen>()
                }
            }
            return true
        }

        override fun longPress(x: Float, y: Float): Boolean {
            isInLongPress = true
            val worldXy = camera.currentMouseWorldCoordinates()
            val existingPerson = GdxUtil.aabbQueryPerson(worldXy, selectionHalfSize)
            if (!editMode) {
                clickedPersonA?.showNormalInfoDialog(uiStage)
                clickedPersonA?.isSelected = false
                clickedPersonA = null
                return true
            } else if (existingPerson != null && editMode && Gdx.input.isButtonPressed(Input.Buttons.RIGHT)) {
                level.removePerson(existingPerson)
                updateDeletionSelectBox()
                return true
            }
            return false
        }

        override fun touchDown(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
            val worldXy = camera.screenToWorldCoordinates(screenX.toFloat(), screenY.toFloat())

            when (button) {
                Input.Buttons.LEFT, Input.Buttons.MIDDLE -> {
                    val existingPerson = GdxUtil.aabbQueryPerson(worldXy, selectionHalfSize)
                    if (existingPerson != null && !cameraInputHandler.isPanning) {
                        clickedPersonA = existingPerson
                        clickedPersonA?.isSelected = true
                        cameraInputHandler.freezePanningUntilTouchUp()
                    }
                }
                Input.Buttons.RIGHT -> {
                    val existingPerson = GdxUtil.aabbQueryPerson(worldXy, selectionHalfSize / 4f)
                    if (editMode && clickedPersonA == null && existingPerson == null) {
                        ignoreException {
                            val person = Person(level.randomAtPosition(worldXy))
                            level.addPerson(person)
                        }
                        return true
                    }
                }

            }
            return false
        }

        override fun touchUp(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
            when (button) {
                Input.Buttons.LEFT, Input.Buttons.MIDDLE -> {
                    if (clickedPersonA != null && hoveredPersonB != null) {
                        if (level.isAddingNewConnectionsAllowed()) {
                            level.tryAddingNewConnection(currentConnectionType, clickedPersonA!!, hoveredPersonB!!)
                            updateDeletionSelectBox()
                        } else {
                            Toasts.showToast("Cannot add connections while simulation is ongoing.\nYou have to" +
                                    " reset the simulation first.", Toasts.Duration.LONG)
                        }
                    }
                    clickedPersonA?.isSelected = false
                    clickedPersonA = null
                    hoveredPersonB?.isSelected = false
                    hoveredPersonB = null
                }
            }
            isInLongPress = false
            return false
        }

    }

    private fun updateDeletionSelectBox() {
        deletionSelectBox.clearItems()
        val items = alof("[Delete Connection]", "Delete All Connections")
        if (::level.isInitialized) {
            level.connections.connections.forEach {
                items.add(it.toNiceString())
            }
        }
        if (editMode) {
            items.add("==Delete All Persons==")
        }
        deletionSelectBox.setItems(*items.toTypedArray())
    }

    private fun showIntroDialog() {
        myDialog("Welcome to level $levelIndexToLoad: ${level.title}",
                level.introText) { eventObject, dialog ->
            dialog.remove()
        }.apply {
            button("Start", 1)
        }.show(uiStage)
    }

    private fun showLevelDoneDialog() {
        Sounds.solved.play()
        var title = "Level Solved!"
        var message = "Level solved, congratulations! Next level unlocked."
        if (levelIndexToLoad == numberOfLevels) {
            title = "Game Completed!"
            message = "Congratulations! You have finished the game! I hope you enjoyed it :)"
        }
        myDialog(title, message, closeOnClickAnywhere = true) { eventObject, dialog ->
            dialog.remove()
            when (eventObject) {
                1 -> {
                    this@GameScreen.hide()
                    application.setScreen<MainMenuScreen>()
                }
                2 -> {
                    GameScreen.levelIndexToLoad++
                    this@GameScreen.hide()
                    application.setScreen<GameScreen>()
                }
            }
        }.apply {
            button("Main Menu", 1)
            if (levelIndexToLoad < numberOfLevels) {
                button("Next Level", 2)
            }
        }.show(uiStage)
    }

    override fun hide() {
        viewTop.remove()
        viewBottomLeft.remove()
        viewBottomRight.remove()
    }

    override fun resize(width: Int, height: Int) {

    }


}

