# helloworld-unified build recipes.
# One branch, every ATAK version. `just` wraps gradle so building for all
# versions x all flavors (or any slice) is a single command. See versions.json.
# Uses python3 (not jq) to read versions.json — the jq on PATH may be an npm shim.

set shell := ["bash", "-uc"]

_default:
    @just --list

# Show the supported ATAK versions + which band source sets each compiles
# (both derived from versions.json, the single source of truth).
list-versions:
    #!/usr/bin/env python3
    import json
    d = json.load(open('versions.json'))
    def key(v): return [int(x) for x in v.split('.')]
    for x in d['versions']:
        bands = ' '.join(
            (b['pre'] if key(x['version']) < key(b['firstPlusVersion']) else b['plus'])
            for b in d.get('bandPairs', []))
        flag = '' if x['pluginApiVerified'] else ' (unverified)'
        print(f"{x['version']:<7} {x['flavorId']:<9} api={x['pluginApi']:<8} bands: {bands}{flag}")

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
    # With TWO flavor dimensions there is no single `assembleCivDebug` task — only
    # full per-variant tasks (assembleAtak530CivDebug) and the build-type aggregate
    # (assembleDebug). So expand to an explicit task list from versions.json (all
    # versions) x build.gradle supportedFlavors (all distros), narrowed by the args.
    tasks=$(ATAK_VER="{{ver}}" ATAK_FLAVOR="{{flavor}}" ATAK_BT="{{buildtype}}" python3 - <<'PY'
    import json, os, re
    ver, flavor, bt = os.environ['ATAK_VER'], os.environ['ATAK_FLAVOR'], os.environ['ATAK_BT'].capitalize()
    versions = json.load(open('versions.json'))['versions']
    vmap = {v['version']: v['flavorId'] for v in versions}
    gradle = open('app/build.gradle').read()
    # distro names live in the supportedFlavors list (after that def, before android {)
    block = gradle.split('def supportedFlavors', 1)[1].split('android {', 1)[0]
    distros = re.findall(r"name\s*:\s*'(\w+)'", block)
    if ver == 'all' and flavor == 'all':
        print('assemble' + bt); raise SystemExit
    fids = list(vmap.values()) if ver == 'all' else [vmap.get(ver)]
    if fids == [None]: raise SystemExit("unknown ATAK version: " + ver + " (see: just list-versions)")
    fls = distros if flavor == 'all' else [flavor]
    print(' '.join(f"assemble{f[0].upper()+f[1:]}{d.capitalize()}{bt}" for f in fids for d in fls))
    PY
    )
    echo "gradle tasks: $tasks"
    ./gradlew $tasks

# Fail if any src/main file imports an ATAK SDK package (the zero-ATAK-in-main boundary).
# NOTE: during incremental migration this WILL report the not-yet-migrated legacy files.
check-boundary:
    #!/usr/bin/env bash
    set -euo pipefail
    # The plugin's own package is com.atakmap.android.helloworld.* — those imports
    # are plugin-internal, not ATAK SDK touches, so exclude them from the count.
    hits=$(grep -rnE '^\s*import\s+(com\.atakmap\.|gov\.tak\.|com\.atak\.|transapps\.)' app/src/main/java 2>/dev/null \
        | grep -v 'import\s\+com\.atakmap\.android\.helloworld\.' || true)
    if [ -n "$hits" ]; then
        n=$(echo "$hits" | wc -l)
        echo "BOUNDARY: $n ATAK import(s) still in src/main (expected during migration):" >&2
        echo "$hits" | sed 's/^/  /' >&2
        exit 1
    fi
    echo "boundary OK: src/main is ATAK-type-free"

# Run the instrumented espresso suite (incl. SystemsCheckTest, which asserts the
# load-run CheckReport has no FAILED items) on a connected device running the
# matching ATAK. androidTest exists for ONE variant only (-Pespresso re-enables
# it; the other ~199 stay off to keep configuration small).
espresso variant="atak530CivDebug":
    #!/usr/bin/env bash
    set -euo pipefail
    v="{{variant}}"
    ./gradlew -Pespresso="$v" ":app:connected${v^}AndroidTest"
