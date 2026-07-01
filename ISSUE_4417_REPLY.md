<!-- Reply to mxandreas on element-hq/element-x-android#4417 -->

Thanks, glad it looks good!

Agreed on making it the default with no setting, I'll drop the toggle and have it always on.

On showing who removed the messages: I agree that's nicer, and you're right that showing the original authors can be misleading, it reads as if they removed their own messages when it might have been a moderator. The catch is that the redacter doesn't seem to be available to the app right now. As far as I can tell the Rust SDK gives a redaction as a bare `MsgLikeKind.Redacted` with no `redacted_because` / redacter, so unlike Web we just can't tell who did the removal.

So for a first version I'd keep the header to a plain count, like "11 deleted messages", with no names or avatars. That sidesteps the misleading attribution and still gets rid of the wall. Worth noting the timeline doesn't show who removed a message today either, each "Message removed" just sits under its original sender, so count-only doesn't lose anything that's shown now, it only tidies up the clutter. Once the SDK exposes the redacter we can show "removed by X" like Web, and this header is the obvious place for it.

For what it's worth, I personally lean towards showing a bit more (in my own build I went with author names, small avatars and a toggle, and I like it), but I don't want to argue the point with the team. I'm happy to go with count-only and default-on as you prefer.

Does count-only work for you as a v1? And would you be open to the SDK change later so we can show who removed them?

On iOS: I'd keep this PR to Android for now, that's where I'm working. I can't pick up the iOS side myself, but the logic is small and easy to port, so I'm happy to write up the approach for whoever works on that app.
