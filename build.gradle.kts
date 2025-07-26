import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "2.1.0"
    id("org.jetbrains.intellij.platform") version "2.7.0"
}

group = "com.github.carmooo"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
    }
}

// Configure Gradle IntelliJ Plugin
// Read more: https://plugins.jetbrains.com/docs/intellij/tools-intellij-platform-gradle-plugin.html
dependencies {
    intellijPlatform {
        create("GO", "2024.3")
        testFramework(org.jetbrains.intellij.platform.gradle.TestFrameworkType.Platform)

        val platformPlugins = providers.gradleProperty("platformPlugins").orNull
        platformPlugins?.split(',')?.map(String::trim)?.filter(String::isNotEmpty)?.forEach { pluginId ->
            plugin(pluginId)
        }

        pluginVerifier()
        zipSigner()
    }
}

intellijPlatform {
    pluginConfiguration {
        ideaVersion {
            sinceBuild = "243"
        }

        changeNotes = """
      Initial version
    """.trimIndent()
    }
}

tasks {
    // Set the JVM compatibility versions
    withType<JavaCompile> {
        sourceCompatibility = "21"
        targetCompatibility = "21"
    }
    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        compilerOptions.jvmTarget.set(JvmTarget.JVM_21)
    }

/*    publishPlugin {
        token.set(providers.environmentVariable("PUBLISH_TOKEN"))
    }

    signPlugin {
        certificateChain.set(providers.environmentVariable("CERTIFICATE_CHAIN"))
        privateKey.set(providers.environmentVariable("PRIVATE_KEY"))
        password.set(providers.environmentVariable("PRIVATE_KEY_PASSWORD"))
    }*/
}
