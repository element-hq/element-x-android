/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.appnav.root

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.element.android.features.rageshake.api.crash.aCrashDetectionState
import io.element.android.features.rageshake.api.detection.aRageshakeDetectionState
import io.element.android.services.apperror.api.AppErrorState
import io.element.android.services.apperror.api.aAppErrorState

open class RootStateProvider : PreviewParameterProvider<RootState> {
    override val values: Sequence<RootState>
        get() = sequenceOf(
            aRootState().copy(
                rageshakeDetectionState = aRageshakeDetectionState().copy(showDialog = false),
                crashDetectionState = aCrashDetectionState().copy(crashDetected = true),
            ),
            aRootState().copy(
                rageshakeDetectionState = aRageshakeDetectionState().copy(showDialog = true),
                crashDetectionState = aCrashDetectionState().copy(crashDetected = false),
            ),
            aRootState().copy(
                errorState = aAppErrorState(),
            )
        )
}

fun aRootState() = RootState(
    rageshakeDetectionState = aRageshakeDetectionState(),
    crashDetectionState = aCrashDetectionState(),
    errorState = AppErrorState.NoError,
)
