/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.featureflag.test

import io.element.android.libraries.core.meta.BuildMeta
import io.element.android.libraries.featureflag.api.Feature

data class FakeFeature(
    override val key: String,
    override val title: String,
    override val description: String? = null,
    override val defaultValue: (BuildMeta) -> Boolean = { false },
    override val isFinished: Boolean = false,
    override val isInLabs: Boolean = false,
) : Feature
