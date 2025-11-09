# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to the flags specified
# in /usr/local/share/android-sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.kts.

-keep class kotlinx.serialization.** { *; }
-keepclassmembers class ** extends kotlinx.serialization.KSerializer { *; }
