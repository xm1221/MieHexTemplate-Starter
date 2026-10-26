#!/usr/bin/env bash
# Run the bundled hexParse examples against a HexCLI dev server and compare the results.
#
#   scripts/hexcli-check.sh [--platform fabric|forge] [--file hexcli/examples.tsv] [--accept-eula] [--no-start] [--keep-server] [-v]
#
# - --platform (or HEXCLI_PLATFORM) picks the dev server: fabric (default) or forge.
# - Uses an already running `./gradlew :<platform>:runHexCli` server if <platform>/run/hexcli/hexcli/client.json exists,
#   otherwise starts one (needs --accept-eula / HEXCLI_ACCEPT_EULA=1 the first time) and stops it afterwards.
# - hexcli-client.jar is downloaded once from the MieLib HexCLI installer release (SHA-256 checked)
#   into .gradle/hexcli/.
# - Each case runs on a fresh stack (`reset`) via `hexcli-client -e`, and the outcome is classified as
#     OK          the cast finished (EVALUATED / ESCAPED)
#     PARSE_ERROR hexParse could not parse something: `hexParse: ...` lines or `produced no iotas`.
#                 The bad token is dropped BEFORE casting, so the client may still exit 0.
#     MISHAP      Hex Casting failed while executing: `ERRORED` / `INVALID` + `! step N: ...`, exit 1.
#     HEXCLI_ERROR the client/server itself failed (`hexcli: ...`, `[hexcli] ...`), nothing was cast.
#
# Examples file (tab separated, '#' comments):
#   name <TAB> hexParse code <TAB> expected outcome(s) <TAB> expected stack <TAB> message regex
#   - expected outcome(s): OK, PARSE_ERROR, MISHAP, or several joined with '+' (e.g. PARSE_ERROR+MISHAP)
#   - expected stack: values bottom->top joined with ' | ', '(empty)' for an empty stack, '-' or blank = don't check
#   - message regex: optional extended regex that must match some `message: ...` line (Reveal / printMessage)
set -uo pipefail

ROOT=$(cd "$(dirname "$0")/.." && pwd)
cd "$ROOT"

FILE=hexcli/examples.tsv
START=auto
KEEP=0
VERBOSE=0
ACCEPT_EULA=${HEXCLI_ACCEPT_EULA:-0}
PLATFORM=${HEXCLI_PLATFORM:-fabric}
while [[ $# -gt 0 ]]; do
  case "$1" in
    -f|--file) FILE=$2; shift 2 ;;
    -p|--platform) PLATFORM=$2; shift 2 ;;
    --accept-eula) ACCEPT_EULA=1; shift ;;
    --no-start) START=never; shift ;;
    --keep-server) KEEP=1; shift ;;
    -v|--verbose) VERBOSE=1; shift ;;
    -h|--help) sed -n '2,26p' "$0"; exit 0 ;;
    *) echo "hexcli-check: unknown option $1" >&2; exit 2 ;;
  esac
done

# settings.gradle.kts: include("<common>", "<fabric>", "<forge>")
case "$PLATFORM" in
  fabric) n=2 ;; forge) n=3 ;;
  *) echo "hexcli-check: --platform must be fabric or forge" >&2; exit 2 ;;
esac
PLATFORM_DIR=$(sed -n "s/^include(\"\([^\"]*\)\", \"\([^\"]*\)\", \"\([^\"]*\)\")\$/\\$n/p" settings.gradle.kts)
PLATFORM_DIR=${PLATFORM_DIR:-$PLATFORM}
SERVER_DIR="$ROOT/$PLATFORM_DIR/run/hexcli"
CLIENT_JSON="$SERVER_DIR/hexcli/client.json"

toml() { sed -n "s/^$1 *= *\"\([^\"]*\)\".*/\1/p" gradle/libs.versions.toml | head -n1; }
HEXCLI_VERSION=$(toml hexcli)
RELEASE_TAG=$(toml mielib-release)

