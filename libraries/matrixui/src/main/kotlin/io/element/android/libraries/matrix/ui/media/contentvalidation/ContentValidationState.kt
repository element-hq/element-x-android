/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.ui.media.contentvalidation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.update

/**
 * Wrapper for the content validation state of an event.
 *
 * It can be used to check the overall validation state of the event, as well as the validation state of each media item in it.
 */
@Immutable
interface ContentValidationState {
    /**
     * A flow that emits the overall validation state of the event, which is an aggregation of the different media values.
     */
    val overallStateFlow: Flow<ContentValidationValue>

    /**
     * Returns a flow that emits the validation state of a specific media item, identified by its URL.
     */
    fun getMediaStateFlow(url: String): Flow<ContentValidationValue>

    /**
     * Returns the current overall validation state of the event.
     */
    fun getCurrentOverallState(): ContentValidationValue

    /**
     * Returns the current validation state of a specific media item, identified by its URL.
     */
    fun getCurrentMediaState(url: String): ContentValidationValue

    /**
     * Updates the validation state of a specific media item, identified by its URL, with a new [ContentValidationValue].
     */
    fun update(url: String, newValue: ContentValidationValue)
}

/**
 * A no-op implementation of [ContentValidationState] that always returns a fixed validation state.
 * By default, this is [ContentValidationValue.Valid].
 */
@Immutable
class NoopContentValidationState(
    private val initial: ContentValidationValue = ContentValidationValue.Valid,
) : ContentValidationState {
    override val overallStateFlow: Flow<ContentValidationValue> = MutableStateFlow(initial)
    override fun getMediaStateFlow(url: String): Flow<ContentValidationValue> = MutableStateFlow(initial)
    override fun getCurrentOverallState(): ContentValidationValue = initial
    override fun getCurrentMediaState(url: String): ContentValidationValue = initial
    override fun update(url: String, newValue: ContentValidationValue) {}
}

/**
 * A default implementation of [ContentValidationState] that tracks the validation state of media items in a mutable map.
 * It calculates the overall validation state based on the individual media states.
 */
@Immutable
class DefaultContentValidationState(
    private val states: MutableStateFlow<Map<String, ContentValidationValue>>,
) : ContentValidationState {
    @OptIn(ExperimentalCoroutinesApi::class)
    override val overallStateFlow: Flow<ContentValidationValue> = states
        .mapLatest { states -> calculateOverallState(states) }
        .distinctUntilChanged()

    constructor() : this(states = MutableStateFlow(emptyMap()))

    constructor(initial: Map<String, ContentValidationValue>) : this(states = MutableStateFlow(initial))

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun getMediaStateFlow(url: String): Flow<ContentValidationValue> {
        return states.map { states ->
            states[url] ?: ContentValidationValue.Unknown
        }.distinctUntilChanged()
    }

    override fun getCurrentOverallState(): ContentValidationValue {
        return calculateOverallState(states.value)
    }

    override fun getCurrentMediaState(url: String): ContentValidationValue {
        return states.value[url] ?: ContentValidationValue.Unknown
    }

    override fun update(url: String, newValue: ContentValidationValue) {
        states.update { current ->
            if (current[url]?.isValidated() == true && newValue.isLoading()) {
                // If the current state is already validated and the new value is loading, we keep the current validated state
                current
            } else {
                // Otherwise, we update the state for the given URL with the new value
                current + (url to newValue)
            }
        }
    }
}

private fun calculateOverallState(states: Map<String, ContentValidationValue>): ContentValidationValue {
    return when {
        states.values.isEmpty() -> ContentValidationValue.Unknown
        states.values.any { it is ContentValidationValue.Invalid } -> ContentValidationValue.Invalid
        states.values.any { it is ContentValidationValue.Loading } -> ContentValidationValue.Loading
        states.values.any { it is ContentValidationValue.UnrecoverableError } -> states.values.first { it is ContentValidationValue.UnrecoverableError }
        states.values.all { it is ContentValidationValue.Valid } -> ContentValidationValue.Valid
        else -> ContentValidationValue.Unknown
    }
}

/**
 * Represents the validation state of a media item or an event.
 */
@Immutable
sealed interface ContentValidationValue {
    /** Indicates that the content is valid. */
    data object Valid : ContentValidationValue

    /** Indicates that the content is invalid/unsafe. */
    data object Invalid : ContentValidationValue

    /** Indicates that the content is currently being validated. */
    data object Loading : ContentValidationValue

    /** Indicates that an unrecoverable error occurred during validation, and it should not be retried. */
    data class UnrecoverableError(val error: Throwable) : ContentValidationValue

    /** Indicates that the validation state is unknown and should be checked. Recoverable errors are also represented with this. */
    data object Unknown : ContentValidationValue

    /**
     * Returns true if the validation state is either [Valid], [Invalid], or [UnrecoverableError], meaning that the validation process has completed
     * and a definitive result is available.
     */
    fun isValidated(): Boolean = when (this) {
        is Valid -> true
        is Invalid -> true
        is Loading -> false
        is UnrecoverableError -> true
        is Unknown -> false
    }

    /** Returns true if the validation state is [Valid]. */
    fun isValid(): Boolean = this is Valid

    /** Returns true if the validation state is [Invalid]. */
    fun isInvalid(): Boolean = this is Invalid

    /** Returns true if the validation state is [Loading]. */
    fun isLoading(): Boolean = this is Loading

    /** Returns true if the validation state is [UnrecoverableError]. */
    fun hasUnrecoverableError(): Boolean = this is UnrecoverableError

    /** Returns true if the validation state is either [Invalid] or [UnrecoverableError]. */
    fun hasError(): Boolean = this is Invalid || this is UnrecoverableError
}

/**
 * Collects the overall validation state of the [ContentValidationState] as a [State] in a Composable context.
 */
@Composable
fun ContentValidationState.collectOverallState(): State<ContentValidationValue> {
    return produceState(getCurrentOverallState()) {
        overallStateFlow.collect { value = it }
    }
}

/**
 * Collects the validation state of a specific media item, identified by its URL, as a [State] in a Composable context.
 */
@Composable
fun ContentValidationState.collectMediaState(url: String?): State<ContentValidationValue> {
    if (url == null) {
        return remember { mutableStateOf(ContentValidationValue.Valid) }
    }

    return produceState(getCurrentMediaState(url), key1 = url) {
        getMediaStateFlow(url).collect { value = it }
    }
}
