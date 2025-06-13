/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.call.impl.utils

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothHeadset
import android.bluetooth.BluetoothProfile
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import timber.log.Timber
import java.util.concurrent.atomic.AtomicInteger
import kotlin.time.Duration.Companion.seconds

private val scoTimeout = 5.seconds
private const val MAX_SCO_RETRIES = 3

private const val TAG = "LegacyBluetoothAudioHelper"

@Suppress("DEPRECATION")
class LegacyBluetoothAudioHelper(
    private val context: Context,
    private val audioManager: AudioManager,
    private val coroutineScope: CoroutineScope,
) {
    val connectionState = MutableStateFlow<BluetoothHeadsetConnectionState>(BluetoothHeadsetConnectionState.DISCONNECTED)
    private val connectionAttempts = AtomicInteger(0)
    private var bluetoothHeadset: BluetoothHeadset? = null
    private var currentConnectionAttempt: Job? = null
    private var isInitialDisconnectedState = false
    private var registeredReceiver = false

    private val intentFilter = IntentFilter().apply {
        addAction(BluetoothHeadset.ACTION_CONNECTION_STATE_CHANGED)
        addAction(BluetoothHeadset.ACTION_AUDIO_STATE_CHANGED)
        addAction(BluetoothHeadset.ACTION_VENDOR_SPECIFIC_HEADSET_EVENT)
        addAction(BluetoothDevice.ACTION_ACL_CONNECTED)
        addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED)
        addAction(BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED)
        addAction(AudioManager.ACTION_SCO_AUDIO_STATE_UPDATED)
        addAction(AudioManager.ACTION_SCO_AUDIO_STATE_CHANGED)
    }

    fun startConnection() {
        if (connectionAttempts.get() > 0) {
            println("LegacyBluetoothAudioHelper: Connection attempt already in progress, skipping new attempt")
            return
        }

        val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        if (bluetoothAdapter?.getProfileProxy(context, bluetoothHeadsetListener, BluetoothProfile.HEADSET) == false) {
            println("LegacyBluetoothAudioHelper: Failed to get Bluetooth headset profile proxy")
            return
        }

        if (audioManager.isBluetoothScoOn) {
            println("LegacyBluetoothAudioHelper: Bluetooth SCO is already on, not starting a new connection")
            audioManager.stopBluetoothSco()
        }

        connectionAttempts.set(0)

        audioManager.startBluetoothSco()

        connectionState.value = BluetoothHeadsetConnectionState.CONNECTING

        context.registerReceiver(bluetoothHeadsetReceiver, intentFilter)
        registeredReceiver = true

        internalConnectionAttempt()
    }

    private fun internalConnectionAttempt() {
        if (connectionAttempts.get() > MAX_SCO_RETRIES) {
            println("LegacyBluetoothAudioHelper: Max SCO connection attempts reached")
            stopConnection()
            return
        }

        connectionAttempts.addAndGet(1)

        currentConnectionAttempt = coroutineScope.launch(Dispatchers.Main) {
            println("LegacyBluetoothAudioHelper: Starting Bluetooth SCO connection attempt ${connectionAttempts.get()}")
            runCatching {
                withTimeout(scoTimeout) {
                    println("LegacyBluetoothAudioHelper: Attempting to start Bluetooth SCO connection, attempt ${connectionAttempts.get()}")
                    connectionState.first { it == BluetoothHeadsetConnectionState.CONNECTED }
                }
            }.onSuccess {
                currentConnectionAttempt = null
            }.onFailure { error ->
                currentConnectionAttempt = null
                if (error is TimeoutCancellationException) {
                    if (connectionState.value == BluetoothHeadsetConnectionState.CONNECTED) {
                        println("LegacyBluetoothAudioHelper: Bluetooth SCO connection already established, skipping retry")
                    } else {
                        println("LegacyBluetoothAudioHelper: Bluetooth SCO connection timed out, retrying...")
                        internalConnectionAttempt()
                    }
                } else {
                    Timber.tag(TAG).e(error, "Failed to start Bluetooth SCO connection")
                    connectionState.value = BluetoothHeadsetConnectionState.ERROR
                }
            }
        }
    }

    fun stopConnection() {
        connectionState.value = BluetoothHeadsetConnectionState.DISCONNECTED

        connectionAttempts.set(0)

        if (registeredReceiver) {
            registeredReceiver = false
            context.unregisterReceiver(bluetoothHeadsetReceiver)
        }

        audioManager.stopBluetoothSco()
        if (audioManager.isBluetoothScoOn) {
            audioManager.isBluetoothScoOn = false
        }

        bluetoothHeadset?.let {
            BluetoothAdapter.getDefaultAdapter().closeProfileProxy(BluetoothProfile.HEADSET, it)
        }
        bluetoothHeadset = null

        audioManager.mode = AudioManager.MODE_NORMAL

        currentConnectionAttempt?.cancel()
    }

    private val bluetoothHeadsetListener = object : BluetoothProfile.ServiceListener {
        override fun onServiceConnected(profile: Int, proxy: BluetoothProfile?) {
            bluetoothHeadset = proxy as? BluetoothHeadset
        }

        override fun onServiceDisconnected(profile: Int) {
            bluetoothHeadset = null
        }
    }

    private val bluetoothHeadsetReceiver = object : BroadcastReceiver() {
        @SuppressLint("MissingPermission")
        override fun onReceive(context: Context, intent: Intent) {
            println("LegacyBluetoothAudioHelper: Bluetooth headset event received: ${intent.action} - ${intent.extras?.keySet()?.joinToString(", ")}")
            when (intent.action) {
                BluetoothHeadset.ACTION_CONNECTION_STATE_CHANGED -> {
                    val headsetConnectionState = intent.getIntExtra(BluetoothHeadset.EXTRA_STATE, BluetoothHeadset.STATE_DISCONNECTED)
                    coroutineScope.launch(Dispatchers.Main) {
                        if (connectionState.value != BluetoothHeadsetConnectionState.DISCONNECTED) {
                            when (headsetConnectionState) {
                                BluetoothHeadset.STATE_CONNECTED -> {
                                    if (connectionState.value != BluetoothHeadsetConnectionState.CONNECTED) {
                                        println("LegacyBluetoothAudioHelper: Bluetooth headset connected")
                                        connectionAttempts.set(0)
                                        connectionState.value = BluetoothHeadsetConnectionState.CONNECTED

                                        audioManager.isBluetoothScoOn = true
                                        audioManager.mode = AudioManager.MODE_IN_COMMUNICATION

                                        currentConnectionAttempt?.cancel()
                                        currentConnectionAttempt = null
                                    }
                                }
                                BluetoothHeadset.STATE_DISCONNECTED -> {
                                    println("LegacyBluetoothAudioHelper: Bluetooth headset disconnected")
                                    stopConnection()
                                }
                            }
                            currentConnectionAttempt?.cancel()
                        }
                    }
                }
                BluetoothHeadset.ACTION_AUDIO_STATE_CHANGED -> {
                    val currentConnectionState = intent.getIntExtra(BluetoothHeadset.EXTRA_STATE, -1)
                    val prevConnectionState = intent.getIntExtra(BluetoothHeadset.EXTRA_PREVIOUS_STATE, -1)
                    val bluetoothAudioDevice = intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                    println("LegacyBluetoothAudioHelper: $bluetoothAudioDevice audio state changed from $prevConnectionState to $currentConnectionState")
                    if (prevConnectionState == BluetoothHeadset.STATE_AUDIO_CONNECTED
                        && currentConnectionState == BluetoothHeadset.STATE_AUDIO_DISCONNECTED
                        && bluetoothHeadset?.getConnectionState(bluetoothAudioDevice) == BluetoothProfile.STATE_CONNECTED) {
                        stopConnection()
                    } else {
                        val audioState = intent.getIntExtra(BluetoothHeadset.EXTRA_STATE, BluetoothHeadset.STATE_AUDIO_DISCONNECTED)
                        when (audioState) {
                            BluetoothHeadset.STATE_AUDIO_CONNECTED -> {
                                if (connectionState.value != BluetoothHeadsetConnectionState.CONNECTED) {
                                    println("LegacyBluetoothAudioHelper: Bluetooth audio connected")
                                    connectionAttempts.set(0)
                                    connectionState.value = BluetoothHeadsetConnectionState.CONNECTED

                                    audioManager.isBluetoothScoOn = true
                                    audioManager.mode = AudioManager.MODE_IN_COMMUNICATION

                                    currentConnectionAttempt?.cancel()
                                    currentConnectionAttempt = null
                                }
                            }
                            BluetoothHeadset.STATE_AUDIO_CONNECTING -> {
                                println("LegacyBluetoothAudioHelper: Bluetooth audio connecting")
                            }
                            BluetoothHeadset.STATE_AUDIO_DISCONNECTED -> {
                                println("LegacyBluetoothAudioHelper: Bluetooth audio disconnected")
                                if (!isInitialDisconnectedState) {
                                    connectionState.value = BluetoothHeadsetConnectionState.DISCONNECTED
                                }
                            }
                            else -> println("LegacyBluetoothAudioHelper: Unknown Bluetooth audio state: $audioState")
                        }
                    }
                }
                AudioManager.ACTION_SCO_AUDIO_STATE_UPDATED -> {
                    val scoState = intent.getIntExtra(AudioManager.EXTRA_SCO_AUDIO_STATE, -1)
                    val prevScoState = intent.getIntExtra(AudioManager.EXTRA_SCO_AUDIO_PREVIOUS_STATE, -1)
                    println("LegacyBluetoothAudioHelper: SCO audio state changed from $prevScoState to $scoState")
                    if (prevScoState == AudioManager.SCO_AUDIO_STATE_CONNECTED && scoState == AudioManager.SCO_AUDIO_STATE_DISCONNECTED) {
                        println("LegacyBluetoothAudioHelper: SCO audio disconnected")
                        connectionState.value = BluetoothHeadsetConnectionState.DISCONNECTED
                    } else if (prevScoState == AudioManager.SCO_AUDIO_STATE_CONNECTING && scoState == AudioManager.SCO_AUDIO_STATE_CONNECTED) {
                        println("LegacyBluetoothAudioHelper: SCO audio connected")
                        if (connectionState.value != BluetoothHeadsetConnectionState.CONNECTED) {
                            connectionState.value = BluetoothHeadsetConnectionState.CONNECTED

                            audioManager.isBluetoothScoOn = true
                            audioManager.mode = AudioManager.MODE_IN_COMMUNICATION

                            connectionAttempts.set(0)

                            currentConnectionAttempt?.cancel()
                            currentConnectionAttempt = null
                        }
                    }
                }
            }
        }
    }
}

enum class BluetoothHeadsetConnectionState {
    CONNECTED,
    DISCONNECTED,
    CONNECTING,
    ERROR;

    companion object {
        fun fromAudioState(state: Int): BluetoothHeadsetConnectionState? {
            return when (state) {
                AudioManager.SCO_AUDIO_STATE_CONNECTED -> CONNECTED
                AudioManager.SCO_AUDIO_STATE_CONNECTING -> CONNECTING
                AudioManager.SCO_AUDIO_STATE_DISCONNECTED -> DISCONNECTED
                AudioManager.SCO_AUDIO_STATE_ERROR -> ERROR
                else -> null
            }
        }
    }
}
