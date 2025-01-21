/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl

import com.squareup.anvil.annotations.ContributesBinding
import io.element.android.libraries.di.AppScope
import org.matrix.rustcomponents.sdk.ClientBuilder
import javax.inject.Inject

interface ClientBuilderProvider {
    fun provide(): ClientBuilder
}

@ContributesBinding(AppScope::class)
class RustClientBuilderProvider @Inject constructor() : ClientBuilderProvider {
    override fun provide(): ClientBuilder {
        return ClientBuilder()
    }
}
