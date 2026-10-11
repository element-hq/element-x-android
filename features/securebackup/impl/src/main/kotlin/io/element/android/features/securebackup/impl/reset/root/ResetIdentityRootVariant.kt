/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.securebackup.impl.reset.root

/**
 * How the identity reset is presented, depending on what else the user could do to confirm their identity.
 */
enum class ResetIdentityRootVariant {
    /** The user has another verified device or a recovery key, so resetting is a last resort. */
    HasConfirmationOptions,

    /** The user can only reset, and has encrypted chats whose history will be lost. */
    NoOptionsWithEncryptedChats,

    /** The user can only reset, and has no encrypted chats to lose. */
    NoOptionsWithoutEncryptedChats;

    val isResetTheOnlyOption: Boolean get() = this != HasConfirmationOptions
}
