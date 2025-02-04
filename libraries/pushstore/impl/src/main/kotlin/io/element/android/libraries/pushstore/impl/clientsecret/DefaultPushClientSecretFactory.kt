/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.pushstore.impl.clientsecret

import com.squareup.anvil.annotations.ContributesBinding
import io.element.android.libraries.di.AppScope
import io.element.android.libraries.pushstore.api.clientsecret.PushClientSecretFactory
import java.util.UUID
import javax.inject.Inject

@ContributesBinding(AppScope::class)
class DefaultPushClientSecretFactory @Inject constructor() : PushClientSecretFactory {
    override fun create(): String {
        return UUID.randomUUID().toString()
    }
}
