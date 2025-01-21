/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.featureflag.ui.model

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

fun aFeatureUiModelList(): ImmutableList<FeatureUiModel> {
    return persistentListOf(
        FeatureUiModel("key1", "Display State Events", "Show state events in the timeline", true),
        FeatureUiModel("key2", "Display Room Events", null, false),
    )
}
