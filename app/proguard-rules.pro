# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Keep line numbers for better crash reports
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Keep all model classes
-keep class com.harshal.didit.Task { *; }
-keep class com.harshal.didit.TaskLog { *; }

# Keep all widget classes
-keep class com.harshal.didit.SimpleTaskWidget { *; }
-keep class com.harshal.didit.SimpleTaskListWidget { *; }
-keep class com.harshal.didit.SimpleWidgetLogReceiver { *; }

# Keep all receiver classes
-keep class com.harshal.didit.ReminderReceiver { *; }

# Keep all activity classes
-keep class com.harshal.didit.MainActivity { *; }
-keep class com.harshal.didit.LauncherActivity { *; }
-keep class com.harshal.didit.ThemeSelectionActivity { *; }
-keep class com.harshal.didit.SimpleTaskWidgetConfigureActivity { *; }

# Keep all preference classes
-keep class com.harshal.didit.ThemePreferences { *; }
-keep class com.harshal.didit.AppPreferences { *; }

# Keep Gson classes
-keepattributes Signature
-keepattributes *Annotation*
-dontwarn sun.misc.**
-keep class com.google.gson.** { *; }
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer

# Keep Material Design classes
-keep class com.google.android.material.** { *; }

# Keep AndroidX classes
-keep class androidx.** { *; }

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}