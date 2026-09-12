/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.tests.konsist

import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.google.common.truth.Truth.assertThat
import com.lemonappdev.konsist.api.Konsist
import com.lemonappdev.konsist.api.declaration.KoFunctionDeclaration
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
                    it.hasNameEndingWith("DarkPreview").not() &&
                    it.hasNameEndingWith("BlackPreview").not()
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
                (it.text.contains("$testedView(") || it.text.contains("ContentToPreview(")) &&
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
            // We can't check Enterprise previews because they are in a different repo, and they aren't present for FOSS
            .withoutEnterpriseFunctions()
            .assertTrue {
                it.text.contains("ElementPreview") ||
                    it.text.contains("ElementTimelineItemPreview")
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
        "AvatarPickerSizesPreview",
        "AvatarPickerViewPreview",
        "AvatarPickerViewRtlPreview",
        "BackgroundVerticalGradientDisabledPreview",
        "BackgroundVerticalGradientPreview",
        "ColorAliasesPreview",
        "EmojiItemWithPopupPreview",
        "FocusedEventPreview",
        "GradientFloatingActionButtonCircleShapePreview",
        "HeaderFooterPageScrollablePreview",
        "HomeTopBarMultiAccountPreview",
        "HomeTopBarSpaceFiltersSelectedPreview",
        "HomeTopBarSpacesPreview",
        "HomeTopBarWithIndicatorPreview",
        "HomeTopBarWithStatusPreview",
        "IconsOtherPreview",
        "MarkdownTextComposerEditPreview",
        "MatrixBadgeAtomInfoPreview",
        "MatrixBadgeAtomNegativePreview",
        "MatrixBadgeAtomNeutralPreview",
        "MatrixBadgeAtomNeutralWrappingPreview",
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
        "PollContentViewMultipleSelectionPreview",
        "PollContentViewUndisclosedPreview",
        "ProgressDialogWithContentPreview",
        "ProgressDialogWithTextAndContentPreview",
        "ReadReceiptBottomSheetPreview",
        "SasEmojisPreview",
        "SecureBackupSetupViewChangePreview",
        "SelectedUserCannotRemovePreview",
        "SpaceMembersViewNoHeroesPreview",
        "SyncStateViewServerUnreachablePreview",
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
        "TimelineItemAttachmentsViewScanningContentFailedPreview",
        "TimelineItemAudioViewScanningContentPreview",
        "TimelineItemEventRowForDirectRoomPreview",
        "TimelineItemEventRowShieldPreview",
        "TimelineItemEventRowTimestampPreview",
        "TimelineItemEventRowUtdPreview",
        "TimelineItemEventRowWithGalleryPreview",
        "TimelineItemEventRowWithManyReactionsPreview",
        "TimelineItemEventRowWithRRPreview",
        "TimelineItemEventRowWithReplyPreview",
        "TimelineItemEventRowWithThreadSummaryPreview",
        "TimelineItemFileViewScanningContentPreview",
        "TimelineItemGalleryViewScanningContentFailedPreview",
        "TimelineItemGroupedEventsRowContentCollapsePreview",
        "TimelineItemGroupedEventsRowContentExpandedPreview",
        "TimelineItemImageViewHideMediaContentPreview",
        "TimelineItemImageViewScanningContentPreview",
        "TimelineItemRedactedMessagesGroupPreview",
        "TimelineItemScanningContentFailedPreview",
        "TimelineItemScanningContentNotFoundPreview",
        "TimelineItemScanningContentWithInvalidRepliesPreview",
        "TimelineItemScanningContentWithRepliesFailedPreview",
        "TimelineItemStickerViewScanningContentPreview",
        "TimelineItemVideoViewHideMediaContentPreview",
        "TimelineItemVideoViewScanningContentPreview",
        "TimelineItemVoiceViewScanningContentPreview",
        "TimelineItemVoiceViewUnifiedPreview",
        "TimelineViewMessageShieldPreview",
        "TimelineViewWithJumpBackPreview",
        "TimelineViewWithReadMarkerBothIndicatorsPreview",
        "TimelineViewWithReadMarkerJumpToUnreadIndicatorOnlyPreview",
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
        val names = previewNameExceptions
            .toMutableSet()
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
            // We can't check Enterprise previews because they are in a different repo, and they aren't present for FOSS
            .withoutEnterpriseFunctions()
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

private fun List<KoFunctionDeclaration>.withoutEnterpriseFunctions() = filter { function ->
    function.packagee?.hasNameStartingWith("io.element.android.enterprise") != true
}
