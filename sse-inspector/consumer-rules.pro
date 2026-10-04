# SSE Inspector ProGuard / R8 Rules

# Preserve public API
-keep class me.melkopisi.sseinspector.SseInspector { *; }
-keep class me.melkopisi.sseinspector.SseInspectorImpl { *; }

# Preserve data models
-keep class me.melkopisi.sseinspector.model.** { *; }

# Preserve inspector UI Activity
-keep class me.melkopisi.sseinspector.ui.SseInspectorActivity { *; }

# Suppress OkHttp warnings if consumers don't provide all optional dependencies
-dontwarn okhttp3.**
