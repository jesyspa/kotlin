plugins {
    kotlin("jvm")
    id("jps-compatible")
}

repositories {
    maven {
        url = uri("https://packages.jetbrains.team/maven/p/kotlin-formver/maven")
    }
}

dependencies {
    implementation("viper:silicon_2.13:1.2-SNAPSHOT")
    implementation(project(mapOf("path" to ":compiler:frontend.common")))
    compileOnly(kotlinStdlib())
}

sourceSets {
    "main" { projectDefault() }
    "test" { none() }
}

runtimeJar()
javadocJar()
sourcesJar()
