# Project specific ProGuard rules

#--------------- GCM proguard configuration ------------------
-keep class * extends java.util.ListResourceBundle {
    protected Object[][] getContents();
}
-keep public class com.google.android.gms.common.internal.safeparcel.SafeParcelable {
    public static final *** NULL;
}
-keepnames @com.google.android.gms.common.annotation.KeepName class *
-keepclassmembernames class * {
    @com.google.android.gms.common.annotation.KeepName *;
}

#-------------- ProGuard settings for serialization --------------
-keepnames class * implements android.os.Parcelable {
    public static final ** CREATOR;
}
-keepclassmembers enum *
-keep public class me.avelar.donee.view.** {
    public *;
    protected *;
}
-keep class me.avelar.donee.model.** { *; }

-keepattributes Signature
-keepattributes *Annotation*

-keep class com.google.gson.**