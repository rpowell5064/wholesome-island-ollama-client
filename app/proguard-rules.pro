# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.

# Kotlin Metadata & Reflection (Crucial for Moshi + KotlinJsonAdapterFactory)
-keep class kotlin.Metadata { *; }
-keep class kotlin.reflect.jvm.internal.** { *; }
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations, Signature, InnerClasses, EnclosingMethod

# Moshi rules
-keep class com.squareup.moshi.** { *; }
-keep @com.squareup.moshi.JsonQualifier interface *
-keepclassmembers class * {
    @com.squareup.moshi.FromJson *;
    @com.squareup.moshi.ToJson *;
}

# Retrofit rules
-keep class retrofit2.** { *; }
-keep interface retrofit2.** { *; }
-dontwarn retrofit2.**

# Jsoup rules
-keep class org.jsoup.** { *; }

# OkHttp rules
-keep class okhttp3.** { *; }
-dontwarn okhttp3.**

# Keep our app's data models to prevent obfuscation issues with Moshi
-keep class com.wholesomeisland.ollamaclient.data.remote.** { *; }
-keep class com.wholesomeisland.ollamaclient.ui.theme.SearchEngineConfig { *; }
-keep class com.wholesomeisland.ollamaclient.ui.theme.SearchEngineType { *; }
-keep class com.wholesomeisland.ollamaclient.ui.theme.ChatUiMessage { *; }
-keep class com.wholesomeisland.ollamaclient.ui.theme.QuickAction { *; }

# JSpecify annotations
-dontwarn org.jspecify.annotations.**
