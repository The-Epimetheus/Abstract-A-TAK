# helloworld-unified build recipes.
# One branch, every ATAK version. `just` wraps gradle so building for all
# versions x all flavors (or any slice) is a single command. See versions.json.
# Uses python3 (not jq) to read versions.json — the jq on PATH may be an npm shim.

set shell := ["bash", "-uc"]

_default:
    @just --list

# Show the supported ATAK versions (from versions.json, the single source of truth).
list-versions:
    @python3 -c "import json;[print(f\"{x['version']:<7} {x['flavorId']:<9} {x['jar']:<16} api={x['pluginApi']}\"+('' if x['pluginApiVerified'] else ' (unverified)')) for x in json.load(open('versions.json'))['versions']]"

# Stage each version's ATAK SDK main.jar from HelloWorld_Collection into sdk/<jar>,
# plus TAK's dev keystore into keystore/. Every build depends on this; gradle's
# compileOnly points at sdk/main-<ver>.jar. If you don't have the local
# HelloWorld_Collection mirror, place the jars in sdk/ yourself (see sdk/README.md)
# and copy android_keystore from any ATAK SDK bundle into keystore/.
sync-sdk:
    #!/usr/bin/env bash
    set -euo pipefail
    mkdir -p sdk keystore
    python3 -c "import json;[print(x['sdkJarSource']+'\t'+x['jar']) for x in json.load(open('versions.json'))['versions']]" | \
    while IFS=$'\t' read -r src dst; do
        if [ -f "$src" ]; then cp -f "$src" "sdk/$dst" && echo "staged sdk/$dst"; \
        else echo "MISSING: $src (sdk/$dst not staged)" >&2; fi
    done
    # TAK's well-known dev keystore (ships in every SDK bundle; password tnttnt)
    if [ ! -f keystore/android_keystore ]; then
        ks=$(find HelloWorld_Collection -name android_keystore 2>/dev/null | head -1)
        if [ -n "$ks" ]; then cp -f "$ks" keystore/android_keystore && echo "staged keystore/android_keystore"; \
        else echo "NOTE: keystore/android_keystore not staged — copy it from an ATAK SDK bundle" >&2; fi
    fi

# Build. Both args default to "all".
#   just build            -> all versions x all flavors
#   just build 5.3        -> ATAK 5.3, all flavors
#   just build 5.3 civ    -> ATAK 5.3, CIV only
#   just build all civ    -> all versions, CIV only
# Optional third arg is the build type (debug|release), default debug.
build ver="all" flavor="all" buildtype="debug": sync-sdk
    #!/usr/bin/env bash
    set -euo pipefail
    cap() { local s="$1"; echo "$(tr '[:lower:]' '[:upper:]' <<< ${s:0:1})${s:1}"; }
    ver="{{ver}}"; flavor="{{flavor}}"; buildtype="{{buildtype}}"
    if [ "$ver" = "all" ]; then vseg=""; else
        fid=$(python3 -c "import json,sys;m={x['version']:x['flavorId'] for x in json.load(open('versions.json'))['versions']};print(m.get(sys.argv[1],''))" "$ver")
        if [ -z "$fid" ]; then echo "unknown ATAK version: $ver (see: just list-versions)" >&2; exit 1; fi
        vseg="$(cap "$fid")"
    fi
    if [ "$flavor" = "all" ]; then fseg=""; else fseg="$(cap "$flavor")"; fi
    bt="$(cap "$buildtype")"
    task="assemble${vseg}${fseg}${bt}"
    echo "gradle task: $task"
    ./gradlew "$task"

# Fail if any src/main file imports an ATAK SDK package (the zero-ATAK-in-main boundary).
# NOTE: during incremental migration this WILL report the not-yet-migrated legacy files.
check-boundary:
    #!/usr/bin/env bash
    set -euo pipefail
    hits=$(grep -rnE '^\s*import\s+(com\.atakmap\.|gov\.tak\.|com\.atak\.|transapps\.)' app/src/main/java 2>/dev/null || true)
    if [ -n "$hits" ]; then
        n=$(echo "$hits" | wc -l)
        echo "BOUNDARY: $n ATAK import(s) still in src/main (expected during migration):" >&2
        echo "$hits" | sed 's/^/  /' >&2
        exit 1
    fi
    echo "boundary OK: src/main is ATAK-type-free"
