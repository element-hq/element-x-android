/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.call.api

import kotlinx.coroutines.flow.StateFlow

interface CurrentCallService {
    /**
     * The current call state flow, which will be updated when the active call changes.
     * This value reflect the local state of the call. It is not updated if the user answers
     * a call from another session.
     */
    val currentCall: StateFlow<CurrentCall>
}
