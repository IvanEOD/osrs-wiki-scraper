package org.tribot.wikiscraper.lua

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentLinkedDeque

class TitleQueue(titles: Collection<String>, val chunkSize: Int = 100) {
    private val titles: ConcurrentLinkedDeque<String> = ConcurrentLinkedDeque(titles)
    private val titlesChannel: Channel<List<String>> =
        Channel(if (titles.size > chunkSize) titles.size / chunkSize else 1)
    private var emptyIterations = 0

    fun CoroutineScope.process(processor: suspend (List<String>) -> List<String>) {
        val job = launch {
            titlesChannel.consumeEach {
                launch {
                    val failedTitles = processor(it)
                    if (failedTitles.isNotEmpty()) titles.addAll(failedTitles)
                }
            }
        }

        launch {
            while (emptyIterations < 5) {
                val chunk = titles.pollChunk(chunkSize)
                if (chunk.isEmpty()) emptyIterations++
                titlesChannel.send(chunk)
                delay(20)
            }
            job.cancel()
        }
    }
}

fun ConcurrentLinkedDeque<String>.pollChunk(quantity: Int): List<String> {
    val chunk = mutableListOf<String>()
    for (i in 0 until quantity) {
        val title = pollFirst()
        if (title != null) chunk.add(title)
        else break
    }
    return chunk
}