/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.contentscanner.impl

import androidx.collection.LruCache
import androidx.compose.runtime.MutableState
import io.element.android.features.contentscanner.api.ContentScannerService
import io.element.android.libraries.matrix.ui.media.contentvalidation.EventContentValidationCache
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.di.annotations.SessionCoroutineScope
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.media.MediaSource
import io.element.android.libraries.matrix.api.scanner.ContentScanner
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.io.IOException

/**
 * Default implementation of [ContentScannerService] that uses a [ContentScanner] to scan media sources.
 *
 * When a scan is requested for a given event and media source, it'll check if a scan is really needed (the media source has not been scanned before),
 * then it will use a per-event mutex to avoid performing concurrent scans for thumbnail and full-size media sources.
 *
 * If, before starting a new scan, the cached result is already a failure (either we're scanning the full media source and the thumbnail already failed
 * or the other way around), it will not perform the scan again.
 */
class DefaultContentScannerService(
    private val contentScanner: ContentScanner,
    private val eventContentValidationCache: EventContentValidationCache,
    @SessionCoroutineScope private val sessionCoroutineScope: CoroutineScope,
) : ContentScannerService {
    private val cache = LruCache<EventId, ContentScannerResult>(100)

    override fun scan(eventId: EventId, mediaSource: MediaSource, updateState: (AsyncData<Boolean>) -> Unit) {
        val url = mediaSource.safeUrl
        val cachedResult = cache[eventId]
        val needsScan = cachedResult?.needsScan(url) ?: true
        if (!needsScan) {
            updateState(cachedResult.state.value)
            return
        }

        val cachedValue = cache.getOrPut(eventId) {
            ContentScannerResult(
                eventId = eventId,
                mutex = Mutex(),
                state = eventContentValidationCache[eventId].state,
                handledSources = mutableSetOf(),
            )
        }

        sessionCoroutineScope.launch {
            cachedValue.mutex.withLock {
                if (cachedValue.state.value.dataOrNull() == false) {
                    // If the cached result is already a failure, we don't need to scan again
                    return@launch
                }
                updateState(AsyncData.Loading())
                contentScanner.scan(mediaSource)
                    .onSuccess {
                        val cachedValue = cache[eventId]
                        updateState(AsyncData.Success(it))

                        cachedValue?.addHandledSource(url)
                    }.onFailure { exception ->
                        if (exception is IOException) {
                            // If it's an IO-related exception, we can retry later, so we don't cache it
                            updateState(AsyncData.Uninitialized)
                        } else {
                            // For other exceptions, we cache the failure
                            updateState(AsyncData.Failure(exception))

                            cachedValue.addHandledSource(url)
                        }
                    }
            }
        }
    }
}

private data class ContentScannerResult(
    val eventId: EventId,
    val mutex: Mutex,
    val state: MutableState<AsyncData<Boolean>>,
    val handledSources: MutableSet<String>,
) {
    fun addHandledSource(url: String) {
        handledSources.add(url)
    }

    fun needsScan(url: String): Boolean {
        return !handledSources.contains(url)
    }
}

private fun <K : Any, V : Any> LruCache<K, V>.getOrPut(key: K, defaultValue: (K) -> V): V {
    return this[key] ?: defaultValue(key).also { put(key, it) }
}
