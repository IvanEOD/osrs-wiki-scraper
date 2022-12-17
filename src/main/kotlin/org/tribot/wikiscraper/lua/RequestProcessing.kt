package org.tribot.wikiscraper.lua

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.tribot.wikiscraper.classes.ItemDetails


/* Written by IvanEOD 12/17/2022, at 5:03 PM */

class TitlesProcessor<T>(
    val titles: List<String>,
    val chunkSize: Int = 100,
    val processChunk: suspend (List<String>) -> ProcessResult<T>
) {
    private val titlesChannel
    private val itemsChannel = Channel<Pair<String, List<ItemDetails>>>(Channel.UNLIMITED)

    private fun CoroutineScope.produceTitles() = produce {

    }

    fun process(): Flow<ProcessResult<T>> = flow {
        val chunks = titles.chunked(chunkSize)
        for (chunk in chunks) {
            val result = processChunk(chunk)
            emit(result)
        }
    }





    @OptIn(ExperimentalCoroutinesApi::class)
    fun CoroutineScope.produceTitles() = produce {
        titles.chunked(chunkSize).forEach { chunk ->
            send(chunk)
        }
    }



}

sealed class ProcessResult<T> {
    data class Success<T>(val items: Pair<String, T>): ProcessResult<T>()
    data class Failure<T>(val titles: List<String>): ProcessResult<T>()
}
