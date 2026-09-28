# Keep line numbers for debugging stack traces (only for crash analysis, not debuggable build)
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Kotlin metadata (used by reflection; harmless to keep)
-keep class kotlin.Metadata { *; }
-keepclassmembers class **$WhenMappings {
    <fields>;
}

# Compose
-dontwarn androidx.compose.**

# WebView JavaScript interface (none used, but keep safe)
-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}
