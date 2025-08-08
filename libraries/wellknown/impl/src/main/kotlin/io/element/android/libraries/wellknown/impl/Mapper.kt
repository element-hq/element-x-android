/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.wellknown.impl

import io.element.android.libraries.wellknown.api.ElementWellKnown
import io.element.android.libraries.wellknown.api.WellKnown
import io.element.android.libraries.wellknown.api.WellKnownBaseConfig

internal fun InternalElementWellKnown.map() = ElementWellKnown(
    registrationHelperUrl = registrationHelperUrl,
    enforceElementPro = enforceElementPro,
    rageshakeUrl = rageshakeUrl,
)

internal fun InternalWellKnown.map() = WellKnown(
    homeServer = homeServer?.map(),
    identityServer = identityServer?.map(),
)

internal fun InternalWellKnownBaseConfig.map() = WellKnownBaseConfig(
    baseURL = baseURL,
)
