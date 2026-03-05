package io.github.omochice.pinosu.architecture

import com.tngtech.archunit.core.importer.ClassFileImporter
import com.tngtech.archunit.core.importer.ImportOption
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses
import org.junit.Test

class DependencyDirectionTest {

  private val classes =
      ClassFileImporter()
          .withImportOption(ImportOption.DoNotIncludeTests())
          .importPackages("io.github.omochice.pinosu")

  @Test
  fun `core must not depend on feature`() {
    noClasses()
        .that()
        .resideInAPackage("io.github.omochice.pinosu.core..")
        .should()
        .dependOnClassesThat()
        .resideInAPackage("io.github.omochice.pinosu.feature..")
        .check(classes)
  }

  @Test
  fun `feature domain must not depend on data`() {
    noClasses()
        .that()
        .resideInAPackage("io.github.omochice.pinosu.feature.*.domain..")
        .should()
        .dependOnClassesThat()
        .resideInAPackage("io.github.omochice.pinosu.feature.*.data..")
        .check(classes)
  }

  @Test
  fun `feature domain must not depend on presentation`() {
    noClasses()
        .that()
        .resideInAPackage("io.github.omochice.pinosu.feature.*.domain..")
        .should()
        .dependOnClassesThat()
        .resideInAPackage("io.github.omochice.pinosu.feature.*.presentation..")
        .check(classes)
  }
}
