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
        0 -> EmojiResource(R.drawable.ic_verification_dog, R.string.verification_emoji_00)
        1 -> EmojiResource(R.drawable.ic_verification_cat, R.string.verification_emoji_01)
        2 -> EmojiResource(R.drawable.ic_verification_lion, R.string.verification_emoji_02)
        3 -> EmojiResource(R.drawable.ic_verification_horse, R.string.verification_emoji_03)
        4 -> EmojiResource(R.drawable.ic_verification_unicorn, R.string.verification_emoji_04)
        5 -> EmojiResource(R.drawable.ic_verification_pig, R.string.verification_emoji_05)
        6 -> EmojiResource(R.drawable.ic_verification_elephant, R.string.verification_emoji_06)
        7 -> EmojiResource(R.drawable.ic_verification_rabbit, R.string.verification_emoji_07)
        8 -> EmojiResource(R.drawable.ic_verification_panda, R.string.verification_emoji_08)
        9 -> EmojiResource(R.drawable.ic_verification_rooster, R.string.verification_emoji_09)
        10 -> EmojiResource(R.drawable.ic_verification_penguin, R.string.verification_emoji_10)
        11 -> EmojiResource(R.drawable.ic_verification_turtle, R.string.verification_emoji_11)
        12 -> EmojiResource(R.drawable.ic_verification_fish, R.string.verification_emoji_12)
        13 -> EmojiResource(R.drawable.ic_verification_octopus, R.string.verification_emoji_13)
        14 -> EmojiResource(R.drawable.ic_verification_butterfly, R.string.verification_emoji_14)
        15 -> EmojiResource(R.drawable.ic_verification_flower, R.string.verification_emoji_15)
        16 -> EmojiResource(R.drawable.ic_verification_tree, R.string.verification_emoji_16)
        17 -> EmojiResource(R.drawable.ic_verification_cactus, R.string.verification_emoji_17)
        18 -> EmojiResource(R.drawable.ic_verification_mushroom, R.string.verification_emoji_18)
        19 -> EmojiResource(R.drawable.ic_verification_globe, R.string.verification_emoji_19)
        20 -> EmojiResource(R.drawable.ic_verification_moon, R.string.verification_emoji_20)
        21 -> EmojiResource(R.drawable.ic_verification_cloud, R.string.verification_emoji_21)
        22 -> EmojiResource(R.drawable.ic_verification_fire, R.string.verification_emoji_22)
        23 -> EmojiResource(R.drawable.ic_verification_banana, R.string.verification_emoji_23)
        24 -> EmojiResource(R.drawable.ic_verification_apple, R.string.verification_emoji_24)
        25 -> EmojiResource(R.drawable.ic_verification_strawberry, R.string.verification_emoji_25)
        26 -> EmojiResource(R.drawable.ic_verification_corn, R.string.verification_emoji_26)
        27 -> EmojiResource(R.drawable.ic_verification_pizza, R.string.verification_emoji_27)
        28 -> EmojiResource(R.drawable.ic_verification_cake, R.string.verification_emoji_28)
        29 -> EmojiResource(R.drawable.ic_verification_heart, R.string.verification_emoji_29)
        30 -> EmojiResource(R.drawable.ic_verification_smiley, R.string.verification_emoji_30)
        31 -> EmojiResource(R.drawable.ic_verification_robot, R.string.verification_emoji_31)
        32 -> EmojiResource(R.drawable.ic_verification_hat, R.string.verification_emoji_32)
        33 -> EmojiResource(R.drawable.ic_verification_glasses, R.string.verification_emoji_33)
        34 -> EmojiResource(R.drawable.ic_verification_spanner, R.string.verification_emoji_34)
        35 -> EmojiResource(R.drawable.ic_verification_santa, R.string.verification_emoji_35)
        36 -> EmojiResource(R.drawable.ic_verification_thumbs_up, R.string.verification_emoji_36)
        37 -> EmojiResource(R.drawable.ic_verification_umbrella, R.string.verification_emoji_37)
        38 -> EmojiResource(R.drawable.ic_verification_hourglass, R.string.verification_emoji_38)
        39 -> EmojiResource(R.drawable.ic_verification_clock, R.string.verification_emoji_39)
        40 -> EmojiResource(R.drawable.ic_verification_gift, R.string.verification_emoji_40)
        41 -> EmojiResource(R.drawable.ic_verification_light_bulb, R.string.verification_emoji_41)
        42 -> EmojiResource(R.drawable.ic_verification_book, R.string.verification_emoji_42)
        43 -> EmojiResource(R.drawable.ic_verification_pencil, R.string.verification_emoji_43)
        44 -> EmojiResource(R.drawable.ic_verification_paperclip, R.string.verification_emoji_44)
        45 -> EmojiResource(R.drawable.ic_verification_scissors, R.string.verification_emoji_45)
        46 -> EmojiResource(R.drawable.ic_verification_lock, R.string.verification_emoji_46)
        47 -> EmojiResource(R.drawable.ic_verification_key, R.string.verification_emoji_47)
        48 -> EmojiResource(R.drawable.ic_verification_hammer, R.string.verification_emoji_48)
        49 -> EmojiResource(R.drawable.ic_verification_phone, R.string.verification_emoji_49)
        50 -> EmojiResource(R.drawable.ic_verification_flag, R.string.verification_emoji_50)
        51 -> EmojiResource(R.drawable.ic_verification_train, R.string.verification_emoji_51)
        52 -> EmojiResource(R.drawable.ic_verification_bicycle, R.string.verification_emoji_52)
        53 -> EmojiResource(R.drawable.ic_verification_aeroplane, R.string.verification_emoji_53)
        54 -> EmojiResource(R.drawable.ic_verification_rocket, R.string.verification_emoji_54)
        55 -> EmojiResource(R.drawable.ic_verification_trophy, R.string.verification_emoji_55)
        56 -> EmojiResource(R.drawable.ic_verification_ball, R.string.verification_emoji_56)
        57 -> EmojiResource(R.drawable.ic_verification_guitar, R.string.verification_emoji_57)
        58 -> EmojiResource(R.drawable.ic_verification_trumpet, R.string.verification_emoji_58)
        59 -> EmojiResource(R.drawable.ic_verification_bell, R.string.verification_emoji_59)
        60 -> EmojiResource(R.drawable.ic_verification_anchor, R.string.verification_emoji_60)
        61 -> EmojiResource(R.drawable.ic_verification_headphones, R.string.verification_emoji_61)
        62 -> EmojiResource(R.drawable.ic_verification_folder, R.string.verification_emoji_62)
        /* 63 */ else -> EmojiResource(R.drawable.ic_verification_pin, R.string.verification_emoji_63)
    }
}
