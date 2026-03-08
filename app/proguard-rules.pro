# Jsoup references com.google.re2j as an optional dependency.
# Since the app does not include re2j, suppress the missing-class warnings.
-dontwarn com.google.re2j.Matcher
-dontwarn com.google.re2j.Pattern

# Preserve source file names and line numbers for readable crash stack traces.
-keepattributes SourceFile,LineNumberTable
