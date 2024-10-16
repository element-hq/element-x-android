/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.matrix.api.encryption

import io.element.android.libraries.matrix.api.core.UserId
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface EncryptionService {
    val backupStateStateFlow: StateFlow<BackupState>
    val recoveryStateStateFlow: StateFlow<RecoveryState>
    val enableRecoveryProgressStateFlow: StateFlow<EnableRecoveryProgress>
    val isLastDevice: StateFlow<Boolean>

    suspend fun enableBackups(): Result<Unit>

    /**
     * Enable recovery. Observe enableProgressStateFlow to get progress and recovery key.
     */
    suspend fun enableRecovery(waitForBackupsToUpload: Boolean): Result<Unit>

    /**
     * Change the recovery and return the new recovery key.
     */
    suspend fun resetRecoveryKey(): Result<String>

    suspend fun disableRecovery(): Result<Unit>

    suspend fun doesBackupExistOnServer(): Result<Boolean>

    /**
     * Note: accept both recoveryKey and passphrase.
     */
    suspend fun recover(recoveryKey: String): Result<Unit>

    /**
     * Wait for backup upload steady state.
     */
    fun waitForBackupUploadSteadyState(): Flow<BackupUploadState>

    /**
     * Get the public curve25519 key of our own device in base64. This is usually what is
     * called the identity key of the device.
     */
    suspend fun deviceCurve25519(): String?

    /**
     * Get the public ed25519 key of our own device. This is usually what is
     * called the fingerprint of the device.
     */
    suspend fun deviceEd25519(): String?

    /**
     * Starts the identity reset process. This will return a handle that can be used to reset the identity.
     */
    suspend fun startIdentityReset(): Result<IdentityResetHandle?>

    suspend fun isUserVerified(userId: UserId): Result<Boolean>

    /**
     * Remember this identity, ensuring it does not result in a pin violation.
     */
    suspend fun pinUserIdentity(userId: UserId): Result<Unit>
}

/**
 * A handle to reset the user's identity.
 */
sealed interface IdentityResetHandle {
    /**
     * Cancel the reset process and drops the existing handle in the SDK.
     */
    suspend fun cancel()
}

/**
 * A handle to reset the user's identity with a password login type.
 */
interface IdentityPasswordResetHandle : IdentityResetHandle {
    /**
     * Reset the password of the user.
     *
     * This method will block the coroutine it's running on and keep polling indefinitely until either the coroutine is cancelled, the [cancel] method is
     * called, or the identity is reset.
     *
     * @param password the current password, which will be validated before the process takes place.
     */
    suspend fun resetPassword(password: String): Result<Unit>
}

/**
 * A handle to reset the user's identity with an OIDC login type.
 */
interface IdentityOidcResetHandle : IdentityResetHandle {
    /**
     * The URL to open in a webview/custom tab to reset the identity.
     */
    val url: String

    /**
     * Reset the identity using the OIDC flow.
     *
     * This method will block the coroutine it's running on and keep polling indefinitely until either the coroutine is cancelled, the [cancel] method is
     * called, or the identity is reset.
     */
    suspend fun resetOidc(): Result<Unit>
}
