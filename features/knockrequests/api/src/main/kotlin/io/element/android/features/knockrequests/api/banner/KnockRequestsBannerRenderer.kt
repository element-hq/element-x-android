/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.knockrequests.api.banner

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

interface KnockRequestsBannerRenderer {
    @Composable
    fun View(modifier: Modifier, onViewRequestsClick: () -> Unit)
}
