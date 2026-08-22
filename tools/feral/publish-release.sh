#!/usr/bin/env bash
# Feral Android release publisher — run on the BUILD/SIGNING machine (eheyu) after
# signing the APKs (tools/feral/sign-release.sh, or a signed gradle build). Generates
# the update-channel manifests and (optionally) deploys everything to feralisme.fr.
# See docs/FERAL_MAINTENANCE.md §11.
#
# What it produces in --out (default: <apk-dir>/publish):
#   Feral-<ver>[-abi].apk           (copied)
#   Feral-<ver>[-abi].apk.sha256    (sha256sum sidecars, used by the member page)
#   update.json                     (read by the in-app updater)
#   version.json                    (read by the member download page on feralisme.fr)
#   latest.json                     (legacy pointer)
#
# Deploy layout on the VPS (both are kept in sync):
#   /var/www/html/feralism/media/downloads/android/   -> PUBLIC (nginx /media/), updater
#   /var/www/html/feralism/protected_downloads/       -> member download page
#
# IMPORTANT: upload the APKs BEFORE update.json (the manifest must never point to a
# file that is not fully uploaded yet). This script deploys in that order.
set -euo pipefail

usage() {
    cat >&2 <<'USAGE'
Usage: publish-release.sh --version <versionName> --apk-dir <dir> [--out <dir>]
                          [--deploy user@host] [--changelog-fr "..."] [--changelog-en "..."]
Example:
  ./tools/feral/publish-release.sh --version 26.08.0 --apk-dir ~/feral-rel/26.08.0/signed \
      --changelog-fr "Appels réparés" --changelog-en "Calls fixed" \
      --deploy loic_feral@172.232.45.124
USAGE
    exit "${1:-1}"
}
need() { [ $# -ge 2 ] && [ -n "$2" ] || { echo "ERROR: $1 needs a value" >&2; usage; }; }

VERSION="" APK_DIR="" OUT="" DEPLOY="" CHANGELOG_FR="" CHANGELOG_EN=""
while [ $# -gt 0 ]; do
    case "$1" in
        --version) need "$@"; VERSION="$2"; shift 2 ;;
        --apk-dir) need "$@"; APK_DIR="$2"; shift 2 ;;
        --out) need "$@"; OUT="$2"; shift 2 ;;
        --deploy) need "$@"; DEPLOY="$2"; shift 2 ;;
        --changelog-fr) CHANGELOG_FR="${2:-}"; shift 2 ;;
        --changelog-en) CHANGELOG_EN="${2:-}"; shift 2 ;;
        -h|--help) usage 0 ;;
        *) echo "ERROR: unknown argument '$1'" >&2; usage ;;
    esac
done
[ -n "$VERSION" ] && [ -n "$APK_DIR" ] || usage
[[ "$VERSION" =~ ^[0-9]{2}\.[0-9]{2}\.[0-9]{1,2}$ ]] \
    || { echo "ERROR: --version must look like YY.MM.N as in Versions.kt (got '$VERSION')" >&2; exit 1; }
[ -d "$APK_DIR" ] || { echo "ERROR: $APK_DIR is not a directory" >&2; exit 1; }
OUT="${OUT:-$APK_DIR/publish}"
BASE_URL="https://feralisme.fr/media/downloads/android"
MEDIA_DIR="/var/www/html/feralism/media/downloads/android"
PROTECTED_DIR="/var/www/html/feralism/protected_downloads"
command -v python3 >/dev/null || { echo "ERROR: python3 is required (JSON encoding)" >&2; exit 1; }

