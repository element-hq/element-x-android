/*
 * Copyright (c) 2023 New Vector Ltd
 * Copyright 2021 Google LLC
 * Copied and adapted from android-maps-compose (https://github.com/googlemaps/android-maps-compose)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
