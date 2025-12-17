/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.designsystem.atomic.molecules

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.element.android.libraries.designsystem.components.avatar.AvatarData
import io.element.android.libraries.designsystem.components.avatar.AvatarSize
import io.element.android.libraries.designsystem.components.avatar.anAvatarData

internal data class ComposerAlertMoleculeParams(
    val level: ComposerAlertLevel,
    val avatar: AvatarData? = null,
    val showIcon: Boolean = false,
)

internal class ComposerAlertMoleculeParamsProvider : PreviewParameterProvider<ComposerAlertMoleculeParams> {
    private val allLevels = sequenceOf(
        ComposerAlertLevel.Default,
        ComposerAlertLevel.Info,
        ComposerAlertLevel.Critical
    )

    override val values: Sequence<ComposerAlertMoleculeParams>
        get() = allLevels.flatMap { level ->
            sequenceOf(
                ComposerAlertMoleculeParams(level = level),
                ComposerAlertMoleculeParams(level = level, avatar = anAvatarData(size = AvatarSize.ComposerAlert)),
                ComposerAlertMoleculeParams(level = level, showIcon = true),
            )
        }
}
