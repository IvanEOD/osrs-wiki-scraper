package scripts.wikiscraper.utility


/* Written by IvanEOD 12/3/2022, at 9:52 AM */
sealed interface TemplateProperties {
    var name: String
    val version: IntArray

    fun getKey(): String = name
    fun getKey(version: Int = 0): String {
        return if (version == 0) getKey()
        else getKeyVersions()[version]
    }
    fun getKeyVersions(): List<String>
}

internal sealed class TemplatePropertyInternal : TemplateProperties {
    abstract fun print(prefix: String = "")
    fun print() = print("")
}

internal sealed class TemplateProperty(private val tree: TemplatePropertyInternal) : TemplateProperties by tree {

    override fun getKeyVersions(): List<String> = tree.getKeyVersions()
    companion object {
        fun new(name: String, list: Collection<String>): TemplateProperty =
            when (val versionedProperty = versionedProperty(name, list).normalize()) {
                is VersionedPropertyTree.Node -> VersionedTemplateProperty(versionedProperty)
                is VersionedPropertyTree.Leaf -> SimpleTemplateProperty(versionedProperty)
            }




        fun parse(keys: Collection<String>): List<TemplateProperty> {
            val originalKeys = keys.toList()
            val checklist = keys.toMutableList()
            val returnList = mutableListOf<TemplateProperty>()
            while (checklist.isNotEmpty()) {
                val key = checklist.first()
                val chunk = getNameChunk(key, checklist)
                if (chunk.length <= 1) {
                    checklist.remove(key)
                    returnList.add(SimpleTemplateProperty(key))
                } else {
                    val versions = checklist.filter { it.startsWith(chunk) }
                    if (versions.size == 1) {
                        val value = versions.first()
                        checklist.remove(value)
                        returnList.add(SimpleTemplateProperty(value))
                    } else {
                        versions.forEach { checklist.remove(it) }
                        returnList.add(new(chunk, versions))
                    }
                }
            }
            println("Original keys contains 'release': ${originalKeys.contains("release")}")
            println("Return list contains 'release': ${returnList.any { it.name == "release" }}")

            val skipped = originalKeys.filter { !returnList.any { property -> property.getKey() != it } }
            println("Skipped keys: $skipped")
            return returnList
        }
    }
}

internal interface MultiVersionProperty {
    fun getKeys(vararg version: Int): List<String>
    fun getKeys(): List<String>
    fun getVersions(vararg version: Int): List<IntArray>
    fun getVersions(): List<IntArray>
    fun getVersions(vararg keys: String): List<IntArray>
}

internal sealed class VersionedPropertyTree : TemplatePropertyInternal() {
    var parent: Node? = null

    override val version: IntArray
        get() = if (parent == null) intArrayOf() else intArrayOf(*parent!!.version, parent!!.children.indexOf(this) + 1)

    fun normalize(): VersionedPropertyTree {
        when (this) {
            is Leaf -> return this
            is Node -> {
                if (children.isEmpty()) {
                    val returnLeaf = Leaf(name)
                    returnLeaf.parent = parent
                    return returnLeaf
                }
                if (children.size == 1) {
                    val child = children[0]
                    if (child is Leaf) {
                        val returnLeaf = Leaf(name)
                        returnLeaf.parent = parent
                        return returnLeaf
                    }
                    if (child is Node) {
                        child.name = name
                        child.parent = parent
                        return child
                    }
                }
                return this
            }
        }
    }

