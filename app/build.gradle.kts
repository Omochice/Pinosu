plugins {
  alias(libs.plugins.android.application)
  alias(libs.plugins.kotlin.android)
  alias(libs.plugins.kotlin.compose)
  alias(libs.plugins.ksp)
  alias(libs.plugins.hilt)
  alias(libs.plugins.kover)
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
  kotlinOptions { jvmTarget = "11" }
  buildFeatures {
    compose = true
    viewBinding = true
  }
  testOptions { unitTests.isReturnDefaultValues = true }
  packaging {
    resources {
      excludes += "/META-INF/LICENSE.md"
      excludes += "/META-INF/LICENSE-notice.md"
    }
  }
}

dependencies {
  implementation(libs.androidx.core.ktx)
  implementation(libs.androidx.appcompat)
  implementation(libs.material)
  implementation(libs.androidx.constraintlayout)
  implementation(libs.androidx.navigation.fragment.ktx)
  implementation(libs.androidx.navigation.ui.ktx)

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

  implementation(libs.androidx.hilt.navigation.compose)

  implementation(libs.quartz)

  implementation(libs.androidx.security.crypto)

  implementation(libs.okhttp)
  implementation(libs.jsoup)

  implementation(libs.hilt.android)
  ksp(libs.hilt.compiler)

  testImplementation(libs.junit)
  testImplementation(libs.mockk)
  testImplementation(libs.robolectric)
  testImplementation(libs.kotlinx.coroutines.test)
  androidTestImplementation(libs.androidx.junit)
  androidTestImplementation(libs.androidx.espresso.core)

  androidTestImplementation(libs.mockk)
  androidTestImplementation("com.google.dagger:hilt-android-testing:${libs.versions.hilt.get()}")
  kspAndroidTest("com.google.dagger:hilt-compiler:${libs.versions.hilt.get()}")
}

kover {
  reports {
    variant("debug") {
      xml {
        onCheck = false
        xmlFile = layout.buildDirectory.file("reports/kover/coverage.xml")
      }
      html {
        onCheck = false
        htmlDir = layout.buildDirectory.dir("reports/kover/html")
      }
    }
  }
}
