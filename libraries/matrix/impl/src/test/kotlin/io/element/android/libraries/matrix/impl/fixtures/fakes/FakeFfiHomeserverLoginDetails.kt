/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.fixtures.fakes

import org.matrix.rustcomponents.sdk.HomeserverLoginDetails
import org.matrix.rustcomponents.sdk.NoPointer

class FakeFfiHomeserverLoginDetails(
    private val url: String = "https://example.org",
    private val supportsPasswordLogin: Boolean = true,
    private val supportsOidcLogin: Boolean = false
) : HomeserverLoginDetails(NoPointer) {
    override fun url(): String = url
    override fun supportsOidcLogin(): Boolean = supportsOidcLogin
    override fun supportsPasswordLogin(): Boolean = supportsPasswordLogin
}
