/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.pushproviders.unifiedpush

import com.squareup.anvil.annotations.ContributesBinding
import io.element.android.libraries.di.AppScope
import javax.inject.Inject

interface GuardServiceStarter {
    fun start() {}
    fun stop() {}
}

@ContributesBinding(AppScope::class)
class NoopGuardServiceStarter @Inject constructor() : GuardServiceStarter
