# WireFM ProGuard / R8 Rules

# Preserve Gson serializable model classes & annotated fields
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}
-keep class com.wirefm.app.model.** { *; }

# OkHttp & Okio
-dontwarn okhttp3.**
-dontwarn okio.**
-keepattributes Signature
-keepattributes *Annotation*

# CameraX & ZXing
-dontwarn androidx.camera.**
-keep class androidx.camera.core.** { *; }
-keep class androidx.camera.camera2.** { *; }
-keep class com.google.zxing.** { *; }

# Jetpack Compose
-dontwarn androidx.compose.**
