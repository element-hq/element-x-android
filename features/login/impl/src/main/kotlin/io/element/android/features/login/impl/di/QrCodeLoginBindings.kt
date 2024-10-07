/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.login.impl.di

import com.squareup.anvil.annotations.ContributesTo
import io.element.android.features.login.impl.qrcode.QrCodeLoginManager

@ContributesTo(QrCodeLoginScope::class)
interface QrCodeLoginBindings {
    fun qrCodeLoginManager(): QrCodeLoginManager
}
