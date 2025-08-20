/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.call.impl.utils

import dev.zacsweers.metro.ContributesBinding
import io.element.android.features.call.api.CurrentCall
import io.element.android.features.call.api.CurrentCallService
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.flow.MutableStateFlow
import dev.zacsweers.metro.Inject

@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
@Inject
class DefaultCurrentCallService() : CurrentCallService {
    override val currentCall = MutableStateFlow<CurrentCall>(CurrentCall.None)

    fun onCallStarted(call: CurrentCall) {
        currentCall.value = call
    }

    fun onCallEnded() {
        currentCall.value = CurrentCall.None
    }
}
