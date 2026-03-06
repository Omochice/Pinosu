package io.github.omochice.pinosu.architecture

import com.tngtech.archunit.core.importer.ImportOption
import com.tngtech.archunit.junit.AnalyzeClasses
import com.tngtech.archunit.junit.ArchTest
import com.tngtech.archunit.junit.ArchUnitRunner
import com.tngtech.archunit.lang.ArchRule
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses
import org.junit.runner.RunWith

@RunWith(ArchUnitRunner::class)
@AnalyzeClasses(
    packages = ["io.github.omochice.pinosu"],
    importOptions = [ImportOption.DoNotIncludeTests::class],
)
class DependencyDirectionTest {

  @ArchTest
  val `core must not depend on feature`: ArchRule =
      noClasses()
          .that()
          .resideInAPackage("io.github.omochice.pinosu.core..")
          .should()
          .dependOnClassesThat()
          .resideInAPackage("io.github.omochice.pinosu.feature..")

  @ArchTest
  val `feature domain must not depend on data`: ArchRule =
      noClasses()
          .that()
          .resideInAPackage("io.github.omochice.pinosu.feature.*.domain..")
          .should()
          .dependOnClassesThat()
          .resideInAPackage("io.github.omochice.pinosu.feature.*.data..")

  @ArchTest
  val `feature domain must not depend on presentation`: ArchRule =
      noClasses()
          .that()
          .resideInAPackage("io.github.omochice.pinosu.feature.*.domain..")
          .should()
          .dependOnClassesThat()
          .resideInAPackage("io.github.omochice.pinosu.feature.*.presentation..")
}
