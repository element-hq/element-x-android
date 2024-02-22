// This array contains all the required environment variable. When adding a variable, add it here also.
// If a variable is missing, an error will occur.

if (MAESTRO_APP_ID == null) throw "Fatal: missing env variable MAESTRO_APP_ID"
if (MAESTRO_USERNAME == null) throw "Fatal: missing env variable MAESTRO_USERNAME"
if (MAESTRO_PASSWORD == null) throw "Fatal: missing env variable MAESTRO_PASSWORD"
if (MAESTRO_RECOVERY_KEY == null) throw "Fatal: missing env variable MAESTRO_RECOVERY_KEY"
if (MAESTRO_ROOM_NAME == null) throw "Fatal: missing env variable MAESTRO_ROOM_NAME"
if (MAESTRO_INVITEE1_MXID == null) throw "Fatal: missing env variable MAESTRO_INVITEE1_MXID"
if (MAESTRO_INVITEE2_MXID == null) throw "Fatal: missing env variable MAESTRO_INVITEE2_MXID"
