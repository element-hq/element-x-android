/*
 * Copyright (c) 2025 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.linknewdevice

import io.element.android.libraries.matrix.api.linknewdevice.ErrorType
import org.matrix.rustcomponents.sdk.HumanQrGrantLoginException

internal fun HumanQrGrantLoginException.map() = when (this) {
    is HumanQrGrantLoginException.DeviceIdAlreadyInUse -> ErrorType.DeviceIdAlreadyInUse(message.orEmpty())
    is HumanQrGrantLoginException.InvalidCheckCode -> ErrorType.InvalidCheckCode(message.orEmpty())
    is HumanQrGrantLoginException.MissingSecretsBackup -> ErrorType.MissingSecretsBackup(message.orEmpty())
    is HumanQrGrantLoginException.NotFound -> ErrorType.NotFound(message.orEmpty())
    is HumanQrGrantLoginException.Cancelled -> ErrorType.Cancelled(message.orEmpty())
    is HumanQrGrantLoginException.ConnectionInsecure -> ErrorType.ConnectionInsecure(message.orEmpty())
    is HumanQrGrantLoginException.DeviceNotFound -> ErrorType.DeviceNotFound(message.orEmpty())
    is HumanQrGrantLoginException.Expired -> ErrorType.Expired(message.orEmpty())
    is HumanQrGrantLoginException.OtherDeviceAlreadySignedIn -> ErrorType.OtherDeviceAlreadySignedIn(message.orEmpty())
    is HumanQrGrantLoginException.Unknown -> ErrorType.Unknown(message.orEmpty())
    is HumanQrGrantLoginException.UnsupportedProtocol -> ErrorType.UnsupportedProtocol(message.orEmpty())
    is HumanQrGrantLoginException.UnsupportedQrCodeType -> ErrorType.UnsupportedQrCodeType(message.orEmpty())
}
