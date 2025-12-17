/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.lockscreen.impl.pin

open class DefaultPinCodeManagerCallback : PinCodeManager.Callback {
    override fun onPinCodeVerified() = Unit

    override fun onPinCodeCreated() = Unit

    override fun onPinCodeRemoved() = Unit
}
