# Setting Up Android SDK

Your build is failing because the Android SDK is not found. Here's how to fix it:

## Option 1: Install Android Studio (Recommended - Easiest)

Android Studio includes the Android SDK and all necessary tools.

### Steps:

1. **Download Android Studio:**
   - Visit: https://developer.android.com/studio
   - Download for macOS
   - The file is large (~1GB), so it may take a while

2. **Install Android Studio:**
   - Open the downloaded `.dmg` file
   - Drag Android Studio to Applications
   - Launch Android Studio

3. **First-time Setup:**
   - Android Studio will guide you through setup
   - It will automatically download the Android SDK
   - The SDK will be installed to: `~/Library/Android/sdk`

4. **Create local.properties:**
   After Android Studio installs the SDK, create a `local.properties` file in your project root:
   
   ```bash
   echo "sdk.dir=$HOME/Library/Android/sdk" > local.properties
   ```

5. **Verify:**
   ```bash
   ./gradlew build
   ```

## Option 2: Install Android SDK Command Line Tools (Without Android Studio)

If you don't want the full Android Studio IDE:

### Steps:

1. **Download Command Line Tools:**
   ```bash
   # Create SDK directory
   mkdir -p ~/Library/Android/sdk
   
   # Download command line tools
   cd ~/Library/Android/sdk
   curl -o cmdline-tools.zip https://dl.google.com/android/repository/commandlinetools-mac-11076708_latest.zip
   
   # Extract
   unzip cmdline-tools.zip
   mkdir -p cmdline-tools/latest
   mv cmdline-tools/* cmdline-tools/latest/ 2>/dev/null || true
   ```

2. **Install SDK Components:**
   ```bash
   export ANDROID_HOME=~/Library/Android/sdk
   export PATH=$PATH:$ANDROID_HOME/cmdline-tools/latest/bin
   export PATH=$PATH:$ANDROID_HOME/platform-tools
   
   # Accept licenses and install SDK
   yes | sdkmanager --licenses
   sdkmanager "platform-tools" "platforms;android-34" "build-tools;34.0.0"
   ```

3. **Create local.properties:**
   ```bash
   echo "sdk.dir=$HOME/Library/Android/sdk" > local.properties
   ```

4. **Add to ~/.zshrc:**
   ```bash
   echo 'export ANDROID_HOME=$HOME/Library/Android/sdk' >> ~/.zshrc
   echo 'export PATH=$PATH:$ANDROID_HOME/cmdline-tools/latest/bin' >> ~/.zshrc
   echo 'export PATH=$PATH:$ANDROID_HOME/platform-tools' >> ~/.zshrc
   source ~/.zshrc
   ```

## Quick Fix (If Android Studio is Already Installed)

If you already have Android Studio installed but the SDK path isn't set:

1. **Find your SDK location:**
   - Open Android Studio
   - Go to: Android Studio → Settings → Appearance & Behavior → System Settings → Android SDK
   - Copy the "Android SDK Location" path

2. **Create local.properties:**
   ```bash
   echo "sdk.dir=/path/to/your/sdk" > local.properties
   ```
   
   Replace `/path/to/your/sdk` with the actual path from Android Studio.

## Verify Setup

After setting up, verify everything works:

```bash
# Check if SDK is found
./gradlew tasks

# Or try building
./gradlew build
```

## Troubleshooting

### Issue: "SDK location not found" still appears
- **Solution:** Make sure `local.properties` is in the project root (same directory as `build.gradle.kts`)
- Check that the path in `local.properties` is correct
- Use absolute path, not relative path

### Issue: "SDK platform not found"
- **Solution:** Install the required SDK platform (Android 34 in your case)
- In Android Studio: Tools → SDK Manager → SDK Platforms → Check Android 13.0 (API 34)

### Issue: "Build tools not found"
- **Solution:** Install build tools
- In Android Studio: Tools → SDK Manager → SDK Tools → Check Android SDK Build-Tools

## Next Steps

Once the SDK is set up:
1. ✅ Build the project: `./gradlew build`
2. ✅ Run the scraper tests
3. ✅ Install the app: `./gradlew installDebug`

