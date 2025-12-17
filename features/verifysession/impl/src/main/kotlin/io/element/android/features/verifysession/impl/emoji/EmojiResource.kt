/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */
package io.element.android.features.verifysession.impl.emoji

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import io.element.android.features.verifysession.impl.R

internal data class EmojiResource(
    @DrawableRes val drawableRes: Int,
    @StringRes val nameRes: Int
)

internal fun Int.toEmojiResource(): EmojiResource {
    return when (this % 64) {
        0 -> EmojiResource(R.drawable.ic_verification_00, R.string.verification_emoji_00)
        1 -> EmojiResource(R.drawable.ic_verification_01, R.string.verification_emoji_01)
        2 -> EmojiResource(R.drawable.ic_verification_02, R.string.verification_emoji_02)
        3 -> EmojiResource(R.drawable.ic_verification_03, R.string.verification_emoji_03)
        4 -> EmojiResource(R.drawable.ic_verification_04, R.string.verification_emoji_04)
        5 -> EmojiResource(R.drawable.ic_verification_05, R.string.verification_emoji_05)
        6 -> EmojiResource(R.drawable.ic_verification_06, R.string.verification_emoji_06)
        7 -> EmojiResource(R.drawable.ic_verification_07, R.string.verification_emoji_07)
        8 -> EmojiResource(R.drawable.ic_verification_08, R.string.verification_emoji_08)
        9 -> EmojiResource(R.drawable.ic_verification_09, R.string.verification_emoji_09)
        10 -> EmojiResource(R.drawable.ic_verification_10, R.string.verification_emoji_10)
        11 -> EmojiResource(R.drawable.ic_verification_11, R.string.verification_emoji_11)
        12 -> EmojiResource(R.drawable.ic_verification_12, R.string.verification_emoji_12)
        13 -> EmojiResource(R.drawable.ic_verification_13, R.string.verification_emoji_13)
        14 -> EmojiResource(R.drawable.ic_verification_14, R.string.verification_emoji_14)
        15 -> EmojiResource(R.drawable.ic_verification_15, R.string.verification_emoji_15)
        16 -> EmojiResource(R.drawable.ic_verification_16, R.string.verification_emoji_16)
        17 -> EmojiResource(R.drawable.ic_verification_17, R.string.verification_emoji_17)
        18 -> EmojiResource(R.drawable.ic_verification_18, R.string.verification_emoji_18)
        19 -> EmojiResource(R.drawable.ic_verification_19, R.string.verification_emoji_19)
        20 -> EmojiResource(R.drawable.ic_verification_20, R.string.verification_emoji_20)
        21 -> EmojiResource(R.drawable.ic_verification_21, R.string.verification_emoji_21)
        22 -> EmojiResource(R.drawable.ic_verification_22, R.string.verification_emoji_22)
        23 -> EmojiResource(R.drawable.ic_verification_23, R.string.verification_emoji_23)
        24 -> EmojiResource(R.drawable.ic_verification_24, R.string.verification_emoji_24)
        25 -> EmojiResource(R.drawable.ic_verification_25, R.string.verification_emoji_25)
        26 -> EmojiResource(R.drawable.ic_verification_26, R.string.verification_emoji_26)
        27 -> EmojiResource(R.drawable.ic_verification_27, R.string.verification_emoji_27)
        28 -> EmojiResource(R.drawable.ic_verification_28, R.string.verification_emoji_28)
        29 -> EmojiResource(R.drawable.ic_verification_29, R.string.verification_emoji_29)
        30 -> EmojiResource(R.drawable.ic_verification_30, R.string.verification_emoji_30)
        31 -> EmojiResource(R.drawable.ic_verification_31, R.string.verification_emoji_31)
        32 -> EmojiResource(R.drawable.ic_verification_32, R.string.verification_emoji_32)
        33 -> EmojiResource(R.drawable.ic_verification_33, R.string.verification_emoji_33)
        34 -> EmojiResource(R.drawable.ic_verification_34, R.string.verification_emoji_34)
        35 -> EmojiResource(R.drawable.ic_verification_35, R.string.verification_emoji_35)
        36 -> EmojiResource(R.drawable.ic_verification_36, R.string.verification_emoji_36)
        37 -> EmojiResource(R.drawable.ic_verification_37, R.string.verification_emoji_37)
        38 -> EmojiResource(R.drawable.ic_verification_38, R.string.verification_emoji_38)
        39 -> EmojiResource(R.drawable.ic_verification_39, R.string.verification_emoji_39)
        40 -> EmojiResource(R.drawable.ic_verification_40, R.string.verification_emoji_40)
        41 -> EmojiResource(R.drawable.ic_verification_41, R.string.verification_emoji_41)
        42 -> EmojiResource(R.drawable.ic_verification_42, R.string.verification_emoji_42)
        43 -> EmojiResource(R.drawable.ic_verification_43, R.string.verification_emoji_43)
        44 -> EmojiResource(R.drawable.ic_verification_44, R.string.verification_emoji_44)
        45 -> EmojiResource(R.drawable.ic_verification_45, R.string.verification_emoji_45)
        46 -> EmojiResource(R.drawable.ic_verification_46, R.string.verification_emoji_46)
        47 -> EmojiResource(R.drawable.ic_verification_47, R.string.verification_emoji_47)
        48 -> EmojiResource(R.drawable.ic_verification_48, R.string.verification_emoji_48)
        49 -> EmojiResource(R.drawable.ic_verification_49, R.string.verification_emoji_49)
        50 -> EmojiResource(R.drawable.ic_verification_50, R.string.verification_emoji_50)
        51 -> EmojiResource(R.drawable.ic_verification_51, R.string.verification_emoji_51)
        52 -> EmojiResource(R.drawable.ic_verification_52, R.string.verification_emoji_52)
        53 -> EmojiResource(R.drawable.ic_verification_53, R.string.verification_emoji_53)
        54 -> EmojiResource(R.drawable.ic_verification_54, R.string.verification_emoji_54)
        55 -> EmojiResource(R.drawable.ic_verification_55, R.string.verification_emoji_55)
        56 -> EmojiResource(R.drawable.ic_verification_56, R.string.verification_emoji_56)
        57 -> EmojiResource(R.drawable.ic_verification_57, R.string.verification_emoji_57)
        58 -> EmojiResource(R.drawable.ic_verification_58, R.string.verification_emoji_58)
        59 -> EmojiResource(R.drawable.ic_verification_59, R.string.verification_emoji_59)
        60 -> EmojiResource(R.drawable.ic_verification_60, R.string.verification_emoji_60)
        61 -> EmojiResource(R.drawable.ic_verification_61, R.string.verification_emoji_61)
        62 -> EmojiResource(R.drawable.ic_verification_62, R.string.verification_emoji_62)
        63 -> EmojiResource(R.drawable.ic_verification_63, R.string.verification_emoji_63)
        else -> error("Cannot happen ($this)!")
    }
}
