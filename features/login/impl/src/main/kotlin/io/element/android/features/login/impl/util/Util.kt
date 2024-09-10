/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.login.impl.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import io.element.android.appconfig.AuthenticationConfig
import io.element.android.libraries.core.data.tryOrNull

fun openLearnMorePage(context: Context) {
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(AuthenticationConfig.SLIDING_SYNC_READ_MORE_URL))
    tryOrNull { context.startActivity(intent) }
}
