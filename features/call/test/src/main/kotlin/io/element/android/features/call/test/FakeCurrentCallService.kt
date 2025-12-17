/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.call.test

import io.element.android.features.call.api.CurrentCall
import io.element.android.features.call.api.CurrentCallService
import kotlinx.coroutines.flow.MutableStateFlow

class FakeCurrentCallService(
    override val currentCall: MutableStateFlow<CurrentCall> = MutableStateFlow(CurrentCall.None),
) : CurrentCallService
