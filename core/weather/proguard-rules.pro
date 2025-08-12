# Keep data classes for Moshi
-keepclasseswithmembers class ** {
    @com.squareup.moshi.JsonAdapter <fields>;
}

