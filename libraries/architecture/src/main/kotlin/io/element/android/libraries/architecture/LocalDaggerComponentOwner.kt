/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.architecture

import androidx.compose.runtime.staticCompositionLocalOf
import io.element.android.libraries.di.DaggerComponentOwner

val LocalDaggerComponentOwner = staticCompositionLocalOf<DaggerComponentOwner?> { null }
