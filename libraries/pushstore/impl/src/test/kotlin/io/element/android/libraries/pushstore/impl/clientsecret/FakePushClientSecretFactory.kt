/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.pushstore.impl.clientsecret

import io.element.android.libraries.pushstore.api.clientsecret.PushClientSecretFactory

private const val A_SECRET_PREFIX = "A_SECRET_"

class FakePushClientSecretFactory : PushClientSecretFactory {
    private var index = 0

    override fun create() = getSecretForUser(index++)

    fun getSecretForUser(i: Int): String {
        return A_SECRET_PREFIX + i
    }
}
