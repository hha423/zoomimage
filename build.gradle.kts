import org.jetbrains.kotlin.compose.compiler.gradle.ComposeCompilerGradlePluginExtension
import org.jetbrains.kotlin.compose.compiler.gradle.ComposeFeatureFlag
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType
import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile

buildscript {
    repositories {
//        maven { setUrl("https://maven.aliyun.com/repository/public") }  // central、jcenter
//        maven { setUrl("https://maven.aliyun.com/repository/google") }  // google
//        maven { setUrl("https://repo.huaweicloud.com/repository/maven/") }    // central、google、jcenter
//        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
        gradlePluginPortal()
        mavenCentral()
        google()
    }
    dependencies {
        classpath(libs.gradlePlugin.android)
        classpath(libs.gradlePlugin.androidxNavigationSafeArgs)
        classpath(libs.gradlePlugin.buildkonfig)
        classpath(libs.gradlePlugin.jetbrainsCompose)
        classpath(libs.gradlePlugin.kotlin)
        classpath(libs.gradlePlugin.kotlinSerialization)
        classpath(libs.gradlePlugin.kotlinComposeCompiler)
        classpath(libs.gradlePlugin.kotlinxAtomicfu)
        classpath(libs.gradlePlugin.kotlinxCover)
        classpath(libs.gradlePlugin.mavenPublish)
//        classpath(libs.gradlePlugin.dokka)
    }
}

plugins {
    alias(libs.plugins.dokka)
}

allprojects {
    repositories {
//        maven { setUrl("https://maven.aliyun.com/repository/public") }  // central、jcenter
//        maven { setUrl("https://maven.aliyun.com/repository/google") }  // google
//        maven { setUrl("https://repo.huaweicloud.com/repository/maven/") }    // central、google、jcenter
        mavenCentral()
        google()
        maven { setUrl("https://www.jitpack.io") }
//        maven { setUrl("https://s01.oss.sonatype.org/content/repositories/snapshots") }
//        mavenLocal()
    }
}

tasks.register("cleanRootBuild", Delete::class) {
    delete(rootProject.project.layout.buildDirectory.get().asFile.absolutePath)
}

allprojects {
    kotlinDependenciesConfig()
    jvmTargetConfig()
    composeConfig()
    publishConfig()
    dokkaConfig()
    applyOkioJsTestWorkaround()
    androidTestConfig()
}

// TODO jetbrains-compose bug https://youtrack.jetbrains.com/issue/CMP-5831
allprojects {
    configurations.all {
        resolutionStrategy.eachDependency {
            if (requested.group == "org.jetbrains.kotlinx" && requested.name == "atomicfu") {
                useVersion(libs.versions.kotlinx.atomicfu.get())
            }
        }
    }
}

fun Project.kotlinDependenciesConfig() {
    dependencies {
        modules {
            module("org.jetbrains.kotlin:kotlin-stdlib-jdk7") {
                replacedBy("org.jetbrains.kotlin:kotlin-stdlib")
            }
            module("org.jetbrains.kotlin:kotlin-stdlib-jdk8") {
                replacedBy("org.jetbrains.kotlin:kotlin-stdlib")
            }
        }
    }
}

fun Project.jvmTargetConfig() {
    // Must be included in afterEvaluate to find the plugin
    afterEvaluate {
        // Compose Multiplatform 1.8.0 must use JVM target 11+, and Android View also requires 1.8+
        val (version, target) = if (plugins.findPlugin("org.jetbrains.kotlin.plugin.compose") != null) {
            JavaVersion.VERSION_11 to JvmTarget.JVM_11
        } else {
            JavaVersion.VERSION_1_8 to JvmTarget.JVM_1_8
        }
        tasks.withType<JavaCompile>().configureEach {
            sourceCompatibility = version.toString()
            targetCompatibility = version.toString()
            options.compilerArgs = options.compilerArgs + "-Xlint:-options"
        }
        tasks.withType<KotlinJvmCompile>().configureEach {
            compilerOptions.jvmTarget = target
        }
    }
}

