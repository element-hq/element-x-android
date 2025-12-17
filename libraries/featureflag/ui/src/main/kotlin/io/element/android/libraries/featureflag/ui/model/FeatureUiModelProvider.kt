/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.featureflag.ui.model

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

fun aFeatureUiModelList(): ImmutableList<FeatureUiModel> {
    return persistentListOf(
        FeatureUiModel(key = "key1", title = "Display State Events", description = "Show state events in the timeline", icon = null, isEnabled = true),
        FeatureUiModel(key = "key2", title = "Display Room Events", description = null, icon = null, isEnabled = false),
    )
}