if [[ -n "${JAVA_HOME:-}" && -x "$JAVA_HOME/bin/java" ]]; then JAVA="$JAVA_HOME/bin/java"; else JAVA=java; fi

# ---- hexcli-client.jar -------------------------------------------------------
CLIENT_DIR="$ROOT/.gradle/hexcli"
CLIENT_JAR="$CLIENT_DIR/hexcli-client-$HEXCLI_VERSION.jar"
if [[ ! -f "$CLIENT_JAR" ]]; then
  mkdir -p "$CLIENT_DIR"
  base="https://github.com/xm1221/MieLib/releases/download/$RELEASE_TAG"
  tgz="hexcli-$HEXCLI_VERSION-linux.tar.gz"
  echo "hexcli-check: downloading $base/$tgz"
  tmp=$(mktemp -d)
  curl -fsSL "$base/$tgz" -o "$tmp/$tgz" && curl -fsSL "$base/SHA256SUMS" -o "$tmp/SHA256SUMS" || { echo "hexcli-check: download failed" >&2; exit 2; }
  (cd "$tmp" && awk -v f="$tgz" '{n=$2; sub(/^\*/,"",n); sub(/^\.\//,"",n); if (n==f) print $1"  "f}' SHA256SUMS \
     | grep -q . && awk -v f="$tgz" '{n=$2; sub(/^\*/,"",n); sub(/^\.\//,"",n); if (n==f) print $1"  "f}' SHA256SUMS | sha256sum -c --status) || { echo "hexcli-check: SHA-256 mismatch for $tgz" >&2; exit 2; }
  tar -xzf "$tmp/$tgz" -C "$tmp" hexcli-client.jar || { echo "hexcli-check: hexcli-client.jar not in $tgz" >&2; exit 2; }
  mv "$tmp/hexcli-client.jar" "$CLIENT_JAR"
  rm -rf "$tmp"
fi
hexcli() { "$JAVA" -jar "$CLIENT_JAR" --dir "$SERVER_DIR" --no-color "$@"; }

# ---- server ------------------------------------------------------------------
SERVER_PGID=""
stop_server() {
  if [[ -n "$SERVER_PGID" && "$KEEP" != 1 ]]; then
    echo "hexcli-check: stopping the HexCLI server we started (pgid $SERVER_PGID)"
    kill -TERM -- "-$SERVER_PGID" 2>/dev/null
    for _ in $(seq 60); do kill -0 -- "-$SERVER_PGID" 2>/dev/null || break; sleep 1; done
    kill -KILL -- "-$SERVER_PGID" 2>/dev/null
  fi
}
trap stop_server EXIT

server_up() { [[ -f "$CLIENT_JSON" ]] && printf 'quit\n' | hexcli >/dev/null 2>&1; }

if ! server_up; then
  if [[ "$START" == never ]]; then echo "hexcli-check: no HexCLI server at $SERVER_DIR (run ./gradlew :$PLATFORM_DIR:runHexCli)" >&2; exit 2; fi
  rm -f "$CLIENT_JSON"   # stale file from a killed server
  args=(":$PLATFORM_DIR:runHexCli" --console=plain)
  [[ "$ACCEPT_EULA" == 1 ]] && args+=(-PacceptEula=true)
  mkdir -p build
  echo "hexcli-check: starting ./gradlew ${args[*]} (log: build/hexcli-server.log)"
  setsid ./gradlew "${args[@]}" </dev/null >build/hexcli-server.log 2>&1 &
  SERVER_PGID=$!
  for _ in $(seq 900); do
    [[ -f "$CLIENT_JSON" ]] && break
    kill -0 "$SERVER_PGID" 2>/dev/null || { echo "hexcli-check: server exited, see build/hexcli-server.log" >&2; tail -n 30 build/hexcli-server.log >&2; exit 2; }
    if grep -qE 'Failed to start the minecraft server|BUILD FAILED|FAILURE: ' build/hexcli-server.log 2>/dev/null; then
      echo "hexcli-check: server failed to start, see build/hexcli-server.log" >&2
      grep -nE 'Caused by|FAILURE|What went wrong|Failed to start' -A2 build/hexcli-server.log | head -n 20 >&2; exit 2
    fi
    sleep 1
  done
  [[ -f "$CLIENT_JSON" ]] || { echo "hexcli-check: timed out waiting for $CLIENT_JSON" >&2; exit 2; }
