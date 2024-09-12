/*
 * Copyright 2023, 2024 New Vector Ltd.
 * Copyright 2021 Google LLC
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.maplibre.compose

import androidx.compose.runtime.ComposableTargetMarker

/**
 * An annotation that can be used to mark a composable function as being expected to be use in a
 * composable function that is also marked or inferred to be marked as a [MapLibreMapComposable].
 *
 * This will produce build warnings when [MapLibreMapComposable] composable functions are used outside
 * of a [MapLibreMapComposable] content lambda, and vice versa.
 */
@Retention(AnnotationRetention.BINARY)
@ComposableTargetMarker(description = "MapLibre Map Composable")
@Target(
    AnnotationTarget.FILE,
    AnnotationTarget.FUNCTION,
    AnnotationTarget.PROPERTY_GETTER,
    AnnotationTarget.TYPE,
    AnnotationTarget.TYPE_PARAMETER,
)
public annotation class MapLibreMapComposable
