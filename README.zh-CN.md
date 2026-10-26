> [!NOTE]
> 这是一个 **GitHub 模板仓库**，由 [xm1221/MieHexTemplate](https://github.com/xm1221/MieHexTemplate) @ `2bf8ace`
> 按默认答案自动生成。点 **Use this template** 建好你自己的仓库，clone 下来，然后把模组改成你的名字：
>
> ```sh
> python3 rename-mod.py --modid mymod --package io.github.me.mymod --name "My Mod" --github-user me
> ./gradlew build
> ```
>
> 想自己回答生成时的问题（比如不要 MieLib/HexCLI），或者以后想用 `copier update` 跟进模板更新，
> 请改用 Copier：`uvx copier copy gh:xm1221/MieHexTemplate .`。
> 这个仓库每次发布都会被整个覆盖，所以请不要往这里提 PR。

# MieHex Addon

[English](README.md) | **简体中文**

[![powered by hexdoc](https://img.shields.io/endpoint?url=https://hexxy.media/api/v0/badge/hexdoc?label=1)](https://github.com/hexdoc-dev/hexdoc)

MieHex Addon addon for Hex Casting

一个 [Hex Casting](https://github.com/FallingColors/HexMod) 附属模组。Minecraft 1.20.1，Fabric + Forge 双端（Architectury），Kotlin 编写。

## 常用命令

```sh
./gradlew build                           # 编译两端的模组 jar，输出在 fabric/build/libs 和 forge/build/libs
./gradlew :fabric:runClient            # 启动 Fabric 开发客户端
./gradlew :forge:runClient             # 启动 Forge 开发客户端
uv sync && hexdoc build                   # 构建 hexdoc 文档（需要先激活 .venv）
```

## HexCLI（MieLib）开发流程

本项目依赖 [MieLib](https://github.com/xm1221/MieLib)，并且已经配好了 **HexCLI**。HexCLI 是一个无头的专用服务器，
你把 [hexParse](https://modrinth.com/mod/hexparse) 代码交给它，它就会施法并打印出结果：栈、渡鸦之思（ravenmind）和事故（mishap）。
hexParse 只在开发环境运行时加载，**不会**成为你发布出去的模组的依赖。

```sh
# 1. 启动 HexCLI 服务器（带 -Dmielib.hexcli=true 的开发服务器）。第一次需要同意 Minecraft EULA（https://aka.ms/MinecraftEULA）：
./gradlew :fabric:runHexCli -PacceptEula=true     # Fabric，运行目录 fabric/run/hexcli
./gradlew :forge:runHexCli -PacceptEula=true      # 或者 Forge，运行目录 forge/run/hexcli

# 2. 另开一个终端，跑自带的例子（hexcli/examples.tsv）并核对栈
./scripts/hexcli-check.sh
#    没有开着的服务器时，脚本会自己启动一个，跑完再关掉：
#    ./scripts/hexcli-check.sh --accept-eula
#    加 --platform forge 就改用 Forge 服务器

# 3. 看看解析错误和事故分别是怎么报的（这个文件里的例子本来就会失败）
./scripts/hexcli-check.sh -f hexcli/examples-broken.tsv
```

`runHexCli` 一定要带 `:fabric:` 或 `:forge:` 前缀。直接写 `./gradlew runHexCli` 会让 Gradle 依次启动两个服务器，
第一个不关，第二个就永远起不来。

`scripts/hexcli-check.sh` 会从 MieLib 的 HexCLI Release 下载 `hexcli-client.jar`（校验 SHA-256），放在 `.gradle/hexcli/`。
想自己交互着玩的话：
`java -jar .gradle/hexcli/hexcli-client-*.jar --dir fabric/run/hexcli`（Forge 用 `--dir forge/run/hexcli`）。

`hexcli/examples.tsv` 每行用 Tab 分隔：`名称  代码  预期结果  预期栈  消息正则`。改了图案或者新加了图案，就往里面加几行，
以后每次改代码都能一键回归。

检查脚本会把失败分成三类：

| 结果 | 输出里的样子 | 客户端退出码 |
|---|---|---|
| `PARSE_ERROR` 解析错误 | `hexParse: Unknown symbol: …`、`hexParse produced no iotas for: …`（写错的符号在施法前就被丢掉了） | 经常是 **0** |
| `MISHAP` 事故 | `ERRORED` / `INVALID`，加上 `! step N: …` | 1 |
| `HEXCLI_ERROR` | `hexcli: …` / `[hexcli] …`（连接失败、参数错误等） | 1 或 2 |

注意解析错误时退出码往往还是 0，所以别只看退出码，要看输出里有没有 `hexParse:` 开头的行。

[`skills/hexcasting/SKILL.md`](skills/hexcasting/SKILL.md) 是一份完整的 HexCLI / hexParse 指南，人和 AI 都能看：
语法、图案注册名、真实输出示例和常见的坑都在里面。

关于 Forge 上的 MieLib：目前还没有公开的 Forge jar，所以模板在 [`libs/`](libs/README.md) 里附带了一份。

## 致谢

本项目由 [MieHexTemplate](https://github.com/xm1221/MieHexTemplate) 生成。MieHexTemplate 基于
artynova 和 Falling Colors 的 [HexDummy v2](https://github.com/FallingColors/hexdummy)（MIT 协议）。HexCLI、`libs/` 里的 Forge jar 和 `skills/hexcasting/SKILL.md` 来自 [MieLib](https://github.com/xm1221/MieLib)（MIT 协议）。
