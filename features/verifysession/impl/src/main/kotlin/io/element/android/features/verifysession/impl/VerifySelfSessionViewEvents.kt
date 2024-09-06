/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.verifysession.impl

sealed interface VerifySelfSessionViewEvents {
    data object RequestVerification : VerifySelfSessionViewEvents
    data object StartSasVerification : VerifySelfSessionViewEvents
    data object ConfirmVerification : VerifySelfSessionViewEvents
    data object DeclineVerification : VerifySelfSessionViewEvents
    data object Cancel : VerifySelfSessionViewEvents
    data object Reset : VerifySelfSessionViewEvents
    data object SignOut : VerifySelfSessionViewEvents
    data object SkipVerification : VerifySelfSessionViewEvents
}
