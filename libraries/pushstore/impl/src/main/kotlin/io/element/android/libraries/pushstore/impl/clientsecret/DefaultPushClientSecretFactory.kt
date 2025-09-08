/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.pushstore.impl.clientsecret

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import io.element.android.libraries.pushstore.api.clientsecret.PushClientSecretFactory
import java.util.UUID

@ContributesBinding(AppScope::class)
@Inject
class DefaultPushClientSecretFactory : PushClientSecretFactory {
    override fun create(): String {
        return UUID.randomUUID().toString()
    }
}
