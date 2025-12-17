/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.testtags

import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.semantics.testTagsAsResourceId

/**
 * Add a testTag to a Modifier, to be used by external tool, like TrafficLight for instance.
 */
fun Modifier.testTag(id: TestTag) = semantics {
    testTag = id.value
    testTagsAsResourceId = true
}
