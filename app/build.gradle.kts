plugins { id("com.android.application") }
android {
    namespace = "br.edu.librasfacil"
    compileSdk = 37
    defaultConfig { applicationId = "br.edu.librasfacil"; minSdk = 26; targetSdk = 37; versionCode = 1; versionName = "0.1" }
    compileOptions { sourceCompatibility = JavaVersion.VERSION_17; targetCompatibility = JavaVersion.VERSION_17 }
}
