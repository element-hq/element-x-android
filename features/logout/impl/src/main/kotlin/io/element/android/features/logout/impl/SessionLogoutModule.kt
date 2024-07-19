/*
 * Copyright (c) 2024 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.element.android.features.logout.impl

import com.squareup.anvil.annotations.ContributesTo
import dagger.Module
import dagger.Provides
import io.element.android.features.logout.api.LogoutUseCase
import io.element.android.libraries.di.SessionScope
import io.element.android.libraries.matrix.api.user.CurrentSessionIdHolder

@Module
@ContributesTo(SessionScope::class)
object SessionLogoutModule {
     @Provides
     fun provideLogoutUseCase(
         currentSessionIdHolder: CurrentSessionIdHolder,
         factory: DefaultLogoutUseCase.Factory,
     ): LogoutUseCase {
         return factory.create(currentSessionIdHolder.current.value)
     }
}
