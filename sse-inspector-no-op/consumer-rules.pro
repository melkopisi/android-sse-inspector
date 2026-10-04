# SSE Inspector No-Op ProGuard / R8 Rules

# Preserve public API signatures so consumer release calls don't break
-keep class me.melkopisi.sseinspector.SseInspector { *; }
-keep class me.melkopisi.sseinspector.SseInspectorNoOp { *; }

-dontwarn me.melkopisi.sseinspector.**
