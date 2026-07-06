/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.ui.media.contentvalidation

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateOf
import io.element.android.libraries.architecture.AsyncData

/**
 * Wrapper for the content validation state of an event. It holds a [MutableState] of [AsyncData] that represents the validation state.
 */
@Stable
class ContentValidationState(
    val state: MutableState<AsyncData<Boolean>>,
) {
    constructor() : this(state = mutableStateOf(AsyncData.Uninitialized))

    constructor(isValid: Boolean) : this(state = mutableStateOf(AsyncData.Success(isValid)))

    fun isValid(): Boolean {
        return state.value.dataOrNull() == true
    }

    fun isInvalid(): Boolean {
        return state.value.dataOrNull() == false
    }

    fun update(newState: AsyncData<Boolean>) {
        state.value = newState
    }
}
