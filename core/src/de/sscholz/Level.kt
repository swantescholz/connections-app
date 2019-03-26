package de.sscholz

import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.Body
import com.badlogic.gdx.physics.box2d.BodyDef
import com.badlogic.gdx.scenes.scene2d.Stage
import de.sscholz.Global.settings
import de.sscholz.Global.shapeRenderer
import de.sscholz.Physics.world
import de.sscholz.connections.Connections
import de.sscholz.connections.Type
import de.sscholz.extensions.applyLinearImpulseToCenter
import de.sscholz.extensions.loadTransformData
import de.sscholz.util.*
import ktx.box2d.body
import ktx.graphics.use
import ktx.math.timesAssign
import ktx.math.vec2

class Level(private val levelIndex: Int, private val uiStage: Stage,
            private val isNonGraphicalSimulation: Boolean = false,
            private val simulationDoneCallback: (Boolean) -> Unit) {

    companion object {
        const val cameraAdditionalZoomOutFactor = 1.5f
        const val defaultSimulationTime = 600f
        const val levelPathPrefix = "levels/"
        const val staticPostfix = "-static.txt"
        const val dynamicPostfix = "-dynamic.txt"
        const val goalsPostfix = "-goals.txt"
    }

    private val levelStage = Stage()
    private var hasLocalGoalsData = false
    private lateinit var borderLoop: MyLoop
    var title: String = ""
        private set
    var introText: String = ""
        private set
    var width: Float = settings.defaultLevelWidth
        private set
    var height: Float = settings.defaultLevelHeight
        private set
    var timeUntilGoalSituation: Float = 1f
        private set
    var totalPhysicsTime = 0f
        private set
    private val persons = ArrayList<Person>()
    private val shapeTemplates = ArrayList<ShapeTemplateData>()
    private val shapeInstances = ArrayList<ShapeInstanceData>()
    private var correctConnections: List<ConnectionData>? = null
    private val goals = ArrayList<GoalData>()
    val connections = Connections()
    private val staticBodies = ArrayList<Body>()

    private var accumulatedTime = 0.0f
    private lateinit var originalStaticData: StaticLevelData
    private lateinit var originalDynamicData: DynamicLevelData
    private lateinit var originalGoalsData: GoalLevelData

    init {
        loadLevel(true, null)
    }

    private fun createBorder() {
        val x = 0f
        val y = 0f
        borderLoop = MyLoop(alof(vec2(x, y), vec2(x + width, y),
                vec2(x + width, y + height), vec2(x, y + height)))
        borderLoop.color = settings.defaultBorderColor
        levelStage.addActor(borderLoop)
    }

    // generate DynamicLevelData object for the current state of the level,
    // and saves it (potentially overwriting old input file)
    fun saveCurrentStateAsDynamicData() {
        val dynamicData = createCurrentDynamicData()
        val text = myjson.stringify(DynamicLevelData.serializer(), dynamicData)
        MyDatabase.writeStringToLocalFile("$levelPathPrefix$levelIndex$dynamicPostfix", text, append = false)
        resetLevel() // reset, so that all impulses etc. are nulled at the start of the simulation
    }

    // generate GoalLevelData object for the current state of the level,
    // and saves it (potentially overwriting old goals file)
    fun saveCurrentStateAsGoalsData() {
        val goalLevelData = computeCurentStateAsGoalData()
        val text = myjson.stringify(GoalLevelData.serializer(), goalLevelData)
        MyDatabase.writeStringToLocalFile("$levelPathPrefix$levelIndex$goalsPostfix", text, append = false)
    }

    private fun computeCurentStateAsGoalData(): GoalLevelData {
        val goalDatas = ArrayList<GoalData>()
        val dynamicData = createCurrentDynamicData() // use this as goal data
        dynamicData.persons.forEach {
            goalDatas.add(GoalData(personIndex = it.index,
                    x = it.x, y = it.y, angle = it.angle))
        }
        return GoalLevelData(timeToSimulate = totalPhysicsTime, goals = goalDatas)
    }

    private fun createCurrentDynamicData(): DynamicLevelData {
        val personDatas = ArrayList<PersonData>()
        val connectionDatas = ArrayList<ConnectionData>()
        persons.forEach { personDatas.add(it.createCurrentPersonData()) }
        connections.connections.forEach { connectionDatas.add(it.createConnectionData()) }
        return DynamicLevelData(persons = personDatas, connections = connectionDatas)
    }


    // tries reading locally. if that doesn't work it tried reading internally. if that
    // fails too, just return null
    private fun readLevelConfigFile(filename: String): String? {
        try {
            return MyDatabase.readLocalFileOrElseInternalFile("$levelPathPrefix$filename")
        } catch (e: Exception) {
            return null
        }
    }

    fun updateAndDoPhysicsStep(deltaTime: Float, clampTimeInOrderToAvoidSpiralOfDeath: Boolean = true) {
        var clampedAdditionalTime = deltaTime
        if (clampTimeInOrderToAvoidSpiralOfDeath) {
            clampedAdditionalTime = Math.min(deltaTime, maximumPhysicsStepsPerFrame * settings.defaultTimeStep) // at most 5 iterations per screen frame
        }
        accumulatedTime += clampedAdditionalTime
        while (accumulatedTime >= settings.defaultTimeStep) {
            accumulatedTime -= settings.defaultTimeStep
            totalPhysicsTime += settings.defaultTimeStep
            connections.updateAllConnections(settings.defaultTimeStep)
            world.step(settings.defaultTimeStep, defaultPhysicsVelocityIterations, defaultPhysicsPositionIterations)
            if (totalPhysicsTime >= timeUntilGoalSituation && totalPhysicsTime - settings.defaultTimeStep < timeUntilGoalSituation) {
                checkLevelSolved()
                return
            }
        }
    }

    fun reinitializeEverythingOnNewStart() {
        printl("reinit level()")
        resetLevel()
    }

    private fun checkLevelSolved() {
        val solved = areCurrentConnectionsCorrect()
        simulationDoneCallback(solved)
        var error = 1000000f // if no goals present, assume high error
        if (!goals.isEmpty()) {
            error = computeCurrentStateVsGoalError()
        }
        if (solved) {
            if (error > 0.0f) {
                if (!isNonGraphicalSimulation) {
                    UiUtil.myDialog("Physics Engine Error", "Mismatch of expected final state and actual final state " +
                            "(error=%d mm),".format((error * 1000).toInt()) +
                            " probably due to unexpected behavior of your phone's CPU. Saving current final state as default." +
                            " Sorry for the inconvenience. This should actually not happen, please let me know when it does.",
                            closeOnClickAnywhere = true) { _, dialog ->
                        dialog.remove()
                    }.show(uiStage)
                }
            }
            if ((!editMode && error > 0f) || !hasLocalGoalsData) {
                saveCurrentStateAsGoalsData()
            }
        }
    }

    fun doSimulationAndSaveGoalDataIfNoLocalGoalDataExists() {
        if (hasLocalGoalsData) {
            return // if local data exists, do nothing
        }
        updateAndDoPhysicsStep(timeUntilGoalSituation + 0.1f, false)
    }

    private fun computeCurrentStateVsGoalError(): Float {
        return persons.zip(goals).map { (p, g) -> p.worldCenter.dst(vec2(g.x, g.y)) }.max()!!
    }

    fun areCurrentConnectionsCorrect(): Boolean {
        val currentConnections = connections.connections.map {
            ConnectionData(it.personA.personIndex,
                    it.personB.personIndex, it.type)
        }.map { it.normalized() }.toHashSet()
        return currentConnections == correctConnections!!.map { it.normalized() }.toHashSet()
    }

    fun render() {

        levelStage.draw()
        shapeInstances.forEach { shapeInstance ->
            val template = findShapeTemplateForName(shapeInstance.shape)
            with(shapeRenderer) {
                shapeRenderer.loadTransformData(shapeInstance.transform)
                use(ShapeRenderer.ShapeType.Line) {
                    color.set(settings.defaultStaticLevelColor)
                    polyline(template.vertexFloats)
                }
            }
        }
        connections.renderAllConnections()
        goals.forEach { goal ->
            ignoreException {
                val person = findPersonForIndex(goal.personIndex)
                person.renderFutureShadow(TransformData(goal.x, goal.y, goal.angle))
            }
        }
        persons.forEach { it.render() }
    }

    fun destroy() {
        connections.destroyAll()
        borderLoop.destroy()
        persons.forEach { it.destroy() }
        persons.clear()
        shapeTemplates.clear()
        shapeInstances.clear()
        goals.clear()
        staticBodies.forEach { world.destroyBody(it) }
        staticBodies.clear()
        Physics.resetWorld() // needed for more deterministic behavior?
    }

    fun applyImpulseToAllPersons(impulse: Vector2) {
        persons.forEach {
            it.body.applyLinearImpulseToCenter(impulse)
        }
    }

    fun addPerson(person: Person, mustHaveUniqueIndex: Boolean = false) {
        if (mustHaveUniqueIndex && persons.any { it.personIndex == person.personIndex }) {
            throw RuntimeException("cannot add person, as index ${person.personIndex} is already taken")
        }
        persons.add(person)
    }

    // shows dialog on failure
    fun tryAddingNewConnection(connectionType: Type, personA: Person, personB: Person) {
        if (!connections.hasIdenticalConnectionOfType(connectionType, personA, personB)) {
            connections.addConnection(connectionType, personA, personB)
            Sounds.kick.play()
        } else {
            Sounds.error.play()
            UiUtil.myDialog("Connection not possible",
                    "The selected persons already have that connection.",
                    closeOnClickAnywhere = true) { eventObject, dialog ->
                dialog.remove()
            }.show(uiStage)
        }
    }

    fun resetLevel() {
        printl("reset")
        val originalPersonIndexSet = originalDynamicData.persons.map { it.index }.toHashSet()
        // only keep the connections that are possible (i.e. the persons originally exist)
        val validCurrentConnectionData = connections.connectionsInUserOrder.filter {
            it.personA.personIndex in originalPersonIndexSet && it.personB.personIndex in originalPersonIndexSet
        }.map { ConnectionData(it.personA.personIndex, it.personB.personIndex, it.type) }
        destroy()
        loadLevel(false, validCurrentConnectionData)
    }

    private fun loadLevel(moveCameraToCenter: Boolean, currentUserConnectionData: List<ConnectionData>?) {
        Preferences.lastPlayedLevelId.set(levelIndex)
        totalPhysicsTime = 0.0f
        accumulatedTime = 0.0f
        val staticTemplateText = MyDatabase.readLocalFileOrElseInternalFile("${levelPathPrefix}static-template.txt")
        fun config(postfix: String) = readLevelConfigFile("$levelIndex$postfix")
        try {
            originalStaticData = ("{" + staticTemplateText + config(staticPostfix)!! + "}").let {
                myjson.parse(StaticLevelData.serializer(), it)
            }
        } catch (e: Exception) {
            log("could not load static level data: $e")
        }
        originalDynamicData = exceptionToNull {
            config(dynamicPostfix)?.let { myjson.parse(DynamicLevelData.serializer(), it) }
        } ?: DynamicLevelData()
        var goalsDataText: String
        try {
            // we only want to read local, not the internal data
            goalsDataText = MyDatabase.readLocalFile("$levelPathPrefix$levelIndex$goalsPostfix")
            hasLocalGoalsData = true
        } catch (e: Exception) {
            log("Couldn't find local goal data: $e")
            log("Reading internal goal data instead")
            try {
                goalsDataText = MyDatabase.readInternalReadOnlyFile("$levelPathPrefix$levelIndex$goalsPostfix")
            } catch (e: Exception) {
                log("Couldn't find internal goal data either: $e")
                log("creating empty goal data instead")
                goalsDataText = "{timeToSimulate: 10, goals: []}"
            }
        }
        originalGoalsData = myjson.parse(GoalLevelData.serializer(), goalsDataText)
        if (originalDynamicData == DynamicLevelData()) {
            originalGoalsData = originalGoalsData.clearEverythingExceptTime() // discard goal data, if dynamic data is gone
        }
        correctConnections = originalDynamicData.connections
        loadLevelData(originalStaticData, originalDynamicData, originalGoalsData, currentUserConnectionData)

        createBorder()
        if (moveCameraToCenter && persons.size > 0 && !isNonGraphicalSimulation) {
            val aabbBox = GdxUtil.computeAabbBoxForVectors(
                    persons.asSequence().map { it.worldCenter } +
                            goals.asSequence().map { it.center })
            aabbBox.halfSize *= cameraAdditionalZoomOutFactor
            val newHalfViewportWidth = Math.max(aabbBox.halfSize.x, aabbBox.halfSize.y / camera.heightToWidthRatio)
            camera.setNewViewportWorldWidth(aabbBox.center, 2f * Math.max(minViewportWidth, newHalfViewportWidth))
        }
    }

    private fun findPersonForIndex(personIndex: Int): Person {
        val person = persons.find { it.personIndex == personIndex }
        myAssert(person != null) { "cannot find person with index $personIndex" }
        return person!!
    }

    private fun findShapeTemplateForName(shapeTemplateName: String): ShapeTemplateData {
        val template = shapeTemplates.find { it.name == shapeTemplateName }
        myAssert(template != null) { "cannot find shapeTemplate with name $shapeTemplateName" }
        return template!!
    }

    fun randomAtPosition(position: Vector2): PersonData {
        myAssert(persons.size < Person.maxNumberOfPersons) {
            "cannot create another random person, there are already the maximum number possible"
        }
        val personIndex = (0 until Person.maxNumberOfPersons).filter { i -> !persons.any { it.personIndex == i } }.first()
        val corners = listOf(0, 3, 4, 5, 6, 7, 8).random()!!
        val ego = (0f..1f).random()
        val confidence = (0f..1f).random()
        return PersonData(personIndex, position.x, position.y, corners, 0f, ego, confidence)
    }

    private fun loadLevelData(staticData: StaticLevelData, dynamicData: DynamicLevelData,
                              goalsData: GoalLevelData, currentUserConnectionData: List<ConnectionData>?) {
        width = staticData.width
        height = staticData.height
        title = staticData.title
        introText = staticData.introText
        staticData.shapes.forEach { shapeTemplates.add(it) }
        staticData.instances.forEach { instance ->
            shapeInstances.add(instance)
            val template = findShapeTemplateForName(instance.shape)
            staticBodies.add(world.body {
                val transformedVertices = template.vertexList.map { instance.transform.transformPoint(it) }
                loop(GdxUtil.vector2sToFloatArray(transformedVertices, closeLoop = false)) {
                    density = settings.defaultDensity
                    friction = settings.defaultFriction
                    restitution = settings.defaultRestitution
                }
                type = BodyDef.BodyType.StaticBody
            })
        }
        dynamicData.persons.forEach { personData ->
            myAssert(!persons.any { it.personIndex == personData.index }) { "person with index ${personData.index} already exists!" }
            persons.add(Person(personData = personData))
        }
        if (editMode || isNonGraphicalSimulation) {
            addConnectionDatasInSortedOrder(dynamicData.connections)
        } else if (currentUserConnectionData != null) { // in release mode: remember and reset the user connections
            addConnectionDatasInSortedOrder(currentUserConnectionData)
        }
        timeUntilGoalSituation = goalsData.timeToSimulate
        goalsData.goals.forEach { goals.add(it) }
    }

    // before play, all connections should be loaded via this.
    // otherwise non-deterministic behavior might occur
    fun addConnectionDatasInSortedOrder(connectionDatas: List<ConnectionData>) {
        connections.addConnectionsInSortedOrder(connectionDatas.map {
            val a = findPersonForIndex(it.from)
            val b = findPersonForIndex(it.to)
            Triple(it.type, a, b)
        })
    }

    fun removePerson(existingPerson: Person) {
        myAssert(existingPerson in persons)
        connections.destroyAllConnectionsWithPerson(existingPerson)
        persons.remove(existingPerson)
        existingPerson.destroy()
    }

    fun removeAllConnections() {
        connections.destroyAll()
    }

    fun removeAllPersons() {
        connections.destroyAll()
        persons.forEach { it.destroy() }
        persons.clear()
        goals.clear()
    }

    // a new connection can only be added in editMode, or at the start of the simulation
    fun isAddingNewConnectionsAllowed(): Boolean {
        return editMode || totalPhysicsTime == 0f
    }

    fun getAllOringinallyUsedConnectionTypes(): Array<Type> {
        val res = LinkedHashSet<Type>()
        originalDynamicData.connections.forEach {
            res.add(it.type)
        }
        return res.toTypedArray()
    }

    fun isAtBeginning(): Boolean {
        return totalPhysicsTime == 0.0f
    }


}