/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.enterprise.api.remoteconfig

import io.element.android.libraries.wellknown.api.ElementWellKnown

/**
 * Typealias for the well-known configuration used for enterprise features.
 *
 * This will allow for easier refactoring in the future when we'll want to move as much of its logic as possible to this module.
 */
typealias RemoteEnterpriseConfig = ElementWellKnown
