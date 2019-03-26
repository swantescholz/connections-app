package de.sscholz.util

import de.sscholz.releaseMode

// user preferences
object Preferences {
    private const val preferencesFilename = "__prefs__.txt"
    private val map = LinkedHashMap<String, String>()

    init {
        try {
            MyDatabase.readLocalFile(preferencesFilename).split("\n").forEach { line ->
                val (k, v) = line.split("\t", limit = 2)
                map[k] = v
            }
        } catch (e: Exception) {
            printl("file $preferencesFilename not found. starting from scratch.")
        }
    }

    class Preference<T>(val id: String, defaultValue: T, val parse: (String) -> T,
                        val strigify: (T) -> String = { it -> it.toString() }) {
        init {
            if (id !in Preferences.map) {
                Preferences[id] = strigify(defaultValue)
            }
        }

        fun get(): T = parse(Preferences[id]!!)
        fun set(newValue: T) {
            Preferences[id] = strigify(newValue)
        }
    }

    val mute = Preference("mute", false, { it.toBoolean() })
    val lastPlayedLevelId = Preference("lastPlayedLevelId", 1, { it.toInt() })
    val unlockedLevels = Preference("unlockedLevels", if (releaseMode) 1 else 100,
            { it.toInt() })
    val levelsHaveBeenLoaded = Preference("levelsHaveBeenLoaded", false, { it.toBoolean() })

    private operator fun get(key: String): String? = map[key]
    private operator fun set(key: String, value: String) {
        map[key] = value
        flush()
    }

    private fun flush() {
        MyDatabase.writeStringToLocalFile(preferencesFilename, toString())
    }

    override fun toString(): String {
        return map.map { (a, b) -> "$a\t$b" }.joinToString("\n")
    }
}