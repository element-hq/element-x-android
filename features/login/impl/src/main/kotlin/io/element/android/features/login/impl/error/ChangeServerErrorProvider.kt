/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.login.impl.error

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.element.android.features.login.impl.R

class ChangeServerErrorProvider : PreviewParameterProvider<ChangeServerError> {
    override val values: Sequence<ChangeServerError>
        get() = sequenceOf(
            ChangeServerError.Error(
                messageId = R.string.screen_change_server_error_invalid_homeserver,
            ),
            ChangeServerError.Error(
                messageStr = "An error description",
            ),
            ChangeServerError.NeedElementPro(
                unauthorisedAccountProviderTitle = "element.io",
                applicationId = "io.element.enterprise",
            ),
            ChangeServerError.UnauthorizedAccountProvider(
                unauthorisedAccountProviderTitle = "element.io",
                authorisedAccountProviderTitles = listOf("provider.org", "provider.io"),
            ),
            ChangeServerError.SlidingSyncAlert,
        )
}
