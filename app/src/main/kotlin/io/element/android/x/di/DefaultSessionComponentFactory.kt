/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.x.di

import dev.zacsweers.metro.ContributesBinding
import io.element.android.appnav.di.SessionComponentFactory
import io.element.android.libraries.di.AppScope
import io.element.android.libraries.matrix.api.MatrixClient
import dev.zacsweers.metro.Inject

@ContributesBinding(AppScope::class)
@Inject
class DefaultSessionComponentFactory(
    private val sessionComponentBuilder: SessionComponent.Factory
) : SessionComponentFactory {
    override fun create(client: MatrixClient): Any {
        return sessionComponentBuilder.create(client)
    }
}
