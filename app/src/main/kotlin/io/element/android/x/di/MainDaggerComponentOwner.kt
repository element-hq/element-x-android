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

package io.element.android.x.di

import android.content.Context
import io.element.android.libraries.di.AppScope
import io.element.android.libraries.di.ApplicationContext
import io.element.android.libraries.di.DaggerComponentOwner
import io.element.android.libraries.di.SingleIn
import javax.inject.Inject

@SingleIn(AppScope::class)
class MainDaggerComponentOwner @Inject constructor(@ApplicationContext context: Context) : DaggerComponentOwner {

    private val daggerComponents = LinkedHashMap<String, Any>().apply {
        put("app", (context as DaggerComponentOwner).daggerComponent)
    }

    fun addComponent(identifier: String, component: Any) {
        daggerComponents[identifier] = component
    }

    fun removeComponent(identifier: String) {
        daggerComponents.remove(identifier)
    }

    override val daggerComponent: Any
        get() = daggerComponents.values.reversed()
}
