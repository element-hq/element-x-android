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

package io.element.android.appnav.root

import com.bumble.appyx.core.state.MutableSavedStateMap
import com.bumble.appyx.core.state.SavedStateMap
import io.element.android.appnav.di.MatrixClientsHolder
import io.element.android.features.login.api.LoginUserStory
import io.element.android.features.preferences.api.CacheService
import io.element.android.libraries.matrix.api.auth.MatrixAuthenticationService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

private const val SAVE_INSTANCE_KEY = "io.element.android.x.RootNavStateFlowFactory.SAVE_INSTANCE_KEY"

class RootNavStateFlowFactory @Inject constructor(
    private val authenticationService: MatrixAuthenticationService,
    private val cacheService: CacheService,
    private val matrixClientsHolder: MatrixClientsHolder,
    private val loginUserStory: LoginUserStory,
) {

    private var currentCacheIndex = 0

    fun create(savedStateMap: SavedStateMap?): Flow<RootNavState> {
        /**
         * A flow of integer, where each time a clear cache is done, we have a new incremented value.
         */
        val initialCacheIndex = savedStateMap.getCacheIndexOrDefault()
        val cacheIndexFlow = cacheService.clearedCacheEventFlow
            .onEach { sessionId ->
                matrixClientsHolder.remove(sessionId)
            }
            .toIndexFlow(initialCacheIndex)
            .onEach { cacheIndex ->
                currentCacheIndex = cacheIndex
            }

        return combine(
            cacheIndexFlow,
            isUserLoggedInFlow(),
        ) { navId, isLoggedIn ->
            RootNavState(navId, isLoggedIn)
        }
    }

    fun saveIntoSavedState(stateMap: MutableSavedStateMap) {
        stateMap[SAVE_INSTANCE_KEY] = currentCacheIndex
    }

    private fun isUserLoggedInFlow(): Flow<Boolean> {
        return combine(
            authenticationService.isLoggedIn(),
            loginUserStory.loginFlowIsDone
        ) { isLoggedIn, loginFlowIsDone ->
            isLoggedIn && loginFlowIsDone
        }
            .distinctUntilChanged()
    }

    private fun Flow<Any>.toIndexFlow(initialValue: Int): Flow<Int> = flow {
        var index = initialValue
        emit(initialValue)
        collect {
            emit(++index)
        }
    }

    private fun SavedStateMap?.getCacheIndexOrDefault(): Int {
        return this?.get(SAVE_INSTANCE_KEY) as? Int ?: 0
    }
}
