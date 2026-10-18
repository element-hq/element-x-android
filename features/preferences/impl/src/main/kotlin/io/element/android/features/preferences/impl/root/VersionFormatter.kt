/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.preferences.impl.root

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import io.element.android.libraries.core.meta.BuildMeta
import io.element.android.libraries.ui.strings.CommonStrings
import io.element.android.services.toolbox.api.strings.StringProvider

interface VersionFormatter {
    fun get(): String
}

@ContributesBinding(AppScope::class)
class DefaultVersionFormatter(
    private val stringProvider: StringProvider,
    private val buildMeta: BuildMeta,
) : VersionFormatter {
    override fun get(): String {
        return stringProvider.getString(
            CommonStrings.settings_version_number,
            buildMeta.versionName,
            buildMeta.versionCode.toString()
        )
    }
}
