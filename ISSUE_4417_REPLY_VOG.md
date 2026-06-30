<!-- Reply to vog on element-hq/element-x-android#4417 -->

@vog

> Would it be feasible to make this configurable? ... day separators could be "swallowed" as well, at least for days which contain nothing but removed messages

Thanks! Good point for notes-style rooms.

For this change I'd lean towards keeping the day separators though. Not swallowing them is intentional: it's what keeps the day dividers (and read receipts) correct, and that's part of why this works without touching the SDK. The direction here is also to ship it as the default with no setting, so I'd rather not add a config flag for it.

Swallowing days that contain only removed messages could be a nice separate follow-up later. I'd just keep it out of this one to stay focused.
