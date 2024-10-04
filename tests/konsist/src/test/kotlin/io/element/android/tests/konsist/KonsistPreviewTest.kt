/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.tests.konsist

import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.lemonappdev.konsist.api.Konsist
import com.lemonappdev.konsist.api.ext.list.withAllAnnotationsOf
import com.lemonappdev.konsist.api.ext.list.withoutName
import com.lemonappdev.konsist.api.verify.assertEmpty
import com.lemonappdev.konsist.api.verify.assertTrue
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import org.junit.Test

class KonsistPreviewTest {
    @Test
    fun `Functions with '@PreviewsDayNight' annotation should have 'Preview' suffix`() {
        Konsist
            .scopeFromProject()
            .functions()
            .withAllAnnotationsOf(PreviewsDayNight::class)
            .assertTrue {
                it.hasNameEndingWith("Preview") &&
                    it.hasNameEndingWith("LightPreview").not() &&
                    it.hasNameEndingWith("DarkPreview").not()
            }
    }

    @Test
    fun `Functions with '@PreviewsDayNight' annotation should contain 'ElementPreview' composable`() {
        Konsist
            .scopeFromProject()
            .functions()
            .withAllAnnotationsOf(PreviewsDayNight::class)
            .assertTrue {
                it.text.contains("ElementPreview")
            }
    }

    @Test
    fun `Functions with '@PreviewsDayNight' are internal`() {
        Konsist
            .scopeFromProject()
            .functions()
            .withAllAnnotationsOf(PreviewsDayNight::class)
            .assertTrue {
                it.hasInternalModifier
            }
    }

    @Test
    fun `Functions with '@PreviewsDayNight' have correct name`() {
        Konsist
            .scopeFromProject()
            .functions()
            .withAllAnnotationsOf(PreviewsDayNight::class)
            .withoutName(
                "AsyncIndicatorFailurePreview",
                "AsyncIndicatorLoadingPreview",
                "BloomInitialsPreview",
                "BloomPreview",
                "CallScreenPipViewPreview",
                "ColorAliasesPreview",
                "DefaultRoomListTopBarWithIndicatorPreview",
                "GradientFloatingActionButtonCircleShapePreview",
                "IconTitleSubtitleMoleculeWithResIconPreview",
                "IconsCompoundPreview",
                "IconsOtherPreview",
                "MarkdownTextComposerEditPreview",
                "MentionSpanPreview",
                "MessageComposerViewVoicePreview",
                "MessagesReactionButtonAddPreview",
                "MessagesReactionButtonExtraPreview",
                "MessagesViewWithTypingPreview",
                "PageTitleWithIconFullPreview",
                "PageTitleWithIconMinimalPreview",
                "PendingMemberRowWithLongNamePreview",
                "PinUnlockViewInAppPreview",
                "PollAnswerViewDisclosedNotSelectedPreview",
                "PollAnswerViewDisclosedSelectedPreview",
                "PollAnswerViewEndedSelectedPreview",
                "PollAnswerViewEndedWinnerNotSelectedPreview",
                "PollAnswerViewEndedWinnerSelectedPreview",
                "PollAnswerViewUndisclosedNotSelectedPreview",
                "PollAnswerViewUndisclosedSelectedPreview",
                "PollContentViewCreatorEditablePreview",
                "PollContentViewCreatorEndedPreview",
                "PollContentViewCreatorPreview",
                "PollContentViewDisclosedPreview",
                "PollContentViewEndedPreview",
                "PollContentViewUndisclosedPreview",
                "ReadReceiptBottomSheetPreview",
                "RoomBadgePositivePreview",
                "RoomBadgeNeutralPreview",
                "RoomBadgeNegativePreview",
                "RoomMemberListViewBannedPreview",
                "SasEmojisPreview",
                "SecureBackupSetupViewChangePreview",
                "SelectedUserCannotRemovePreview",
                "TextComposerEditPreview",
                "TextComposerFormattingPreview",
                "TextComposerLinkDialogCreateLinkPreview",
                "TextComposerLinkDialogCreateLinkWithoutTextPreview",
                "TextComposerLinkDialogEditLinkPreview",
                "TextComposerReplyPreview",
                "TextComposerSimplePreview",
                "TextComposerVoicePreview",
                "TimelineImageWithCaptionRowPreview",
                "TimelineItemEventRowForDirectRoomPreview",
                "TimelineItemEventRowShieldPreview",
                "TimelineItemEventRowTimestampPreview",
                "TimelineItemEventRowWithManyReactionsPreview",
                "TimelineItemEventRowWithRRPreview",
                "TimelineItemEventRowWithReplyPreview",
                "TimelineItemGroupedEventsRowContentCollapsePreview",
                "TimelineItemGroupedEventsRowContentExpandedPreview",
                "TimelineItemImageViewHideMediaContentPreview",
                "TimelineItemVideoViewHideMediaContentPreview",
                "TimelineItemVoiceViewUnifiedPreview",
                "TimelineVideoWithCaptionRowPreview",
                "TimelineViewMessageShieldPreview",
                "UserAvatarColorsPreview",
            )
            .assertTrue(
                additionalMessage = "Functions for Preview should be named like this: <ViewUnderPreview>Preview. " +
                    "Exception can be added to the test, for multiple Previews of the same view",
            ) {
                val testedView = it.name.removeSuffix("Preview")
                it.text.contains("$testedView(") ||
                    it.text.contains("$testedView {") ||
                    it.text.contains("ContentToPreview(")
            }
    }

    @Test
    fun `Ensure that '@PreviewLightDark' is not used`() {
        Konsist
            .scopeFromProject()
            .functions()
            .withAllAnnotationsOf(PreviewLightDark::class)
            .assertEmpty(
                additionalMessage = "Use '@PreviewsDayNight' instead of '@PreviewLightDark', or else screenshot(s) will not be generated.",
            )
    }
}
