package miehexaddon.utils

import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.TaskProvider

/**
 * Registers `prepareHexCliServer` and makes `runHexCli` depend on it.
 *
 * `./gradlew :<platform>:runHexCli -PacceptEula=true` writes `eula.txt` (you are accepting the Minecraft EULA:
 * https://aka.ms/MinecraftEULA) and, if missing, a loopback-only offline `server.properties` with a small flat world.
 * Without `-PacceptEula=true` it refuses to start unless `eula.txt` already says `eula=true`.
 */
fun Project.configureHexCliServer(runDirPath: String = "run/hexcli"): TaskProvider<Task> {
    val runDir = file(runDirPath)
    val acceptEula = providers.gradleProperty("acceptEula").map { it.isEmpty() || it.toBoolean() }.orElse(false)
    val prepare = tasks.register("prepareHexCliServer") {
        group = "hexcli"
        description = "Writes eula.txt (with -PacceptEula=true) and server.properties for the HexCLI dev server."
        doLast {
            runDir.mkdirs()
            val eula = runDir.resolve("eula.txt")
            if (acceptEula.get()) {
                eula.writeText("# accepted via -PacceptEula=true (https://aka.ms/MinecraftEULA)\neula=true\n")
            } else if (!(eula.exists() && eula.readText().contains("eula=true"))) {
                throw GradleException("Accept the Minecraft EULA first: ./gradlew :${project.name}:runHexCli -PacceptEula=true (https://aka.ms/MinecraftEULA)")
            }
            val props = runDir.resolve("server.properties")
            if (!props.exists()) {
                props.writeText(
                    listOf(
                        "server-ip=127.0.0.1",
                        "online-mode=false",
                        "spawn-protection=0",
                        "level-type=minecraft\\:flat",
                        "generate-structures=false",
                        "spawn-npcs=false",
                        "spawn-animals=false",
                        "spawn-monsters=false",
                        "view-distance=4",
                        "simulation-distance=4",
                    ).joinToString("\n", postfix = "\n")
                )
            }
        }
    }
    tasks.matching { it.name == "runHexCli" }.configureEach { dependsOn(prepare) }
    return prepare
}
