# Installing Java JDK on macOS

Your project requires **Java 17** (JDK 17) to build and run Gradle commands.

## Method 1: Using Homebrew (Recommended)

Since you have Homebrew installed, run these commands in your terminal:

### Step 1: Install Java 17
```bash
brew install --cask temurin@17
```

This will prompt you for your password. Enter your macOS password when asked.

### Step 2: Set up Java environment variables

After installation, add Java to your PATH. Add these lines to your `~/.zshrc` file:

```bash
# Add Java 17 to PATH
export JAVA_HOME=$(/usr/libexec/java_home -v 17)
export PATH="$JAVA_HOME/bin:$PATH"
```

Then reload your shell configuration:
```bash
source ~/.zshrc
```

### Step 3: Verify installation
```bash
java -version
```

You should see something like:
```
openjdk version "17.0.x"
OpenJDK Runtime Environment Temurin-17.0.x+xx
OpenJDK 64-Bit Server VM Temurin-17.0.x+xx
```

## Method 2: Manual Download (Alternative)

If Homebrew doesn't work, you can download directly:

1. **Download Java 17:**
   - Visit: https://adoptium.net/temurin/releases/?version=17
   - Select macOS → ARM64 (for Apple Silicon) or x64 (for Intel)
   - Download the `.pkg` installer

2. **Install:**
   - Double-click the downloaded `.pkg` file
   - Follow the installation wizard

3. **Set JAVA_HOME:**
   ```bash
   export JAVA_HOME=$(/usr/libexec/java_home -v 17)
   export PATH="$JAVA_HOME/bin:$PATH"
   ```

## Method 3: Using SDKMAN (Alternative)

If you prefer SDKMAN for managing multiple Java versions:

```bash
# Install SDKMAN
curl -s "https://get.sdkman.io" | bash
source "$HOME/.sdkman/bin/sdkman-init.sh"

# Install Java 17
sdk install java 17.0.10-tem
```

## After Installation

Once Java is installed, you can:

1. **Build your project:**
   ```bash
   ./gradlew build
   ```

2. **Run Gradle tasks:**
   ```bash
   ./gradlew tasks
   ```

3. **Install the app:**
   ```bash
   ./gradlew installDebug
   ```

## Troubleshooting

### Issue: "java: command not found"
- **Solution:** Make sure you've added Java to your PATH and reloaded your shell

### Issue: Wrong Java version
- **Solution:** Check which Java is being used:
  ```bash
  which java
  java -version
  ```
  If it's not Java 17, update your PATH or use:
  ```bash
  export JAVA_HOME=$(/usr/libexec/java_home -v 17)
  ```

### Issue: "JAVA_HOME not set"
- **Solution:** Add the export commands to your `~/.zshrc` file as shown above

## Quick Install Command

Run this in your terminal (it will ask for your password):

```bash
brew install --cask temurin@17 && \
echo 'export JAVA_HOME=$(/usr/libexec/java_home -v 17)' >> ~/.zshrc && \
echo 'export PATH="$JAVA_HOME/bin:$PATH"' >> ~/.zshrc && \
source ~/.zshrc && \
java -version
```

This will:
1. Install Java 17
2. Add it to your PATH
3. Reload your shell
4. Verify the installation

