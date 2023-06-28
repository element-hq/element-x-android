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

package io.element.android.features.location.api

import io.element.android.features.location.api.internal.API_KEY
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import retrofit2.http.GET
import retrofit2.http.Path

/**
 * https://docs.maptiler.com/cloud/api/geocoding/#search-by-coordinates-reverse
 *
 * @deprecated It's still undecided whether we want to use this API or not.
 *  So for the moment it's marked as deprecated to discourage usages.
 *  We'll either remove the annotation or delete this code as soon as we've made a decision.
 */
@Deprecated("Please don't use this. It might be removed soon.")
interface GeocodingApi {
    @GET("/geocoding/{lon},{lat}.json?types=address&limit=1&key=$API_KEY")
    suspend fun reverseGeocoding(
        @Path("lat") lat: Double,
        @Path("lon") lon: Double,
    ): ReverseGeocodingResponse
}

@Serializable
data class ReverseGeocodingResponse(
    @SerialName("features") val features: List<Feature>,
) {
    @Serializable
    data class Feature(
        @SerialName("place_name") val placeName: String
    )
}

fun ReverseGeocodingResponse.firstPlaceName(): String? = features.firstOrNull()?.placeName
