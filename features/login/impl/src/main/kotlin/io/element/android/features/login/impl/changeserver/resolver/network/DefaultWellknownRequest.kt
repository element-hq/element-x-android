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
package io.element.android.features.login.impl.changeserver.resolver.network

import com.squareup.anvil.annotations.ContributesBinding
import io.element.android.libraries.di.AppScope
import io.element.android.libraries.network.RetrofitFactory
import javax.inject.Inject

@ContributesBinding(AppScope::class)
class DefaultWellknownRequest @Inject constructor(
    private val retrofitFactory: RetrofitFactory,
) : WellknownRequest {
    /**
     * Return the WellKnown data, if found.
     * @param baseUrl for instance https://matrix.org
     */
    override suspend fun execute(baseUrl: String): WellKnown {
        val wellknownApi = retrofitFactory.create(baseUrl)
            .create(WellknownAPI::class.java)
        return wellknownApi.getWellKnown()
    }
}
