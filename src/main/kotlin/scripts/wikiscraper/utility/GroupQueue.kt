package scripts.wikiscraper.utility

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

internal class GroupQueue<T>(
    private val collection: Collection<T>,
    private val maxPoll: Int
) {
    private val queue: MutableList<T> = collection.toMutableList()
    internal val size get() = queue.size
    private var start = 0
    private var end = maxPoll
    private var mutex = Mutex()

    fun isNotEmpty() = start < size
    fun isEmpty() = !isNotEmpty()

    suspend fun syncPoll(): List<T> = mutex.withLock { poll() }
    fun poll(): List<T> {
        if (isEmpty()) return emptyList()
        if (size - start < maxPoll) end = size
        val temp = queue.subList(start, end)
        start += maxPoll
        end += maxPoll
        return temp
    }
}