/*
 * Copyright (c) 2023 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.element.android.libraries.theme

import androidx.compose.ui.graphics.Color
import io.element.android.libraries.theme.compound.generated.internal.DarkDesignTokens
import io.element.android.libraries.theme.compound.generated.internal.LightDesignTokens

// =================================================================================================
// IMPORTANT!
// We should not be adding any new colors here. This file is only for legacy colors.
// In fact, we should try to remove any references to these colors as we
// iterate through the designs. All new colors should come from Compound's Design Tokens.
// =================================================================================================

val LinkColor = Color(0xFF0086E6)

val SnackBarLabelColorLight = LightDesignTokens.colorGray700
val SnackBarLabelColorDark = DarkDesignTokens.colorGray700
