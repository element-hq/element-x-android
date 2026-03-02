/*
 * Copyright (c) 2025 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.api.linknewdevice

sealed class ErrorType(message: String) : Exception(message) {
    /**
     * The requested device ID is already in use.
     */
    class DeviceIdAlreadyInUse(message: String) : ErrorType(message)

    /**
     * The check code was incorrect.
     */
    class InvalidCheckCode(message: String) : ErrorType(message)

    /**
     * The other client proposed an unsupported protocol.
     */
    class UnsupportedProtocol(message: String) : ErrorType(message)

    /**
     * Secrets backup not set up properly.
     */
    class MissingSecretsBackup(message: String) : ErrorType(message)

    /**
     * The rendezvous session was not found and might have expired.
     */
    class NotFound(message: String) : ErrorType(message)

    /**
     * An unknown error has happened.
     */
    class Unknown(message: String) : ErrorType(message)

    /**
     * The requested device was not returned by the homeserver.
     */
    class DeviceNotFound(message: String) : ErrorType(message)

    /**
     * The other device is already signed in and so does not need to sign in.
     */
    class OtherDeviceAlreadySignedIn(message: String) : ErrorType(message)

    /**
     * The sign in was cancelled.
     */
    class Cancelled(message: String) : ErrorType(message)

    /**
     * The sign in was not completed in the required time.
     */
    class Expired(message: String) : ErrorType(message)

    /**
     * A secure connection could not have been established between the two devices.
     */
    class ConnectionInsecure(message: String) : ErrorType(message)
}
