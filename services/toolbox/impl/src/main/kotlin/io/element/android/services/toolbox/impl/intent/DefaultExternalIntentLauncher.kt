/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.services.toolbox.impl.intent

import android.content.Context
import android.content.Intent
import com.squareup.anvil.annotations.ContributesBinding
import io.element.android.libraries.di.AppScope
import io.element.android.libraries.di.ApplicationContext
import io.element.android.services.toolbox.api.intent.ExternalIntentLauncher
import javax.inject.Inject

@ContributesBinding(AppScope::class)
class DefaultExternalIntentLauncher @Inject constructor(
    @ApplicationContext private val context: Context,
) : ExternalIntentLauncher {
    override fun launch(intent: Intent) {
        context.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }
}
