/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.login.impl.support

class FakeHomeserverSupportContactProvider(
    private val contact: String? = null,
) : HomeserverSupportContactProvider {
    override suspend fun getContact(homeserverUrl: String): String? = contact
}
