package de.sscholz.connections

import de.sscholz.Person
import de.sscholz.util.Toasts
import de.sscholz.util.removeLast


class Connections {

    val connections = ArrayList<Connection>()
    val connectionsInUserOrder = ArrayList<Connection>()

    // be careful to always add your connections in the same order,
    // otherwise the behavior might become non-deterministic
    fun addConnection(type: Type, personA: Person, personB: Person) {
        val connection = createConnection(personA, personB, type)
        connections.add(connection)
        connectionsInUserOrder.add(connection)
        connections.sortWith(compareBy({ it.personA.personIndex }, { it.personB.personIndex }))
    }

    private fun createConnection(personA: Person, personB: Person, type: Type): Connection {
        var a = personA
        var b = personB
        if (type.isSymmetric && personA.personIndex > personB.personIndex) { // normalize
            b = personA
            a = personB
        }
        val connection = when (type) {
            Type.Family -> FamilyConnection(a, b)
            Type.Work -> WorkConnection(a, b)
            Type.Hug -> HugConnection(a, b)
            Type.Resentment -> ResentmentConnection(a, b)
            Type.Crush -> CrushConnection(a, b)
            Type.Love -> LoveConnection(a, b)
            Type.Codependency -> CodependencyConnection(a, b)
            Type.Friendship -> FriendshipConnection(a, b)
            Type.Abuse -> AbuseConnection(a, b)
        }
        return connection
    }

    fun removeConnectionAtIndex(connectionIndex: Int) {
        connections[connectionIndex].destroy()
        val removedConnection = connections.removeAt(connectionIndex)
        connectionsInUserOrder.remove(removedConnection)
    }

    // shall be called *before* the world step
    fun updateAllConnections(deltaTime: Float) {
        connections.forEach {
            it.update(deltaTime)
        }
    }

    fun renderAllConnections() {
        connections.forEach { it.render() }
    }

    fun destroyAll() {
        connections.forEach {
            it.destroy()
        }
        connections.clear()
        connectionsInUserOrder.clear()
    }

    // two crushes are possible, but not family twice
    fun hasIdenticalConnectionOfType(connectionType: Type, personA: Person, personB: Person): Boolean {
        connections.forEach {
            if (it.type != connectionType) {
                return@forEach
            }
            if (connectionType.isSymmetric) {
                if (it.personA === personB && it.personB === personA) {
                    return true
                }
            }
            if (it.personA === personA && it.personB === personB) {
                return true
            }
        }
        return false
    }

    fun destroyAllConnectionsWithPerson(existingPerson: Person) {
        val toBeDeleted = connections.filter {
            it.personA === existingPerson || it.personB === existingPerson
        }.toList()
        toBeDeleted.forEach {
            it.destroy()
            connections.remove(it)
            connectionsInUserOrder.remove(it)
        }
    }

    fun tryToRemoveLastConnectionUserAdded() {
        if (!connectionsInUserOrder.isEmpty()) {
            Toasts.showToast("Removed connection:\n${connectionsInUserOrder.last().toNiceString()}")
            val removedConnection = connectionsInUserOrder.removeLast()
            connections.remove(removedConnection)
            removedConnection.destroy()
        } else {
            Toasts.showToast("Nothing to undo.")
        }
    }

    fun addConnectionsInSortedOrder(map: List<Triple<Type, Person, Person>>) {
        map.zip(1..map.size).sortedWith(compareBy({ (it, _) -> it.second.personIndex },
                { (it, _) -> it.third.personIndex })).map { (it, i) ->
            val connection = createConnection(it.second, it.third, it.first)
            connections.add(connection)
            Pair(i, connection)
        }.sortedBy { it.first }.forEach { (_, connection) ->
            connectionsInUserOrder.add(connection)
        }
    }


    companion object {
        const val epsilon = 0.0001f
    }
}
