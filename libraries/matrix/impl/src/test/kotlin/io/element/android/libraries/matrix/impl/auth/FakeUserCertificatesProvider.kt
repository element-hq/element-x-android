/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.auth

import io.element.android.libraries.matrix.impl.certificates.UserCertificatesProvider

class FakeUserCertificatesProvider : UserCertificatesProvider {
    override fun provides(): List<ByteArray> {
        return emptyList()
    }
}
