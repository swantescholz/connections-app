package de.sscholz.util

import java.util.*
import kotlin.collections.HashMap
import kotlin.system.exitProcess

// all utils are here in this file


fun log(vararg args: Any?) {
    printl(*args)
}


val defaultRandom = Random().apply { setSeed(0) }
fun <E> List<E>.random(): E? = if (size > 0) get(defaultRandom.nextInt(size)) else null
fun ClosedRange<Double>.random() = defaultRandom.nextDouble() * (endInclusive - start) + start
fun ClosedRange<Float>.random() = defaultRandom.nextFloat() * (endInclusive - start) + start
fun ClosedRange<Double>.interpolate(alpha: Double) = alpha * (endInclusive - start) + start
fun ClosedRange<Float>.interpolate(alpha: Float) = alpha * (endInclusive - start) + start

fun myAssert(condition: Boolean, messageGenerator: () -> String = { "<none>" }) {
    if (!condition) {
        throw AssertionError("myAssert failed! Error message:\n${messageGenerator()}")
    }
}

val ALPHABET_LOWER = "abcdefghijklmnopqrstuvwxyz".toHashSet()
val ALPHABET_UPPER = "ABCDEFGHIJKLMNOPQRSTUVWXYZ".toHashSet()
val ALPHABET = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ".toHashSet()
val READABLE_CHARS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789!@#$%^&*()-=_+[{}]'| \n\t\"\\,./;?<>`~".toHashSet()

fun Int.times(function: () -> Unit) {
    for (i in 1..this) {
        function()
    }
}

fun <T> ArrayList<T>.removeLast(): T = this.removeAt(this.size - 1)

val Boolean.i: Int get() = toInt()
fun Boolean.toInt(): Int = if (this) 1 else 0
val Number.d: Double get() = toDouble()
val Number.i: Int get() = toInt()
val Number.f: Float get() = toFloat()
val Number.l: Long get() = toLong()

val String.i: Int? get() = toIntOrNull()
val String.f: Float? get() = toFloatOrNull()
val String.d: Double? get() = toDoubleOrNull()

fun Float.toDegree() = this * 180f / Math.PI.toFloat()
fun Float.toRadian() = this * Math.PI.toFloat() / 180f

fun Any.pln() {
    println(this)
}

fun Any.printl() {
    println(this)
}

fun Any.print() {
    kotlin.io.print(this)
}

fun print(vararg args: Any?) {
    var s = ""
    for (it in args) {
        s += it.toString() + " "
    }
    System.out.print(s.removeSuffix(" "))
}

fun printl(vararg args: Any?) {
    var s = ""
    for (it in args) {
        s += it.toString() + " "
    }
    System.out.println(s.removeSuffix(" "))
}

fun <T> Sequence<T>.printlnAll() {
    for (it in this)
        println(it)
}

fun <T> Sequence<T>.printlnAllIndexed() {
    this.withIndex().forEach { kotlin.io.print(it.index.toString() + " "); println(it.value) }
}

fun <T> Sequence<T>.toTreeSet(): TreeSet<T> {
    return toCollection(TreeSet())
}

fun <K, V> Sequence<Pair<K, V>>.toHashMap(): HashMap<K, V> {
    val hm = HashMap<K, V>()
    hm.putAll(this)
    return hm
}

fun <K, V> Sequence<Pair<K, V>>.toSortedMap(comparator: ((K, K) -> Int)? = null): TreeMap<K, V> {
    val res = TreeMap<K, V>(comparator)
    this.forEach { res[it.first] = it.second }
    return res
}

inline fun <T> Sequence<T>.peek(crossinline f: (T) -> Unit) = this.map { f(it); it }

operator fun <A : Comparable<A>, B : Comparable<B>> Pair<A, B>.compareTo(other: Pair<A, B>): Int {
    first.compareTo(other.first).let {
        if (it != 0)
            return it
    }
    return second.compareTo(other.second)
}

fun <A, B> fst(pair: Pair<A, B>): A = pair.first
fun <A, B> snd(pair: Pair<A, B>): B = pair.second
val <A, B> Pair<A, B>.a: A
    get() = this.first
val <A, B> Pair<A, B>.b: B
    get() = this.second

data class MutablePair<A, B>(var first: A, var second: B)

fun <T> Array<T>.toArrayList(): ArrayList<T> = toCollection(ArrayList())
fun <T> Sequence<T>.toArrayList(): ArrayList<T> = toCollection(ArrayList())
fun <T> Iterable<T>.toArrayList(): ArrayList<T> = toCollection(ArrayList())

fun <T> alof(vararg elements: T): ArrayList<T> {
    val al = ArrayList<T>()
    elements.toCollection(al)
    return al
}

fun <T> treeSetOf(vararg elements: T): TreeSet<T> = elements.toCollection(TreeSet())
fun <K, V> treeMapOf(vararg pairs: Pair<K, V>): TreeMap<K, V> = TreeMap<K, V>().apply { putAll(pairs) }

class DefaultHashMap<K, V>(private val defaultGenerator: (K) -> V,
                           private val addWhenQueried: Boolean = true) : HashMap<K, V>() {
    constructor(you: DefaultHashMap<K, V>) : this(you.defaultGenerator, you.addWhenQueried) {
        for (entry in you) {
            put(entry.key, entry.value)
        }
    }

    override operator fun get(key: K): V {
        if (key in this)
            return super.get(key)!!
        val value = defaultGenerator(key)
        if (addWhenQueried)
            put(key, value)
        return value
    }
}

// reads lines and maps them until map returns null
//fun readLinesOfFile(filepath: String): Sequence<String> {
//    val reader = Files.newBufferedReader(File(filepath).toPath())
//    return generateSequence { reader.readLine() }
//}
//fun Sequence<String>.writeLinesToFile(filepath: String) {
//    val path = File(filepath).toPath()
//    val writer = Files.newBufferedWriter(path)
//    for (line in this) {
//        writer.write("$line\n")
//    }
//    writer.close()
//}

// returns null if block fails
inline fun <T> exceptionToNull(task: () -> T): T? {
    return try {
        task()
    } catch (e: Throwable) {
        null
    }
}

inline fun ignoreException(task: () -> Unit) {
    try {
        task()
    } catch (e: Throwable) {
    }
}

fun sleep(seconds: Double) {
    try {
        Thread.sleep((1000 * seconds).toLong())
    } catch (ex: InterruptedException) {
        Thread.currentThread().interrupt()
    }
}

fun quit() {
    exitProcess(-1)
}


private var printRegularlyLastTime = System.nanoTime()
fun printlnRegularly(vararg args: Any, dt: Double = 3.0) {
    val now = System.nanoTime()
    if ((now - printRegularlyLastTime) > dt * 1000000000.0) {
        printRegularlyLastTime = now
        var s = ""
        for (it in args) {
            s += "$it "
        }
        System.out.println(s.removeSuffix(" "))
    }
}

private var doRegularlyLastTimes = HashMap<String, Long>()
// executes function if last call has been at least dtInSeconds seconds ago for given id
fun doRegularly(id: String = "defaultID", dtInSeconds: Double = 5.0,
                runFunctionOnFirstCall: Boolean = true,
                function: () -> Unit) {
    val now = System.nanoTime()
    if (id !in doRegularlyLastTimes.keys) {
        doRegularlyLastTimes[id] = now
        if (runFunctionOnFirstCall) {
            function()
        }
    }
    if ((now - doRegularlyLastTimes[id]!!) > dtInSeconds * 1000000000.0) {
        doRegularlyLastTimes[id] = now
        function()
    }
}