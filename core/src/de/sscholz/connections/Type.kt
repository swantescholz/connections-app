package de.sscholz.connections

enum class Type(val isSymmetric: Boolean = false) {
    Friendship(true),
    Codependency(true),
    Love(true),
    Crush,
    Resentment,
    Hug(true),
    Family(true),
    Work, //Enmity, Imitation, Chance_Encounter
    Abuse,
}