    data class Node internal constructor(
        override var name: String,
        val children: MutableList<VersionedPropertyTree> = mutableListOf()
    ) : VersionedPropertyTree(), MultiVersionProperty {
        override fun toString(): String {
            return "Node(name='$name', children=${children.joinToString(", \n")})"
        }

        override fun print(prefix: String) {
            println("${prefix}Name: $name")
            if (version.isNotEmpty()) println("${prefix}Version: ${version.joinToString(".")}")
            if (children.isNotEmpty()) children.forEach { it.print("$prefix    ") }
        }

        fun getAllLeafNodes(): List<Leaf> {
            val leafs = mutableListOf<Leaf>()
            children.forEach {
                if (it is Leaf) leafs.add(it)
                else leafs.addAll((it as Node).getAllLeafNodes())
            }
            return leafs
        }


        override fun getKeys(vararg version: Int): List<String> {
            val keys = mutableListOf<String>()
            val leafs = getAllLeafNodes()
            leafs.forEach {
                if (it.version.size >= version.size) {
                    var isMatch = true
                    for (i in version.indices) {
                        if (it.version[i] != version[i]) {
                            isMatch = false
                            break
                        }
                    }
                    if (isMatch) keys.add(it.name)
                }
            }
            return keys
        }
        override fun getKeys(): List<String> = getAllLeafNodes().map { it.name }
        override fun getVersions(): List<IntArray> = getAllLeafNodes().map { it.version }
        override fun getVersions(vararg version: Int): List<IntArray> = getAllLeafNodes()
            .filter {
                if (it.version.size >= version.size) {
                    var isMatch = true
                    for (i in version.indices) {
                        if (it.version[i] != version[i]) {
                            isMatch = false
                            break
                        }
                    }
                    isMatch
                } else false
            }.map { it.version }
            .distinctByVersion()
        override fun getVersions(vararg keys: String): List<IntArray> = getAllLeafNodes()
            .filter { keys.contains(it.name) || keys.any { key -> it.name.startsWith(key) } }
            .map { it.version }
            .distinctByVersion()
        override fun getKeyVersions(): List<String> = getKeys().filter { it != name }

        inline fun <reified T : VersionedPropertyTree> addChild(node: T): T {
            children.add(node)
            node.parent = this
            return node
        }

        fun addLeaf(name: String): Leaf = addChild(Leaf(name))
    }

    data class Leaf internal constructor(override var name: String) : VersionedPropertyTree() {
        override fun toString(): String =
            "Leaf(name=$name, version: ${if (version.isEmpty()) "0" else version.joinToString(".")})"
        override fun print(prefix: String) {
            println("${prefix}Name: $name ${if (version.isNotEmpty()) ", Version: ${version.joinToString(".")}" else ""}")
        }
        override fun getKeyVersions(): List<String> = emptyList()
    }

}

internal class SimpleTemplateProperty internal constructor(input: VersionedPropertyTree.Leaf) : TemplateProperty(input) {
    internal constructor(name: String) : this(VersionedPropertyTree.Leaf(name))
    override val version: IntArray = intArrayOf()
    override fun toString(): String = "SimpleTemplateProperty(key=$name)"
}

internal class VersionedTemplateProperty internal constructor(input: VersionedPropertyTree.Node) : TemplateProperty(input),
    MultiVersionProperty by input {
    override fun toString(): String = "VersionedTemplateProperty(key=$name, children=${getKeys()})"
}

private fun versionedProperty(
    name: String,
    list: Collection<String>,
    parent: VersionedPropertyTree.Node? = null
): VersionedPropertyTree {
    val cleanedList = list.filter { it.startsWith(name) || it == name }
    val chunk = getNameChunk(name, cleanedList)
    if (chunk.isEmpty()) return parent?.addLeaf(name) ?: VersionedPropertyTree.Leaf(name)
    else {
        println("Chunk: $chunk")
    }
    val versions = cleanedList.mapNotNull { it.substringAfter(chunk).firstOrNull()?.digitToIntOrNull() }
    val versioned = cleanedList.zip(versions).map { it.first to it.second }
    val versionedMap = versioned.groupBy { it.second }
    val tree = VersionedPropertyTree.Node(name)
    versionedMap.forEach { (version, subVersions) ->
        if (subVersions.size == 1) tree.addLeaf(subVersions[0].first)
        else {
            val names = subVersions.map { it.first }
            val common = names.commonPrefix()
            tree.addChild(versionedProperty(common, names, tree))
        }
    }
    return tree
}

private fun Collection<String>.commonPrefix(): String {
    val first = first()
    val last = last()
    return first.zip(last).takeWhile { it.first == it.second }.map { it.first }.joinToString("")
}

private fun getNameChunk(name: String, list: List<String>): String {
    val cleaned = list.filter { it != name }
    var runningList = cleaned
    var lastMatch = 0
    for (i in name.indices) {
        val char = name[i]
        val matches = runningList.filter { it.length >= i + 1 && it[i] == char }
        if (matches.isEmpty()) break
        lastMatch = i
        runningList = matches
    }
    val possibleChunk = name.take(lastMatch + 1)

    if (possibleChunk.length > 1) {
        val matching = cleaned.filter { it.startsWith(possibleChunk) }
        println("Chunked words = $matching")
        val withNumbers = matching.filter { it.length > possibleChunk.length && it[possibleChunk.length].isDigit() }
        if (withNumbers.size > 1) return possibleChunk
    }

    return ""
}

private fun List<IntArray>.distinctByVersion(): List<IntArray> = distinctBy { it.joinToString(".") }