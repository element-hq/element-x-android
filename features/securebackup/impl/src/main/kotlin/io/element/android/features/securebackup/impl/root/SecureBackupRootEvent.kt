/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.securebackup.impl.root

sealed interface SecureBackupRootEvent {
    data object RetryKeyBackupState : SecureBackupRootEvent
    data object EnableKeyStorage : SecureBackupRootEvent
    data object DisplayKeyStorageDisabledError : SecureBackupRootEvent
    data object DismissDialog : SecureBackupRootEvent
}