# --- locate aapt2 (versionCode / minSdk extraction) ---------------------------------
find_aapt() {
    command -v aapt2 && return
    command -v aapt && return
    local sdk
    for sdk in "${ANDROID_HOME:-}" "${ANDROID_SDK_ROOT:-}" "$HOME/Android/Sdk" "$HOME/Library/Android/sdk" /opt/android-sdk; do
        [ -n "$sdk" ] && [ -d "$sdk/build-tools" ] || continue
        ls "$sdk"/build-tools/*/aapt2 2>/dev/null | sort -V | tail -1 && return
    done
    return 1
}
AAPT="$(find_aapt || true)"
[ -n "$AAPT" ] || { echo "ERROR: aapt2 not found (install Android SDK build-tools or export ANDROID_HOME)" >&2; exit 1; }

apk_version_code() {
    "$AAPT" dump badging "$1" | sed -n "s/^package:.*versionCode='\([0-9]*\)'.*$/\1/p" | head -1
}
apk_version_name() {
    "$AAPT" dump badging "$1" | sed -n "s/^package:.*versionName='\([^']*\)'.*$/\1/p" | head -1
}
apk_min_sdk() {
    "$AAPT" dump badging "$1" | sed -n "s/^sdkVersion:'\([0-9]*\)'.*$/\1/p" | head -1
}
android_version_for_sdk() {
    case "$1" in
        24) echo "7.0" ;; 25) echo "7.1" ;; 26) echo "8.0" ;; 27) echo "8.1" ;; 28) echo "9" ;;
        29) echo "10" ;; 30) echo "11" ;; 31) echo "12" ;; 32) echo "12L" ;; 33) echo "13" ;;
        34) echo "14" ;; 35) echo "15" ;; 36) echo "16" ;; 37) echo "17" ;; *) echo "API $1" ;;
    esac
}
apk_abi() {
    # From the Feral naming convention Feral-<ver>[-<abi>].apk
    case "$(basename "$1")" in
        *-arm64-v8a.apk) echo "arm64-v8a" ;;
        *-armeabi-v7a.apk) echo "armeabi-v7a" ;;
        *-x86_64.apk) echo "x86_64" ;;
        *-x86.apk) echo "x86" ;;
        *.apk) echo "universal" ;;
    esac
}
# JSON string literal (with quotes) — a real encoder, so newlines/backslashes/quotes
# in changelogs can never produce an invalid manifest (= silently dead update channel).
json_str() { python3 -c 'import json,sys; print(json.dumps(sys.argv[1], ensure_ascii=False))' "$1"; }

mkdir -p "$OUT"
shopt -s nullglob
APKS=("$APK_DIR"/Feral-"$VERSION"*.apk)
if [ ${#APKS[@]} -eq 0 ]; then
    # Fall back to raw gradle output names: rename into the Feral convention first.
    for f in "$APK_DIR"/*.apk; do
        abi=$(basename "$f" | grep -oE 'arm64-v8a|armeabi-v7a|x86_64|x86' | head -1 || true)
        cp -f "$f" "$OUT/Feral-$VERSION${abi:+-$abi}.apk"
    done
else
    # The freshly signed APK always wins over a stale copy from a previous run.
    for f in "${APKS[@]}"; do
        dest="$OUT/$(basename "$f")"
        [ "$f" -ef "$dest" ] || cp -f "$f" "$dest"
    done
fi
rm -f "$OUT"/Feral-"$VERSION"*.apk.sha256
APKS=("$OUT"/Feral-"$VERSION"*.apk)
[ ${#APKS[@]} -gt 0 ] || { echo "ERROR: no APKs found for version $VERSION in $APK_DIR" >&2; exit 1; }

# --- sidecars + manifest entries ---------------------------------------------------
APKS_JSON=""
UNIVERSAL_ENTRY=""
UNIVERSAL_VCODE=""
MIN_SDK=""
for f in "${APKS[@]}"; do
    name=$(basename "$f")
    sha=$(sha256sum "$f" | awk '{print $1}')
    size=$(stat -c%s "$f" 2>/dev/null || stat -f%z "$f")
    vcode=$(apk_version_code "$f")
    vname=$(apk_version_name "$f")
    abi=$(apk_abi "$f")
    [ -n "$vcode" ] || { echo "ERROR: could not read versionCode from $name" >&2; exit 1; }
    [ "$vname" = "$VERSION" ] || { echo "ERROR: $name is versionName '$vname', not --version $VERSION" >&2; exit 1; }
    ( cd "$(dirname "$f")" && sha256sum "$name" > "$name.sha256" )
    entry="\"$abi\": { \"url\": \"$BASE_URL/$name\", \"sha256\": \"$sha\", \"versionCode\": $vcode, \"size\": $size }"
    APKS_JSON="${APKS_JSON:+$APKS_JSON,
    }$entry"
    if [ "$abi" = "universal" ]; then
        UNIVERSAL_ENTRY="{ \"filename\": \"$name\", \"url\": \"$BASE_URL/$name\", \"sha256\": \"$sha\", \"size\": $size }"
        UNIVERSAL_VCODE="$vcode"
        MIN_SDK=$(apk_min_sdk "$f")
    fi
    echo "  $abi  versionCode=$vcode  sha256=${sha:0:16}…  $name"
done
NOTES_FR=$(json_str "$CHANGELOG_FR")
NOTES_EN=$(json_str "$CHANGELOG_EN")
MIN_ANDROID=$(json_str "$(android_version_for_sdk "${MIN_SDK:-24}")")

# --- update.json (read by the in-app updater) --------------------------------------
cat > "$OUT/update.json" <<EOF2
{
  "schema": 1,
  "versionName": "$VERSION",
  "minVersionCode": 0,
  "apks": {
    $APKS_JSON
  },
  "notes": { "fr": $NOTES_FR, "en": $NOTES_EN }
}
EOF2

# --- version.json + latest.json (member download page compatibility) ---------------
NOW=$(date -u +%Y-%m-%dT%H:%M:%SZ)
cat > "$OUT/version.json" <<EOF2
{
  "version": "$VERSION",
  "versionCode": ${UNIVERSAL_VCODE:-0},
  "releaseDate": "$NOW",
  "download": ${UNIVERSAL_ENTRY:-null},
  "requirements": { "minAndroidVersion": $MIN_ANDROID },
  "changelog": { "fr": $NOTES_FR, "en": $NOTES_EN }
}
EOF2
cat > "$OUT/latest.json" <<EOF2
{ "version": "$VERSION", "versionCode": ${UNIVERSAL_VCODE:-0}, "downloadUrl": "$BASE_URL/Feral-$VERSION.apk" }
EOF2
for m in update.json version.json latest.json; do
    python3 -m json.tool "$OUT/$m" >/dev/null || { echo "ERROR: generated $m is not valid JSON" >&2; exit 1; }
done
[ -n "$UNIVERSAL_ENTRY" ] || echo "WARNING: no universal APK (Feral-$VERSION.apk): the member page will have nothing to offer" >&2

echo
echo "Manifests written to $OUT"

# --- deploy ------------------------------------------------------------------------
if [ -n "$DEPLOY" ]; then
    echo "Deploying to $DEPLOY…"
    # 1. binaries + sidecars first (public dir + member dir)
    scp "$OUT"/Feral-"$VERSION"*.apk "$OUT"/Feral-"$VERSION"*.apk.sha256 "$DEPLOY:$MEDIA_DIR/"
    scp "$OUT"/Feral-"$VERSION"*.apk "$OUT"/Feral-"$VERSION"*.apk.sha256 "$DEPLOY:$PROTECTED_DIR/"
    # 2. manifests LAST (atomicity of the update channel)
    scp "$OUT"/version.json "$OUT"/latest.json "$DEPLOY:$PROTECTED_DIR/"
    scp "$OUT"/update.json "$OUT"/version.json "$OUT"/latest.json "$DEPLOY:$MEDIA_DIR/"
    echo "Deployed. Sanity check:"
    echo "  curl -s $BASE_URL/update.json | head"
fi
