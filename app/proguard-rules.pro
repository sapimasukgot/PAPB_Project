-keepclassmembers class * extends androidx.room.RoomDatabase {
    <init>();
}
-keep class com.example.easycooking.data.local.entity.** { *; }
-dontwarn javax.crypto.**
