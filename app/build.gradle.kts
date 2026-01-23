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
}

android {
  namespace = "io.github.omochice.pinosu"
  compileSdk = 36

  defaultConfig {
    applicationId = "io.github.omochice.pinosu"
    minSdk = 26
    targetSdk = 36
    versionCode = 1
    versionName = "1.0"

    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
  }

  buildTypes {
    release {
      isMinifyEnabled = false
      proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
    }
  }
  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
  }
  buildFeatures { compose = true }
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

  implementation(libs.androidx.lifecycle.runtime.ktx)
  implementation(libs.androidx.lifecycle.viewmodel.ktx)
  implementation(libs.androidx.lifecycle.runtime.compose)

  implementation(libs.androidx.activity.compose)

  implementation(libs.androidx.navigation.compose)

  implementation(libs.androidx.hilt.navigation.compose)

  implementation(libs.quartz)

  implementation(libs.androidx.security.crypto)
  implementation(libs.androidx.datastore)
  implementation(libs.tink.android)

  implementation(libs.okhttp)
  implementation(libs.jsoup)
  implementation(libs.kotlinx.serialization.json)

  implementation(libs.aboutlibraries.core)
  implementation(libs.aboutlibraries.compose.m3)

  implementation(libs.hilt.android)
  ksp(libs.hilt.compiler)
  // Workaround for Hilt + Kotlin 2.3.0 metadata compatibility
  // https://github.com/google/dagger/issues/5001
  annotationProcessor("org.jetbrains.kotlin:kotlin-metadata-jvm:2.3.0")

  detektPlugins("io.gitlab.arturbosch.detekt:detekt-formatting:${libs.versions.detekt.get()}")

  testImplementation(libs.junit)
  testImplementation(libs.mockk)
  testImplementation(libs.robolectric)
  testImplementation(libs.kotlinx.coroutines.test)
  androidTestImplementation(libs.androidx.junit)
  androidTestImplementation(libs.kotlinx.coroutines.test)
  androidTestImplementation(libs.mockk)
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
