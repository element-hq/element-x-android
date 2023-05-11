Splits a Room's member list in 2 showing pending invitees first and then the actual room member.

This simple user facing change entails a host of under the hood changes:
- It copies the logic from the `userlist` module and merges it into the `roomdetails` module removing all details not related to the member list (e.g. gets rid of multiple selection, debouncing etc.).
- Uncouples the `roomdetails` module from the `userlist` one. Now leaving only the `createroom` module to depend on the `userlist` module. Therefore the `userlist` module could be in the future completely removed and merged into the `createroom` module.
 - Changes the room members count in the room details screen to only show the members who have joined (i.e. don't count those still in the invited state).


Parent issue:
- https://github.com/vector-im/element-x-android/issues/246
