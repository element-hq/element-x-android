/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.matrix.api.encryption.identity

enum class IdentityState {
    /** The user is verified with us. */
    Verified,

    /**
     * Either this is the first identity we have seen for this user, or the
     * user has acknowledged a change of identity explicitly e.g. by
     * clicking OK on a notification.
     */
    Pinned,

    /**
     * The user's identity has changed since it was pinned. The user should be
     * notified about this and given the opportunity to acknowledge the
     * change, which will make the new identity pinned.
     */
    PinViolation,

    /**
     * The user's identity has changed, and before that it was verified. This
     * is a serious problem. The user can either verify again to make this
     * identity verified, or withdraw verification to make it pinned.
     */
    VerificationViolation,
}