fi

# ---- run cases ---------------------------------------------------------------
classify() { # stdin: client output, $1: exit code
  local out; out=$(cat); local k=()
  grep -qE '^(hexcli: |\[hexcli\] )' <<<"$out" && k+=(HEXCLI_ERROR)
  grep -qE '^hexParse:|produced no iotas' <<<"$out" && k+=(PARSE_ERROR)
  grep -qE '^cast #[0-9]+ +(ERRORED|INVALID)|^! step [0-9]+:' <<<"$out" && k+=(MISHAP)
  if [[ ${#k[@]} -eq 0 ]]; then
    if [[ "$1" == 0 ]] && grep -qE '^cast #[0-9]+ +(EVALUATED|ESCAPED)' <<<"$out"; then k=(OK); else k=(HEXCLI_ERROR); fi
  fi
  local IFS=+; echo "${k[*]}"
}
stack_of() { # stdin: client output -> "a | b | c" or "(empty)"
  local out; out=$(cat)
  if grep -qE '^ *\(empty stack\)' <<<"$out"; then echo "(empty)"; return; fi
  sed -n 's/^  \[[0-9]*\] \(▶ \|  \)\{0,1\}//p' <<<"$out" | sed 's/^▶ //' | paste -sd'|' | sed 's/|/ | /g'
}

pass=0; fail=0; n=0
while IFS=$'\t' read -r name code expect stack msg || [[ -n "${name:-}" ]]; do
  [[ -z "${name// }" || "$name" == \#* ]] && continue
  n=$((n+1))
  printf 'reset\nquit\n' | hexcli >/dev/null 2>&1
  out=$(hexcli -e "$code" 2>&1); rc=$?
  got=$(classify "$rc" <<<"$out")
  gstack=$(stack_of <<<"$out")
  problems=()
  [[ "$got" == "${expect:-OK}" ]] || problems+=("outcome $got, expected ${expect:-OK}")
  if [[ -n "${stack:-}" && "$stack" != "-" && "$gstack" != "$stack" ]]; then problems+=("stack [$gstack], expected [$stack]"); fi
  if [[ -n "${msg:-}" ]] && ! grep -E '^message: ' <<<"$out" | grep -qE -- "$msg"; then problems+=("no message matching /$msg/"); fi
  detail=$(grep -E '^hexParse:|produced no iotas|^! step|^hexcli: |^\[hexcli\]' <<<"$out" | head -n 3 | paste -sd';' | sed 's/;/; /g')
  if [[ ${#problems[@]} -eq 0 ]]; then
    pass=$((pass+1)); printf 'PASS  %-24s %-20s exit=%s stack=[%s]%s\n' "$name" "$got" "$rc" "$gstack" "${detail:+  ($detail)}"
  else
    fail=$((fail+1)); printf 'FAIL  %-24s %-20s exit=%s stack=[%s]\n' "$name" "$got" "$rc" "$gstack"
    for p in "${problems[@]}"; do printf '        - %s\n' "$p"; done
    [[ -n "$detail" ]] && printf '        > %s\n' "$detail"
  fi
  [[ "$VERBOSE" == 1 ]] && sed 's/^/        | /' <<<"$out"
done <"$FILE"

echo "hexcli-check: $pass/$n passed ($FILE, $PLATFORM)"
[[ $fail -eq 0 && $n -gt 0 ]]
