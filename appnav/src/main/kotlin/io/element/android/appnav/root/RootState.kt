/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.appnav.root

import androidx.compose.runtime.Immutable
import io.element.android.features.rageshake.api.crash.CrashDetectionState
import io.element.android.features.rageshake.api.detection.RageshakeDetectionState
import io.element.android.services.apperror.api.AppErrorState

@Immutable
data class RootState(
    val rageshakeDetectionState: RageshakeDetectionState,
    val crashDetectionState: CrashDetectionState,
    val errorState: AppErrorState,
)
