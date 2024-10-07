/*
 * Copyright 2022-2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.rageshake.api.detection

import io.element.android.features.rageshake.api.preferences.aRageshakePreferencesState

fun aRageshakeDetectionState() = RageshakeDetectionState(
    takeScreenshot = false,
    showDialog = false,
    isStarted = false,
    preferenceState = aRageshakePreferencesState(),
    eventSink = {}
)
