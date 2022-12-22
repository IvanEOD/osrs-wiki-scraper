package scripts.wikiscraper.utility

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.produce
import java.util.concurrent.ConcurrentLinkedDeque

class TitleQueue(titles: Collection<String>, val chunkSize: Int = 100) {
    private val start = titles.size
    private val titles: ConcurrentLinkedDeque<String> = ConcurrentLinkedDeque(titles)
    private var emptyIterations = 0
    private val finished get() = emptyIterations > 5
    private val consumerCount = 20
    private var completedCount = 0
    private var activeJobs = 0

    fun print() {
        print("$completedCount of $start completed.\r")
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun CoroutineScope.produceChannel(): ReceiveChannel<List<String>> = produce {
        while (!finished) {
            val chunk = titles.pollChunk(chunkSize)
            if (chunk.isEmpty()) {
                emptyIterations++
                if (emptyIterations > 5) {
                    close()
                    break
                }
            }
            else emptyIterations = 0
            send(chunk)
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class, DelicateCoroutinesApi::class)
    fun execute(processor: suspend (List<String>) -> List<String>?) = runBlocking {
        GlobalScope.launch {
            val channel = produceChannel()
            val consumeJobs = mutableListOf<Job>()
            while (!channel.isClosedForReceive && !finished && consumeJobs.count { !it.isCompleted && !it.isCancelled } < consumerCount) {
                consumeJobs += launch {
                    var cancelled = false
                    while (!channel.isClosedForReceive && !finished && !cancelled) {
                        runCatching {
                            val chunk = channel.receive()
                            if (chunk.isEmpty()) cancelled = true
                            else {
                                val failed = processor(chunk) ?: emptyList()
                                if (failed.isNotEmpty()) {
                                    if (failed.size > 50) {
                                        failed.chunked(failed.size / 2).forEach {
                                            titles.addAll(it)
                                            delay(100)
                                        }
                                    } else titles.addAll(failed)
                                } else completedCount += chunk.size
                            }
                        }.onFailure {
                            cancelled = true
                        }
                        print()
                    }
                }
                activeJobs = consumeJobs.count { !it.isCompleted && !it.isCancelled }
            }
            activeJobs = consumeJobs.count { !it.isCompleted && !it.isCancelled }
        }.join()
    }

    private fun ConcurrentLinkedDeque<String>.pollChunk(quantity: Int): List<String> {
        val chunk = mutableListOf<String>()
        for (i in 0 until quantity) {
            val title = pollFirst()
            if (title != null) chunk.add(title)
            else break
        }
        return chunk
    }

}

