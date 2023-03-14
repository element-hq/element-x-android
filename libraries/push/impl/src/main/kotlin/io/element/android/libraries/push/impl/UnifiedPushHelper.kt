/*
 * Copyright (c) 2022 New Vector Ltd
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

package io.element.android.libraries.push.impl

import android.content.Context
import io.element.android.libraries.androidutils.system.getApplicationLabel
import io.element.android.libraries.di.ApplicationContext
import io.element.android.libraries.push.impl.config.PushConfig
import io.element.android.libraries.toolbox.api.strings.StringProvider
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.unifiedpush.android.connector.UnifiedPush
import timber.log.Timber
import java.net.URL
import javax.inject.Inject
import io.element.android.libraries.ui.strings.R as StringR

class UnifiedPushHelper @Inject constructor(
    @ApplicationContext private val context: Context,
    private val unifiedPushStore: UnifiedPushStore,
    // private val matrix: Matrix,
    private val fcmHelper: FcmHelper,
    private val stringProvider: StringProvider,
) {

    /* TODO EAx
    @MainThread
    fun showSelectDistributorDialog(
        context: Context,
        onDistributorSelected: (String) -> Unit,
    ) {
        val internalDistributorName = stringProvider.getString(
            if (fcmHelper.isFirebaseAvailable()) {
                StringR.string.unifiedpush_distributor_fcm_fallback
            } else {
                StringR.string.unifiedpush_distributor_background_sync
            }
        )

        val distributors = UnifiedPush.getDistributors(context)
        val distributorsName = distributors.map {
            if (it == context.packageName) {
                internalDistributorName
            } else {
                context.getApplicationLabel(it)
            }
        }

        MaterialAlertDialogBuilder(context)
            .setTitle(stringProvider.getString(StringR.string.unifiedpush_getdistributors_dialog_title))
            .setItems(distributorsName.toTypedArray()) { _, which ->
                val distributor = distributors[which]
                onDistributorSelected(distributor)
            }
            .setOnCancelListener {
                // we do not want to change the distributor on behalf of the user
                if (UnifiedPush.getDistributor(context).isEmpty()) {
                    // By default, use internal solution (fcm/background sync)
                    onDistributorSelected(context.packageName)
                }
            }
            .setCancelable(true)
            .show()
    }

     */

    @Serializable
    internal data class DiscoveryResponse(
        @SerialName("unifiedpush") val unifiedpush: DiscoveryUnifiedPush = DiscoveryUnifiedPush()
    )

    @Serializable
    internal data class DiscoveryUnifiedPush(
        @SerialName("gateway") val gateway: String = ""
    )

    suspend fun storeCustomOrDefaultGateway(
        endpoint: String,
        onDoneRunnable: Runnable? = null
    ) {
        // if we use the embedded distributor,
        // register app_id type upfcm on sygnal
        // the pushkey if FCM key
        if (UnifiedPush.getDistributor(context) == context.packageName) {
            unifiedPushStore.storePushGateway(PushConfig.pusher_http_url)
            onDoneRunnable?.run()
            return
        }
        /* TODO EAx UnifiedPush
        // else, unifiedpush, and pushkey is an endpoint
        val gateway = PushConfig.default_push_gateway_http_url
        val parsed = URL(endpoint)
        val custom = "${parsed.protocol}://${parsed.host}/_matrix/push/v1/notify"
        Timber.i("Testing $custom")
        try {
            val response = matrix.rawService().getUrl(custom, CacheStrategy.NoCache)
            tryOrNull { Json.decodeFromString<DiscoveryResponse>(response) }
                ?.let { discoveryResponse ->
                    if (discoveryResponse.unifiedpush.gateway == "matrix") {
                        Timber.d("Using custom gateway")
                        unifiedPushStore.storePushGateway(custom)
                        onDoneRunnable?.run()
                        return
                    }
                }
        } catch (e: Throwable) {
            Timber.d(e, "Cannot try custom gateway")
        }
        unifiedPushStore.storePushGateway(gateway)
        onDoneRunnable?.run()

         */
    }

    fun getExternalDistributors(): List<String> {
        return UnifiedPush.getDistributors(context)
            .filterNot { it == context.packageName }
    }

    fun getCurrentDistributorName(): String {
        return when {
            isEmbeddedDistributor() -> stringProvider.getString(StringR.string.unifiedpush_distributor_fcm_fallback)
            isBackgroundSync() -> stringProvider.getString(StringR.string.unifiedpush_distributor_background_sync)
            else -> context.getApplicationLabel(UnifiedPush.getDistributor(context))
        }
    }

    fun isEmbeddedDistributor(): Boolean {
        return isInternalDistributor() && fcmHelper.isFirebaseAvailable()
    }

    fun isBackgroundSync(): Boolean {
        return isInternalDistributor() && !fcmHelper.isFirebaseAvailable()
    }

    private fun isInternalDistributor(): Boolean {
        return UnifiedPush.getDistributor(context).isEmpty() ||
            UnifiedPush.getDistributor(context) == context.packageName
    }

    fun getPrivacyFriendlyUpEndpoint(): String? {
        val endpoint = getEndpointOrToken()
        if (endpoint.isNullOrEmpty()) return null
        if (isEmbeddedDistributor()) {
            return endpoint
        }
        return try {
            val parsed = URL(endpoint)
            "${parsed.protocol}://${parsed.host}/***"
        } catch (e: Exception) {
            Timber.e(e, "Error parsing unifiedpush endpoint")
            null
        }
    }

    fun getEndpointOrToken(): String? {
        return if (isEmbeddedDistributor()) fcmHelper.getFcmToken()
        else unifiedPushStore.getEndpoint()
    }

    fun getPushGateway(): String? {
        return if (isEmbeddedDistributor()) PushConfig.pusher_http_url
        else unifiedPushStore.getPushGateway()
    }
}
