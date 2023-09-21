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

package io.element.android.libraries.designsystem.preview

import androidx.compose.ui.tooling.preview.Preview

/**
 * Our Paparazzi tests will check components with non-null `heightDp` and use a custom rendering for them,
 * adding extra vertical space so long scrolling components can be displayed. This is a helper for that functionality.
 */
@Preview(heightDp = 1000)
annotation class PreviewWithLargeHeight
