> [!NOTE]
> This repository is a **GitHub template** generated automatically from
> [xm1221/MieHexTemplate](https://github.com/xm1221/MieHexTemplate) @ `2bf8ace` with the default answers.
> Click **Use this template**, clone your new repo, then rename the mod:
>
> ```sh
> python3 rename-mod.py --modid mymod --package io.github.me.mymod --name "My Mod" --github-user me
> ./gradlew build
> ```
>
> Prefer Copier (`uvx copier copy gh:xm1221/MieHexTemplate .`) if you want to answer the questions yourself
> (e.g. a project without MieLib/HexCLI) or `copier update` later. Don't open pull requests here:
> this repo is overwritten on every publish.

# MieHex Addon

**English** | [简体中文](README.zh-CN.md)

[![powered by hexdoc](https://img.shields.io/endpoint?url=https://hexxy.media/api/v0/badge/hexdoc?label=1)](https://github.com/hexdoc-dev/hexdoc)

MieHex Addon addon for Hex Casting

## HexCLI (MieLib) development loop

This project depends on [MieLib](https://github.com/xm1221/MieLib) and is set up for **HexCLI**: a headless
dedicated server that runs [hexParse](https://modrinth.com/mod/hexparse) code and prints the resulting stack,
ravenmind and mishaps. hexParse is only on the development runtime; it is not a dependency of the published mod.

```sh
# 1. start a HexCLI server (dev server with -Dmielib.hexcli=true). The first time you must accept the
#    Minecraft EULA (https://aka.ms/MinecraftEULA):
./gradlew :fabric:runHexCli -PacceptEula=true     # Fabric, in fabric/run/hexcli
./gradlew :forge:runHexCli -PacceptEula=true      # or Forge, in forge/run/hexcli

# 2. in another terminal: run the bundled examples (hexcli/examples.tsv) and compare the stacks
./scripts/hexcli-check.sh
#    (with no server running, the script starts one itself and stops it afterwards:
#     ./scripts/hexcli-check.sh --accept-eula)
#    add --platform forge to use the Forge server instead of the Fabric one

# 3. see how a parse error and a mishap are reported (this one is expected to FAIL)
./scripts/hexcli-check.sh -f hexcli/examples-broken.tsv
```

`scripts/hexcli-check.sh` downloads `hexcli-client.jar` from the MieLib HexCLI release (SHA-256 checked) into
`.gradle/hexcli/`. Always run `runHexCli` with the `:fabric:` or `:forge:` prefix: a bare
`./gradlew runHexCli` makes Gradle start both servers one after another, and the second one never starts.
To play interactively:
`java -jar .gradle/hexcli/hexcli-client-*.jar --dir fabric/run/hexcli` (or `--dir forge/run/hexcli`).

The check script tells the failure types apart:

| outcome | looks like | client exit code |
|---|---|---|
| `PARSE_ERROR` | `hexParse: Unknown symbol: …`, `hexParse produced no iotas for: …` (bad token dropped before casting) | often **0** |
| `MISHAP` | `ERRORED` / `INVALID` + `! step N: …` | 1 |
| `HEXCLI_ERROR` | `hexcli: …` / `[hexcli] …` (connection, arguments) | 1 or 2 |

[`skills/hexcasting/SKILL.md`](skills/hexcasting/SKILL.md) is a full HexCLI / hexParse guide (in Chinese) for both
humans and AI agents: syntax, pattern names, real outputs and common pitfalls.

MieLib on Forge: no public Forge jar exists yet, so one is vendored in [`libs/`](libs/README.md).

## Credits

Generated from [MieHexTemplate](https://github.com/xm1221/MieHexTemplate), which is based on
[HexDummy v2](https://github.com/FallingColors/hexdummy) by artynova and Falling Colors (MIT).
