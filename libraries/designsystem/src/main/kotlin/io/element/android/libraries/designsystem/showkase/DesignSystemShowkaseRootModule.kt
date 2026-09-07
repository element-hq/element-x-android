/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2022-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.designsystem.showkase

import com.airbnb.android.showkase.annotation.ShowkaseRoot
import com.airbnb.android.showkase.annotation.ShowkaseRootModule

/**
 * The single Showkase root for the app. It aggregates the previews of this module plus the
 * `@ShowkaseTypography` tokens of `:libraries:compound`.
 *
 * That typography category is load-bearing, not decorative: when Showkase 1.0.5 finds only one
 * non-empty category it builds a nav graph without the `SHOWKASE_CATEGORIES` destination, yet the
 * groups screen still navigates to it on back press, which crashes the app. Keeping a second
 * category populated keeps that destination in the graph. See [DesignSystemShowkaseRootModuleTest].
 */
@ShowkaseRoot
class DesignSystemShowkaseRootModule : ShowkaseRootModule
