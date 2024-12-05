/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.tests.konsist.failures

import androidx.compose.runtime.Composable

// Make test `Sealed interface used in Composable MUST be Immutable or Stable` fails

sealed interface SealedInterface

@Composable
fun FailingComposableWithNonImmutableSealedInterface(
    sealedInterface: SealedInterface
) {
}
