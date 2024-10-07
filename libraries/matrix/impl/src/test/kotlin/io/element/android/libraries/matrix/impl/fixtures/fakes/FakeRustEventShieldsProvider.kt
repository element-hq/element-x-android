/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.fixtures.fakes

import org.matrix.rustcomponents.sdk.EventShieldsProvider
import org.matrix.rustcomponents.sdk.NoPointer
import org.matrix.rustcomponents.sdk.ShieldState

class FakeRustEventShieldsProvider(
    private val shieldsState: ShieldState? = null,
) : EventShieldsProvider(NoPointer) {
    override fun getShields(strict: Boolean): ShieldState? = shieldsState
}
