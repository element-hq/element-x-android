/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.tests.konsist

import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.google.common.truth.Truth.assertThat
import com.lemonappdev.konsist.api.Konsist
import com.lemonappdev.konsist.api.ext.list.withAllAnnotationsOf
import com.lemonappdev.konsist.api.ext.list.withName
import com.lemonappdev.konsist.api.ext.list.withNameEndingWith
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
    fun `Check functions with 'A11yPreview'`() {
        Konsist
            .scopeFromProject()
            .functions()
            .withNameEndingWith("A11yPreview")
            .assertTrue(
                additionalMessage = "Functions with 'A11yPreview' suffix should have '@Preview' annotation and not '@PreviewsDayNight'," +
                    " should contain 'ElementPreview' composable," +
                    " should contain the tested view" +
                    " and should be internal."
            ) {
                val testedView = it.name.removeSuffix("A11yPreview")
                it.text.contains("$testedView(") &&
                    it.hasAllAnnotationsOf(PreviewsDayNight::class).not() &&
                    it.text.contains("ElementPreview") &&
                    it.hasInternalModifier
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

    private val previewNameExceptions = listOf(
        "AsyncIndicatorFailurePreview",
        "AsyncIndicatorLoadingPreview",
        "BackgroundVerticalGradientDisabledPreview",
        "BackgroundVerticalGradientPreview",
        "ColorAliasesPreview",
        "FocusedEventPreview",
        "GradientFloatingActionButtonCircleShapePreview",
        "HeaderFooterPageScrollablePreview",
        "HomeTopBarMultiAccountPreview",
        "HomeTopBarWithIndicatorPreview",
        "IconsOtherPreview",
        "MarkdownTextComposerEditPreview",
        "MatrixBadgeAtomInfoPreview",
        "MatrixBadgeAtomNegativePreview",
        "MatrixBadgeAtomNeutralPreview",
        "MatrixBadgeAtomPositivePreview",
        "MentionSpanThemeInTimelinePreview",
        "MessageComposerViewVoicePreview",
        "MessagesReactionButtonAddPreview",
        "MessagesReactionButtonExtraPreview",
        "MessagesViewWithIdentityChangePreview",
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
        "ProgressDialogWithContentPreview",
        "ProgressDialogWithTextAndContentPreview",
        "ReadReceiptBottomSheetPreview",
        "RoomMemberListViewBannedPreview",
        "SasEmojisPreview",
        "SecureBackupSetupViewChangePreview",
        "SelectedUserCannotRemovePreview",
        "SpaceMembersViewNoHeroesPreview",
        "TextComposerAddCaptionPreview",
        "TextComposerCaptionPreview",
        "TextComposerEditCaptionPreview",
        "TextComposerEditNotEncryptedPreview",
        "TextComposerEditPreview",
        "TextComposerFormattingNotEncryptedPreview",
        "TextComposerFormattingPreview",
        "TextComposerLinkDialogCreateLinkPreview",
        "TextComposerLinkDialogCreateLinkWithoutTextPreview",
        "TextComposerLinkDialogEditLinkPreview",
        "TextComposerReplyPreview",
        "TextComposerSimpleNotEncryptedPreview",
        "TextComposerSimplePreview",
        "TextComposerVoiceNotEncryptedPreview",
        "TextComposerVoicePreview",
        "TextFieldDialogWithErrorPreview",
        "TimelineImageWithCaptionRowPreview",
        "TimelineItemEventRowForDirectRoomPreview",
        "TimelineItemEventRowShieldPreview",
        "TimelineItemEventRowTimestampPreview",
        "TimelineItemEventRowUtdPreview",
        "TimelineItemEventRowWithManyReactionsPreview",
        "TimelineItemEventRowWithRRPreview",
        "TimelineItemEventRowWithReplyPreview",
        "TimelineItemEventRowWithThreadSummaryPreview",
        "TimelineItemGroupedEventsRowContentCollapsePreview",
        "TimelineItemGroupedEventsRowContentExpandedPreview",
        "TimelineItemImageViewHideMediaContentPreview",
        "TimelineItemVideoViewHideMediaContentPreview",
        "TimelineItemVoiceViewUnifiedPreview",
        "TimelineVideoWithCaptionRowPreview",
        "TimelineViewMessageShieldPreview",
        "UserAvatarColorsPreview",
        "UserProfileHeaderSectionWithVerificationViolationPreview",
        "VoiceItemViewPlayPreview",
    )

    @Test
    fun `previewNameExceptions is sorted alphabetically`() {
        assertThat(previewNameExceptions.sorted()).isEqualTo(previewNameExceptions)
    }

    @Test
    fun `previewNameExceptions only contains existing functions`() {
        val names = previewNameExceptions.toMutableSet()
        Konsist
            .scopeFromProject()
            .functions()
            .withAllAnnotationsOf(PreviewsDayNight::class)
            .withName(previewNameExceptions)
            .let {
                it.forEach { function ->
                    names.remove(function.name)
                }
            }
        assertThat(names).isEmpty()
    }

    @Test
    fun `Functions with '@PreviewsDayNight' have correct name`() {
        Konsist
            .scopeFromProject()
            .functions()
            .withAllAnnotationsOf(PreviewsDayNight::class)
            .withoutName(previewNameExceptions)
            .assertTrue(
                additionalMessage = "Functions for Preview should be named like this: <ViewUnderPreview>Preview. " +
                    "Exception can be added to the test, for multiple Previews of the same view",
            ) {
                val testedView = if (it.name.endsWith("RtlPreview")) {
                    it.name.removeSuffix("RtlPreview")
                } else {
                    it.name.removeSuffix("Preview")
                }
                it.name.endsWith("Preview") &&
                    (it.text.contains("$testedView(") ||
                        it.text.contains("$testedView {") ||
                        it.text.contains("ContentToPreview("))
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
