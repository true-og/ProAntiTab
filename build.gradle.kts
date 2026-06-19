/* This is free and unencumbered software released into the public domain */

/* ------------------------------ Plugins ------------------------------ */
plugins {
    id("java") // Import Java plugin.
    id("java-library") // Import Java Library plugin.
    id("com.diffplug.spotless") version "8.1.0" // Import Spotless plugin.
    id("com.gradleup.shadow") version "8.3.9" // Import Shadow plugin.
    id("checkstyle") // Import Checkstyle plugin.
    eclipse
}

/* --------------------------- JDK / Kotlin ---------------------------- */
java {
    sourceCompatibility = JavaVersion.VERSION_17 // Compile with JDK 17 compatibility.
    toolchain { // Select Java toolchain.
        languageVersion.set(JavaLanguageVersion.of(17)) // Use JDK 17.
    }
}

/* ----------------------------- Metadata ------------------------------ */
group = "de.rayzs.pat" // Declare bundle identifier.

version = "2.3.2" // Declare plugin version (will be in .jar).

val apiVersion = "1.13" // Declare minecraft server target version.

/* ----------------------------- Resources ----------------------------- */
tasks.named<ProcessResources>("processResources") {
    exclude("**/*.txt") // Exclude text files from the jar (matches the original Maven build).
    from("LICENSE") { into("/") } // Bundle licenses into jarfiles.
}

/* ---------------------------- Repos ---------------------------------- */
repositories {
    mavenCentral() // Import the Maven Central Maven Repository.
    gradlePluginPortal() // Import the Gradle Plugin Portal Maven Repository.
    maven { url = uri("https://hub.spigotmc.org/nexus/content/repositories/snapshots/") } // Spigot.
    maven { url = uri("https://oss.sonatype.org/content/repositories/snapshots") } // Sonatype (BungeeCord).
    maven { url = uri("https://repo.papermc.io/repository/maven-public/") } // PaperMC.
    maven { url = uri("https://jitpack.io") } // JitPack.
    maven { url = uri("https://repo.velocitypowered.com/snapshots/") } // Velocity.
    maven { url = uri("https://libraries.minecraft.net/") } // Minecraft Libraries.
    maven { url = uri("https://repo.extendedclip.com/content/repositories/placeholderapi/") } // PlaceholderAPI.
    maven { url = uri("https://repo.viaversion.com") } // ViaVersion.
    maven { url = uri("https://repo.william278.net/velocity") } // William278 (Velocity).
    maven { url = uri("https://repo.william278.net/papiproxybridge") } // William278 (PAPIProxyBridge).
    mavenLocal() // Import the local Maven Repository.
}

/* ---------------------- Java project deps ---------------------------- */
dependencies {
    // Platform / API dependencies (provided at runtime, not shaded).
    compileOnly("dev.folia:folia-api:1.20.1-R0.1-SNAPSHOT")
    compileOnly("org.spigotmc:spigot-api:1.19-R0.1-SNAPSHOT")
    compileOnly("io.netty:netty-all:4.2.0.Final")
    compileOnly("net.md-5:bungeecord-api:1.21-R0.5-SNAPSHOT")
    compileOnly("net.md-5:bungeecord-protocol:1.16-R0.4")
    compileOnly("com.mojang:authlib:4.0.43")
    compileOnly("com.github.ElgarL:groupmanager:3.2")
    compileOnly("com.mojang:brigadier:1.1.8")
    compileOnly("com.mojang:datafixerupper:6.0.8")
    compileOnly("com.mojang:logging:1.1.1")
    compileOnly("org.apache.maven:maven-resolver-provider:3.8.5")
    compileOnly("org.apache.maven.resolver:maven-resolver-connector-basic:1.7.3")
    compileOnly("org.apache.maven.resolver:maven-resolver-transport-http:1.7.3")
    compileOnly("org.apache.logging.log4j:log4j-core:2.18.0")
    compileOnly("io.github.waterfallmc:waterfall-api:1.20-R0.3-SNAPSHOT")
    compileOnly("net.william278:papiproxybridge:1.4")
    compileOnly("com.velocitypowered:velocity-api:3.3.0-SNAPSHOT")
    compileOnly("com.velocitypowered:velocity-proxy:3.3.0-SNAPSHOT") {
        exclude(group = "com.velocitypowered", module = "velocity-proxy-log4j2-plugin")
    }
    compileOnly("org.sonatype.sisu:sisu-guice:2.1.7")
    compileOnly("org.spongepowered:configurate-hocon:4.1.2")
    compileOnly("me.clip:placeholderapi:2.11.6")
    compileOnly("net.luckperms:api:5.5")
    compileOnly("com.viaversion:viaversion-api:5.7.0-SNAPSHOT")
    compileOnly("io.papermc.paper:paper-api:1.17-R0.1-SNAPSHOT")

    // Generate velocity-plugin.json from the @Plugin annotation.
    annotationProcessor("com.velocitypowered:velocity-api:3.3.0-SNAPSHOT")

    // Adventure (shaded into the jar).
    implementation("net.kyori:adventure-text-minimessage:4.26.1")
    implementation("net.kyori:adventure-api:4.26.1")
    implementation("net.kyori:adventure-platform-api:4.4.1")
    implementation("net.kyori:adventure-platform-bungeecord:4.4.1")
    implementation("net.kyori:adventure-platform-bukkit:4.4.1")
}

/* ---------------------- Reproducible jars ---------------------------- */
tasks.withType<AbstractArchiveTask>().configureEach { // Ensure reproducible .jars
    isPreserveFileTimestamps = false
    isReproducibleFileOrder = true
}

/* ----------------------------- Shadow -------------------------------- */
tasks.shadowJar {
    archiveFileName.set("ProAntiTab.jar") // Match the original Maven finalName.
}

tasks.jar { archiveClassifier.set("part") } // Applies to root jarfile only.

tasks.build { dependsOn(tasks.spotlessApply, tasks.shadowJar) } // Build depends on spotless and shadow.

/* --------------------------- Javac opts ------------------------------- */
tasks.withType<JavaCompile>().configureEach {
    options.compilerArgs.add("-parameters") // Enable reflection for java code.
    options.isFork = true // Run javac in its own process.
    options.compilerArgs.add("-Xlint:deprecation") // Trigger deprecation warning messages.
    options.encoding = "UTF-8" // Use UTF-8 file encoding.
}

/* ----------------------------- Auto Formatting ------------------------ */
spotless {
    java {
        eclipse().configFile("config/formatter/eclipse-java-formatter.xml") // Eclipse java formatting.
        leadingTabsToSpaces() // Convert leftover leading tabs to spaces.
        removeUnusedImports() // Remove imports that aren't being called.
    }
    kotlinGradle {
        ktfmt().kotlinlangStyle().configure { it.setMaxWidth(120) } // JetBrains Kotlin formatting.
        target("build.gradle.kts", "settings.gradle.kts") // Gradle files to format.
    }
}

checkstyle {
    toolVersion = "10.18.1" // Declare checkstyle version to use.
    configFile = file("config/checkstyle/checkstyle.xml") // Point checkstyle to config file.
    isIgnoreFailures = true // Don't fail the build if checkstyle does not pass.
    isShowViolations = true // Show the violations in any IDE with the checkstyle plugin.
}

tasks.named("compileJava") {
    dependsOn("spotlessApply") // Run spotless before compiling with the JDK.
}

tasks.named("spotlessCheck") {
    dependsOn("spotlessApply") // Run spotless before checking if spotless ran.
}
