/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.architecture.events

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.rememberUpdatedState

// Like rememberUpdatedState but force the return type of the lambda to be Unit
@Composable
fun <T> rememberEventSink(newValue: (T) -> Unit): State<(T) -> Unit> = rememberUpdatedState(newValue)
