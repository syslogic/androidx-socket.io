import com.android.build.gradle.internal.tasks.factory.dependsOn

import org.gradle.internal.os.OperatingSystem

import java.io.ByteArrayOutputStream

import com.github.gradle.node.npm.task.NpmTask

import kotlin.apply

plugins {
    id("base")
    alias(libs.plugins.nodejs)
}

node {
    version = "22.14.0"
    npmVersion = "11.4.0"
    distBaseUrl = "https://nodejs.org/dist"
    download = true
}

/** Configure NpmTask. */
tasks.named("npm_update", NpmTask::class.java) {
    args.apply { listOf<String>("--omit", "dev", "--loglevel", "warn") }
}

/** Configure NpmTask. */
val npmRunBuildTask = tasks.named("npm_run_build", NpmTask::class.java) {
    inputs.files(fileTree("public"))
    inputs.file("package.json")
    inputs.file("package-lock.json")
    outputs.upToDateWhen { file("${projectDir}/build").exists() }
    outputs.dir("build")
}

/** Package output of the build into JAR file. */
val packageNpmApp = tasks.register("packageNpmApp", Zip::class.java) {
    dependsOn(npmRunBuildTask)
    destinationDirectory = file("${projectDir}/build")
    from(".")
    include("index.js")
    include("package.json")
    include("README.md")
    include("public/*")
}

// Declare a dedicated scope for publishing the packaged JAR
val npmResources: Configuration? by configurations.creating
configurations.default.get().extendsFrom(npmResources)

// Expose the artifact created by the packaging task
artifacts {
    npmResources.apply {
        add("archives", packageNpmApp) {
            builtBy(packageNpmApp)
            type = "jar"
        }
    }
}

tasks.named("assemble").dependsOn(packageNpmApp)

tasks.named("clean") {
    delete(packageNpmApp.get().path)
}

tasks.register("startServer", Exec::class.java) {
    val path: String = rootProject.file("server/scripts").absoluteFile.path
    val os: OperatingSystem = OperatingSystem.current()
    val stdOut = ByteArrayOutputStream()
    val stdErr = ByteArrayOutputStream()
    isIgnoreExitValue = true
    standardOutput = stdOut
    errorOutput = stdErr

    if (os.isUnix || os.isLinux || os.isMacOsX) {
        commandLine("$path/start_server.sh")
    } else {
        commandLine("cmd", "/c", "pwsh -File $path/start_server.ps1")
    }
    doLast {
        if (executionResult.get().exitValue == 0) {
            println(standardOutput.toString())
        } else {
            println(stdErr.toString())
        }
    }
}

tasks.register("stopServer", Exec::class.java) {
    val os: OperatingSystem = OperatingSystem.current()
    val stdOut = ByteArrayOutputStream()
    val stdErr = ByteArrayOutputStream()
    isIgnoreExitValue = true
    standardOutput = stdOut
    errorOutput = stdErr

    if (os.isUnix || os.isLinux || os.isMacOsX) {
        commandLine(rootProject.file("server/scripts").absoluteFile.path + "/stop_server.sh")
    } else {
        commandLine("cmd", "/c", "pwsh -File " + rootProject.file("server/scripts").absoluteFile.path + "/stop_server.ps1")
    }

    doLast {
        if (executionResult.get().exitValue == 0) {
            println(standardOutput.toString())
        } else {
            println(stdErr.toString())
        }
    }
}
