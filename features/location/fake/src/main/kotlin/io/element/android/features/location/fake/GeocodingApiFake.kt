/*
 * Copyright (c) 2023 New Vector Ltd
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

package io.element.android.features.location.fake

import io.element.android.features.location.api.GeocodingApi
import io.element.android.features.location.api.ReverseGeocodingResponse

/**
 * Creates a fake [GeocodingApi] for tests.
 */
fun createGeocodingApiFake(): GeocodingApi = object : GeocodingApi {
    override suspend fun reverseGeocoding(lat: Double, lon: Double): ReverseGeocodingResponse {
        return ReverseGeocodingResponse(
            features = listOf(
                ReverseGeocodingResponse.Feature(
                    placeName = "4 Turnham Green Terrace Mews, London Borough of Hounslow, W41QU, United Kingdom"
                )
            )
        )
    }
}
