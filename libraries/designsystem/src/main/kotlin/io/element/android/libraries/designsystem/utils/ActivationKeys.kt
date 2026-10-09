/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.designsystem.utils

import androidx.compose.ui.input.key.Key

/**
 * Keys which can be used to activate (i.e. click) the component having the keyboard focus.
 */
val activationKeys = setOf(
    Key.Enter,
    Key.NumPadEnter,
    Key.DirectionCenter,
    Key.Spacebar,
)
