/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.wellknown.impl

import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import io.element.android.features.enterprise.api.EnterpriseService
import io.element.android.libraries.di.SessionScope
import io.element.android.libraries.di.annotations.SessionCoroutineScope
import io.element.android.libraries.matrix.api.UrlContentFetcher
import io.element.android.libraries.wellknown.api.ElementWellKnownParser
import io.element.android.libraries.wellknown.api.ElementWellknownStore
import io.element.android.libraries.wellknown.api.WellknownRetriever
import kotlinx.coroutines.CoroutineScope

@BindingContainer
@ContributesTo(SessionScope::class)
object SessionWellknownModule {
    @Provides
    fun provideWellKnownRetriever(
        elementWellknownStoreFactory: ElementWellknownStore.Factory,
        enterpriseService: EnterpriseService,
        elementWellKnownParser: ElementWellKnownParser,
        urlContentFetcher: UrlContentFetcher,
        @SessionCoroutineScope coroutineScope: CoroutineScope,
    ): WellknownRetriever {
        return DefaultWellknownRetriever(
            elementWellknownStoreFactory = elementWellknownStoreFactory,
            enterpriseService = enterpriseService,
            elementWellKnownParser = elementWellKnownParser,
            urlContentFetcher = urlContentFetcher,
            coroutineScope = coroutineScope
        )
    }
}
