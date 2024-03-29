plugins {
    kotlin("jvm") version BuildSrcGlobal.VersionKotlin
    id("com.github.johnrengelman.shadow") version "shadow".pluginVersion()
    application
}

group = "com.hoffi"
version = "1.0.0"
val artifactName by extra { project.name.toLowerCase().replace('_', '-') }
val theMainClass by extra { "com.hoffi.dsl.AppKt" }

repositories {
    mavenCentral()
}

dependencies {
    //implementation("io.github.microutils:kotlin-logging:2.0.6")
    implementation("org.jetbrains.kotlinx:kotlinx-datetime".depAndVersion())
    implementation("com.github.ajalt.clikt:clikt".depAndVersion())
    //implementation(kotlin("reflect"))

    runtimeOnly("org.jetbrains.kotlin:kotlin-main-kts:${BuildSrcGlobal.VersionKotlin}")
    runtimeOnly("org.jetbrains.kotlin:kotlin-scripting-jsr223:${BuildSrcGlobal.VersionKotlin}")
    //runtimeOnly("net.java.dev.jna:jna:5.8.0")

    testImplementation(kotlin("test-common"))
    testImplementation(kotlin("test-annotations-common"))
}

application {
    mainClass.set(theMainClass)
}

//kotlin {
//    jvmToolchain(BuildSrcGlobal.jdkVersion)
//}
buildSrcCommonConfigureKotlin()

tasks {
    withType<Jar> {
        archiveBaseName.set(artifactName)
    }
    shadowJar {
        manifest { attributes["Main-Class"] = theMainClass }
        archiveClassifier.set("fat")
        mergeServiceFiles()
        minimize()
    }
//    val build by existing {
//        finalizedBy(shadowCreate)
//    }
    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
        kotlinOptions{
            //Will retain parameter names for Java reflection
            javaParameters = true
            kotlinOptions.freeCompilerArgs = listOf("-Xcontext-receivers")
        }
    }

    withType<Test> {
        buildSrcJvmTestConfig()
    }
}

// Helper tasks to speed up things and don't waste time
//=====================================================
// 'c'ompile 'c'ommon
val cc by tasks.registering {
    dependsOn(
        ":compileKotlin",
        ":compileTestKotlin")
}

fun Project.buildSrcCommonConfigureKotlin() {
    val prj = this
    prj.plugins.withId("org.jetbrains.kotlin.multiplatform") {
        val kotlinMultiplatformExtension = prj.extensions.findByType(org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension::class.java)
        kotlinMultiplatformExtension?.apply {
            // THIS IS THE ACTUAL kotlin { ... } configure in case of a kotlin("multiplatform") project
            afterEvaluate {
                println("project ${prj.name}: configure from configureKotlinProjectFromRootSubprojectClause() for kotlin MPP project ...")
                jvmToolchain(BuildSrcGlobal.jdkVersion)
            }
            // END OF kotlin { ... } configure in case of a kotlin("multiplatform") project
            val build by prj.tasks.existing
            build.configure { doLast {
                val kotlinTopLevelExtension = kotlinMultiplatformExtension as org.jetbrains.kotlin.gradle.dsl.KotlinTopLevelExtension
                print  ("   Kotlin version: " + kotlinTopLevelExtension.coreLibrariesVersion) ; if (kotlinTopLevelExtension.coreLibrariesVersion != BuildSrcGlobal.VersionKotlin) println(" ( != ${BuildSrcGlobal.VersionKotlin} )") else println()
                println("   java.sourceCompatibility: ${java.sourceCompatibility}")
                println("   java.targetCompatibility: ${java.targetCompatibility}")
                println("   gradle version: ${gradle.gradleVersion}")
                val mppCompileConfiguration = prj.configurations.first { it.name.endsWith("CompileClasspath") && it.isCanBeResolved }
                val regex = Regex(pattern = "^(spring-cloud-starter|spring-boot-starter|micronaut-core|kotlin-stdlib-[0-9]|foundation-desktop).*$")
                val jarsToReport = mppCompileConfiguration.filter { it.name.matches(regex) }.files
                if (jarsToReport.isNotEmpty()) {
                    println("    chosen first '...CompileClasspath configuration to inspect JVM classpath: ${mppCompileConfiguration.name}")
                    jarsToReport.forEach { println("    ${project.name}: ${it.name}") }
                }
            } }
        }
    }
    project.plugins.withId("org.jetbrains.kotlin.jvm") {
        project.extensions.findByType(org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension::class.java)?.apply {
            // THIS IS THE ACTUAL kotlin { ... } configure in case of a kotlin("jvm") project
            afterEvaluate {
                println("project ${project.name}: configure from configureKotlinProjectFromRootSubprojectClause() for kotlin JVM project ...")
                jvmToolchain(BuildSrcGlobal.jdkVersion)
            }
            // END OF kotlin { ... } configure in case of a kotlin("jvm") project
            val build by project.tasks.existing
            build.configure { doLast {
                println("project ${project.name}: versionReport from configureKotlinProjectFromRootSubprojectClause() for kotlin JVM project ...")
                print  ("   Kotlin version: " + kotlin.coreLibrariesVersion) ; if (kotlin.coreLibrariesVersion != BuildSrcGlobal.VersionKotlin) println(" ( != ${BuildSrcGlobal.VersionKotlin} )") else println()
                println("   java.sourceCompatibility: ${java.sourceCompatibility}")
                println("   java.targetCompatibility: ${java.targetCompatibility}")
                println("   gradle version: ${project.gradle.gradleVersion}")
                val regex = Regex(pattern = "^(spring-cloud-starter|spring-boot-starter|micronaut-core|kotlin-stdlib-[0-9]|foundation-desktop).*$")
                prj.configurations.compileClasspath.get().files.filter { it.name.matches(regex) }
                    .forEach { println("    ${project.name}: ${it.name}") }
            } }
        }
    }
}

// ################################################################################################
// #####    pure informational stuff on stdout    #################################################
// ################################################################################################
tasks.register<CheckVersionsTask>("checkVersions") // implemented in buildSrc/src/main/kotlin/Deps.kt
tasks.register("printClasspath") {
    group = "misc"
    description = "print classpath"
    doLast {
        // filters only existing and non-empty dirs
        project.configurations.getByName("runtimeClasspath").files
            .filter { it.isDirectory && (it?.listFiles()?.isNotEmpty() ?: false) || it.isFile }
            .forEach{ println(it) }
    }
}
