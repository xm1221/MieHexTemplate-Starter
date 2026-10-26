plugins {
    id("miehexaddon.minecraft")
}

architectury {
    common("fabric", "forge")
}

dependencies {
    implementation(libs.kotlin.stdlib)
    implementation(kotlin("reflect"))

    // We depend on fabric loader here to use the fabric @Environment annotations and get the mixin dependencies
    // Do NOT use other classes from fabric loader
    modImplementation(libs.fabric.loader)
    modApi(libs.architectury)

    modApi(libs.hexcasting.common)

    modApi(libs.clothConfig.common)

    // MieLib: compile against the Fabric jar (intermediary, same as the common project); each platform ships its own jar
    modCompileOnly(libs.mielib.fabric) { isTransitive = false }
    // Fabric API must be on this classpath too, so Loom can remap MieLib's FakePlayer overrides correctly.
    // (Loom shares the remapped jar with :fabric; without it the HexCLI player keeps intermediary method names
    // and HexCLI loses chat output such as `message: ...` and `hexParse: Unknown symbol`.)
    modCompileOnly(libs.fabric.api)

    libs.mixinExtras.common.also {
        implementation(it)
        annotationProcessor(it)
    }
}
