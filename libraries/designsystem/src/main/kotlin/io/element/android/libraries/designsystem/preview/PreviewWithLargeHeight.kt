/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.designsystem.preview

import androidx.compose.ui.tooling.preview.Preview

/**
 * Our Paparazzi tests will check components with non-null `heightDp` and use a custom rendering for them,
 * adding extra vertical space so long scrolling components can be displayed. This is a helper for that functionality.
 */
@Preview(heightDp = 1000)
annotation class PreviewWithLargeHeight
