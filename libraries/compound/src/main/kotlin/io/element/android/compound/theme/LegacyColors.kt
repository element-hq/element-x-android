/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.compound.theme

import androidx.compose.ui.graphics.Color
import io.element.android.compound.annotations.CoreColorToken
import io.element.android.compound.tokens.generated.internal.DarkColorTokens
import io.element.android.compound.tokens.generated.internal.LightColorTokens

// =================================================================================================
// IMPORTANT!
// We should not be adding any new colors here. This file is only for legacy colors.
// In fact, we should try to remove any references to these colors as we
// iterate through the designs. All new colors should come from Compound's Design Tokens.
// =================================================================================================

val LinkColor = Color(0xFF0086E6)

@OptIn(CoreColorToken::class)
val SnackBarLabelColorLight = LightColorTokens.colorGray700
@OptIn(CoreColorToken::class)
val SnackBarLabelColorDark = DarkColorTokens.colorGray700
