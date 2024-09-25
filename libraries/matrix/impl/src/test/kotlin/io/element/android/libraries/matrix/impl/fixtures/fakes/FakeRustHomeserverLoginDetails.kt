/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.fixtures.fakes

import org.matrix.rustcomponents.sdk.HomeserverLoginDetails
import org.matrix.rustcomponents.sdk.NoPointer

class FakeRustHomeserverLoginDetails(
    private val url: String = "https://example.org",
    private val supportsPasswordLogin: Boolean = true,
    private val supportsOidcLogin: Boolean = false
) : HomeserverLoginDetails(NoPointer) {
    override fun url(): String = url
    override fun supportsOidcLogin(): Boolean = supportsOidcLogin
    override fun supportsPasswordLogin(): Boolean = supportsPasswordLogin
}
