/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.call.impl.utils

import com.squareup.anvil.annotations.ContributesBinding
import io.element.android.features.call.api.CurrentCall
import io.element.android.features.call.api.CurrentCallService
import io.element.android.libraries.di.AppScope
import io.element.android.libraries.di.SingleIn
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject

@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
class DefaultCurrentCallService @Inject constructor() : CurrentCallService {
    override val currentCall = MutableStateFlow<CurrentCall>(CurrentCall.None)

    fun onCallStarted(call: CurrentCall) {
        currentCall.value = call
    }

    fun onCallEnded() {
        currentCall.value = CurrentCall.None
    }
}
