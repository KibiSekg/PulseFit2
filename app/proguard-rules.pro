# Add project specific ProGuard rules here.
# Firestore model classes need their no-arg constructors and fields kept for reflection.
-keepclassmembers class com.pulsefit.app.data.model.** {
    <init>(...);
    <fields>;
}