fun Project.composeConfig() {
    plugins.withId("org.jetbrains.kotlin.plugin.compose") {
        extensions.configure<ComposeCompilerGradlePluginExtension> {
            featureFlags.addAll(
                ComposeFeatureFlag.OptimizeNonSkippingGroups
            )
            stabilityConfigurationFiles.add { rootDir.resolve("zoomimage-core/compose_compiler_config.conf") }

            /**
             * Run the `./gradlew clean :sketch-compose:assembleRelease -PcomposeCompilerReports=true` command to generate a report,
             * which is located in the `project/module/build/compose_compiler` directory.
             *
             * Interpretation of the report: https://developer.android.com/jetpack/compose/performance/stability/diagnose#kotlin
             */
            if (project.findProperty("composeCompilerReports") == "true") {
                val outputDir = layout.buildDirectory.dir("compose_compiler").get().asFile
                metricsDestination = outputDir
                reportsDestination = outputDir
            }
        }
    }
}

fun Project.publishConfig() {
    if (
//        && hasProperty("mavenCentralUsername")    // configured in the ~/.gradle/gradle.properties file
//        && hasProperty("mavenCentralPassword")    // configured in the ~/.gradle/gradle.properties file
        hasProperty("versionName")    // configured in the rootProject/gradle.properties file
        && hasProperty("GROUP")    // configured in the rootProject/gradle.properties file
        && hasProperty("POM_ARTIFACT_ID")    // configured in the project/gradle.properties file
    ) {
        apply { plugin("com.vanniktech.maven.publish") }

        configure<com.vanniktech.maven.publish.MavenPublishBaseExtension> {
            version = property("versionName").toString()
            if (hasProperty("signing.keyId")    // configured in the ~/.gradle/gradle.properties file
                && hasProperty("signing.password")    // configured in the ~/.gradle/gradle.properties file
                && hasProperty("signing.secretKeyRingFile")    // configured in the ~/.gradle/gradle.properties file
            ) {
                signAllPublications()
            } else if (
                System.getenv("ORG_GRADLE_PROJECT_signingInMemoryKey").orEmpty()
                    .isNotEmpty()    // configured in the github workflow env
                && System.getenv("ORG_GRADLE_PROJECT_signingInMemoryKeyPassword").orEmpty()
                    .isNotEmpty()    // configured in the github workflow env
            ) {
                signAllPublications()
            }
        }
    }
}

fun Project.dokkaConfig() {
    if (
        hasProperty("POM_ARTIFACT_ID")    // configured in the module/gradle.properties file
    ) {
        apply { plugin("org.jetbrains.dokka") }
    }
}

// https://github.com/square/okio/issues/1163
fun Project.applyOkioJsTestWorkaround() {
    if (":sample" in displayName) {
        // The polyfills cause issues with the sample.
        return
    }

    plugins.withId("org.jetbrains.kotlin.multiplatform") {
        val applyNodePolyfillPlugin by lazy {
            tasks.register("applyNodePolyfillPlugin") {
                val applyPluginFile = projectDir
                    .resolve("webpack.config.d/applyNodePolyfillPlugin.js")
                onlyIf {
                    !applyPluginFile.exists()
                }
                doLast {
                    applyPluginFile.parentFile.mkdirs()
                    applyPluginFile.writeText(
                        """
                        const NodePolyfillPlugin = require("node-polyfill-webpack-plugin");
                        config.plugins.push(new NodePolyfillPlugin());
                        """.trimIndent(),
                    )
                }
            }
        }

        extensions.configure<KotlinMultiplatformExtension> {
            sourceSets {
                targets.configureEach {
                    compilations.configureEach {
                        if (platformType == KotlinPlatformType.js && name == "test") {
                            tasks
                                .getByName(compileKotlinTaskName)
                                .dependsOn(applyNodePolyfillPlugin)
                            dependencies {
                                implementation(devNpm("node-polyfill-webpack-plugin", "^2.0.1"))
                            }
                        }
                    }
                }
            }
        }
    }
}

fun Project.androidTestConfig() {
    // Uninstall test APKs after running instrumentation tests.
    tasks.configureEach {
        if (name == "connectedDebugAndroidTest") {
            finalizedBy("uninstallDebugAndroidTest")
        }
    }
}