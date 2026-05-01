/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.push.test.notifications

import io.element.android.libraries.push.api.notifications.SoundDisplayNameResolver

class FakeSoundDisplayNameResolver(
    // Default resolves to a non-null sentinel so a default-constructed fake doesn't masquerade
    // as "every Custom URI is unresolvable" — that would trip mid-session detection paths in
    // every test that uses default fakes. Tests that need the unavailable case pass `{ null }`.
    private val resolveLambda: suspend (uri: String) -> String? = { "FakeRingtoneTitle" },
) : SoundDisplayNameResolver {
    override suspend fun resolveCustomSoundTitle(uri: String): String? = resolveLambda(uri)
}
