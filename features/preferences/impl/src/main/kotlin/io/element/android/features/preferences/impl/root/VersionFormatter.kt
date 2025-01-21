/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.preferences.impl.root

import com.squareup.anvil.annotations.ContributesBinding
import io.element.android.libraries.core.meta.BuildMeta
import io.element.android.libraries.di.AppScope
import io.element.android.libraries.ui.strings.CommonStrings
import io.element.android.services.toolbox.api.strings.StringProvider
import javax.inject.Inject

interface VersionFormatter {
    fun get(): String
}

@ContributesBinding(AppScope::class)
class DefaultVersionFormatter @Inject constructor(
    private val stringProvider: StringProvider,
    private val buildMeta: BuildMeta,
) : VersionFormatter {
    override fun get(): String {
        val base = stringProvider.getString(
            CommonStrings.settings_version_number,
            buildMeta.versionName,
            buildMeta.versionCode.toString()
        )
        return if (buildMeta.gitBranchName == "main") {
            base
        } else {
            // In case of a build not from main, we display the branch name and the revision
            "$base\n${buildMeta.gitBranchName} (${buildMeta.gitRevision})"
        }
    }
}
