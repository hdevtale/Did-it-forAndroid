# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Keep line numbers for better crash reports
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Keep all model classes - CRITICAL for data persistence
-keep class com.harshal.didit.Task { *; }
-keep class com.harshal.didit.TaskLog { *; }
-keep class com.harshal.didit.TaskRepository { *; }
-keep class com.harshal.didit.TaskAdapter { *; }

# Keep UUID serialization for Task IDs
-keep class java.util.UUID { *; }
-keep class java.util.UUID$Holder { *; }

# Keep all data classes and their fields
-keepclassmembers class com.harshal.didit.Task {
    <fields>;
    <init>(...);
}

# Keep all widget classes
-keep class com.harshal.didit.SimpleTaskWidget { *; }
-keep class com.harshal.didit.SimpleTaskListWidget { *; }
-keep class com.harshal.didit.SimpleWidgetLogReceiver { *; }
-keep class com.harshal.didit.MinimalTaskWidget { *; }
-keep class com.harshal.didit.WidgetTaskAdapter { *; }

# Keep all receiver classes
-keep class com.harshal.didit.ReminderReceiver { *; }
-keep class com.harshal.didit.NotificationActionReceiver { *; }

# Keep all activity classes
-keep class com.harshal.didit.MainActivity { *; }
-keep class com.harshal.didit.LauncherActivity { *; }
-keep class com.harshal.didit.ThemeSelectionActivity { *; }
-keep class com.harshal.didit.SimpleTaskWidgetConfigureActivity { *; }

# Keep all dialog fragment classes
-keep class com.harshal.didit.AddTaskDialogFragment { *; }
-keep class com.harshal.didit.SetReminderDialogFragment { *; }

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

# CRITICAL: Keep all data classes and their serialization
-keepclassmembers,allowshrinking,allowobfuscation class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

# Keep all enum classes
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# Keep Parcelable classes
-keep class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator *;
}

# Keep Material Design classes
-keep class com.google.android.material.** { *; }

# Keep AndroidX classes
-keep class androidx.** { *; }

# CRITICAL: Keep SharedPreferences keys and values
-keepclassmembers class * {
    public static final java.lang.String *;
}

# Keep all string constants that might be used as SharedPreferences keys
-keepclassmembers class com.harshal.didit.TaskRepository {
    public static final java.lang.String *;
}

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}