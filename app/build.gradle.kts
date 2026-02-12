val versionJsonFile = rootProject.file("version.json")
val versionJson =
    versionJsonFile
        .takeIf { it.exists() }
        ?.let {
          @Suppress("UNCHECKED_CAST")
          groovy.json.JsonSlurper().parseText(it.readText()) as Map<String, Any>
        } ?: emptyMap()

val gitCommitHash =
    providers.environmentVariable("GITHUB_SHA").orNull?.take(7)
        ?: providers
            .exec {
              commandLine("git", "rev-parse", "--short", "HEAD")
              isIgnoreExitValue = true
            }
            .standardOutput
            .asText
            .getOrElse("")
            .trim()
            .ifEmpty { "unknown" }

plugins {
  alias(libs.plugins.android.application)
  alias(libs.plugins.detekt)
  alias(libs.plugins.kotlin.android)
  alias(libs.plugins.kotlin.compose)
  alias(libs.plugins.kotlin.serialization)
  alias(libs.plugins.ksp)
  alias(libs.plugins.hilt)
  alias(libs.plugins.kover)
  alias(libs.plugins.aboutlibraries.android)
  jacoco
}

android {
  namespace = "io.github.omochice.pinosu"
  compileSdk = 36

  defaultConfig {
    applicationId = "io.github.omochice.pinosu"
    minSdk = 30
    targetSdk = 36
    versionCode = (versionJson["versionCode"] as? Number)?.toInt() ?: 1
    versionName = (versionJson["versionName"] as? String) ?: "0.1.0"

    buildConfigField("String", "COMMIT_HASH", "\"$gitCommitHash\"")

    testInstrumentationRunner = "io.github.omochice.pinosu.HiltTestRunner"
  }

  signingConfigs {
    create("release") {
      val keystoreFile = file("release.keystore")
      if (keystoreFile.exists()) {
        storeFile = keystoreFile
        storePassword = providers.environmentVariable("KEYSTORE_PASSWORD").orNull
        keyAlias = providers.environmentVariable("KEY_ALIAS").orNull
        keyPassword = providers.environmentVariable("KEY_PASSWORD").orNull
      }
    }
  }

  buildTypes {
    debug { enableAndroidTestCoverage = true }
    release {
      isMinifyEnabled = false
      proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
      signingConfig = signingConfigs.getByName("release")
    }
  }
  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
  }
  buildFeatures {
    buildConfig = true
    compose = true
  }
  testOptions { unitTests.isReturnDefaultValues = true }
  packaging {
    resources {
      excludes += "/META-INF/LICENSE.md"
      excludes += "/META-INF/LICENSE-notice.md"
    }
  }
}

kotlin { compilerOptions { jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11) } }

dependencies {
  implementation(libs.androidx.core.ktx)
  implementation(libs.material)
  val composeBom = platform(libs.androidx.compose.bom)
  implementation(composeBom)
  androidTestImplementation(composeBom)
  implementation(libs.androidx.compose.ui)
  implementation(libs.androidx.compose.ui.graphics)
  implementation(libs.androidx.compose.ui.tooling.preview)
  implementation(libs.androidx.compose.material3)
  implementation(libs.androidx.compose.material.icons.extended)
  debugImplementation(libs.androidx.compose.ui.tooling)
  debugImplementation(libs.androidx.compose.ui.test.manifest)
  androidTestImplementation(libs.androidx.compose.ui.test.junit4)

  implementation(libs.androidx.lifecycle.runtime.ktx)
  implementation(libs.androidx.lifecycle.viewmodel.ktx)
  implementation(libs.androidx.lifecycle.runtime.compose)

  implementation(libs.androidx.activity.compose)

  implementation(libs.androidx.navigation.compose)

  implementation(libs.androidx.hilt.lifecycle.viewmodel.compose)

  implementation(libs.quartz)

  implementation(libs.androidx.datastore)
  implementation(libs.tink.android)

  implementation(libs.okhttp)
  implementation(libs.jsoup)
  implementation(libs.kotlinx.serialization.json)

  implementation(libs.coil.compose)
  implementation(libs.coil.network.okhttp)
  testImplementation(libs.coil.test)
  androidTestImplementation(libs.coil.test)

  implementation(libs.aboutlibraries.core)
  implementation(libs.aboutlibraries.compose.m3)

  implementation(libs.hilt.android)
  ksp(libs.hilt.compiler)
  // Workaround for Hilt + Kotlin 2.3.0 metadata compatibility
  // https://github.com/google/dagger/issues/5001
  annotationProcessor("org.jetbrains.kotlin:kotlin-metadata-jvm:2.3.10")

  detektPlugins("io.gitlab.arturbosch.detekt:detekt-formatting:${libs.versions.detekt.get()}")

  testImplementation(libs.junit)
  testImplementation(libs.mockk)
  testImplementation(libs.robolectric)
  testImplementation(libs.kotlinx.coroutines.test)
  androidTestImplementation(libs.androidx.junit)
  androidTestImplementation(libs.androidx.test.runner)
  androidTestImplementation(libs.kotlinx.coroutines.test)
  androidTestImplementation(libs.mockk.android)
  androidTestImplementation("com.google.dagger:hilt-android-testing:${libs.versions.hilt.get()}")
  kspAndroidTest("com.google.dagger:hilt-compiler:${libs.versions.hilt.get()}")
}

kover {
  reports {
    filters {
      excludes {
        classes(
            // Hilt generated
            "*_Hilt*",
            "Hilt_*",
            "*_HiltModules*",
            "*_Factory",
            "*_MembersInjector",
            "*_Provide*Factory",
            // BuildConfig
            "*.BuildConfig",
            // Jetpack Compose generated
            "ComposableSingletons*",
        )
      }
    }
    variant("debug") { xml { onCheck = true } }
  }
}

detekt {
  buildUponDefaultConfig = true
  allRules = false
  config.setFrom("$rootDir/config/detekt/detekt.yaml")
  baseline = file("detekt-baseline.xml")
  basePath = rootDir.absolutePath
}

tasks.withType<io.gitlab.arturbosch.detekt.Detekt>().configureEach {
  reports {
    html.required.set(true)
    xml.required.set(true)
    sarif.required.set(true)
  }
}

tasks.register<JacocoReport>("jacocoInstrumentationTestReport") {
  dependsOn("connectedDebugAndroidTest")

  reports {
    xml.required.set(true)
    html.required.set(true)
  }

  val fileFilter =
      listOf(
          "**/R.class",
          "**/R$*.class",
          "**/BuildConfig.*",
          "**/Manifest*.*",
          "**/*Test*.*",
          "**/Hilt_*",
          "**/*_Hilt*",
          "**/*_Factory*",
          "**/*_MembersInjector*",
          "**/ComposableSingletons*",
      )

  val debugTree =
      fileTree("${layout.buildDirectory.get()}/intermediates/javac/debug") { exclude(fileFilter) }
  val kotlinDebugTree =
      fileTree("${layout.buildDirectory.get()}/tmp/kotlin-classes/debug") { exclude(fileFilter) }

  sourceDirectories.setFrom(files("src/main/java", "src/main/kotlin"))
  classDirectories.setFrom(files(debugTree, kotlinDebugTree))
  executionData.setFrom(
      fileTree(layout.buildDirectory) {
        include("outputs/code_coverage/debugAndroidTest/connected/**/*.ec")
      })
}
