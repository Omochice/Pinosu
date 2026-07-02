package io.github.omochice.pinosu.architecture

import com.lemonappdev.konsist.api.Konsist
import com.lemonappdev.konsist.api.architecture.KoArchitectureCreator.assertArchitecture
import com.lemonappdev.konsist.api.architecture.Layer
import kotlin.test.Test

class DependencyDirectionTest {

  @Test
  fun `core must not depend on feature`() {
    Konsist.scopeFromProduction().assertArchitecture {
      val core = Layer("Core", "io.github.omochice.pinosu.core..")
      val feature = Layer("Feature", "io.github.omochice.pinosu.feature..")
      core.doesNotDependOn(feature)
    }
  }

  // Konsist Layer patterns allow ".." only at the start or end, so
  // "feature.*.domain.." cannot be expressed directly. Segment-based patterns
  // are equivalent here because domain/data/presentation packages exist only
  // under feature.*.
  @Test
  fun `feature domain must not depend on data`() {
    Konsist.scopeFromProduction().assertArchitecture {
      val domain = Layer("Domain", "..domain..")
      val data = Layer("Data", "..data..")
      domain.doesNotDependOn(data)
    }
  }

  @Test
  fun `feature domain must not depend on presentation`() {
    Konsist.scopeFromProduction().assertArchitecture {
      val domain = Layer("Domain", "..domain..")
      val presentation = Layer("Presentation", "..presentation..")
      domain.doesNotDependOn(presentation)
    }
  }
}
