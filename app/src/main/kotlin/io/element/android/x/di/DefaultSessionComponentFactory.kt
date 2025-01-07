/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.x.di

import com.squareup.anvil.annotations.ContributesBinding
import io.element.android.appnav.di.SessionComponentFactory
import io.element.android.libraries.di.AppScope
import io.element.android.libraries.matrix.api.MatrixClient
import javax.inject.Inject

@ContributesBinding(AppScope::class)
class DefaultSessionComponentFactory @Inject constructor(
    private val sessionComponentBuilder: SessionComponent.Builder
) : SessionComponentFactory {
    override fun create(client: MatrixClient): Any {
        return sessionComponentBuilder.client(client).build()
    }
}
