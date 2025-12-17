/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.login.impl.login

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.element.android.features.login.impl.error.ChangeServerErrorProvider
import io.element.android.libraries.matrix.api.auth.AuthenticationException

class LoginModeViewErrorProvider : PreviewParameterProvider<Exception> {
    override val values: Sequence<Exception>
        get() = ChangeServerErrorProvider().values +
            AuthenticationException.AccountAlreadyLoggedIn("@alice:matrix.org")
}
