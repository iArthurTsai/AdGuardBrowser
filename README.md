# AdGuardBrowser
A zero-config, lightweight Android WebView browser designed for legacy tablets. It leverages Proxy Override to route intranet traffic through Tinyproxy &amp; AdGuard Home on a Debian server for seamless ad-blocking, with an automatic fallback to Chrome when away from home.

---

```markdown
# AdGuard Home Client Browser (Legacy Android Support)

A lightweight, zero-configuration Android WebView browser designed for legacy devices (like the Huawei MatePad T5). It automatically routes your browsing traffic through a local Linux proxy (Tinyproxy) to your self-hosted AdGuard Home when you are at home, and seamlessly falls back to Google Chrome when you leave the network.

---

## 📱 Supported Android Version

This project is highly optimized for legacy hardware:
*   **Minimum SDK:** `26` (Android 8.0 Oreo)
*   **Target SDK:** `36`
*   **Compile SDK:** `37`

> **Note on WebView:** In Android 8.0, the system automatically disables the standalone "Android System WebView" if Google Chrome is installed, routing all WebView requests through the Chrome engine. Upgrading Chrome via the Play Store automatically updates the underlying browser rendering engine for this app!

---

## ⚠️ CRITICAL CONFIGURATION (Do This First!)

Before building the APK, you **must** configure the App to point to your own server IP.

1. Open `MainActivity.kt`.
2. Locate the following lines at the top of the class:
   ```kotlin
   // 🔴 CHANGE THESE TO YOUR OWN SERVER IP AND PORTS
   private val mbpIp = "192.168.50.XXX" // <-- Replace with your AdGuard Home / Debian Server IP!
   private val mbpPort = 53             // <-- Port used to check if you are at home

```

3. Change `"192.168.50.XXX"` to the local IP address of your AdGuard Home server.

---

## 📂 The 3 Core Files You Need

To replicate this project or migrate it to a new template, you only need to copy and paste these **3 core files** into your Android Studio project.

| File Name | Target Path in Project | Purpose |
| --- | --- | --- |
| **`MainActivity.kt`** | `app/src/main/java/your/package/name/MainActivity.kt` | Handles intranet detection, UI creation, proxy binding, and fallback redirection. |
| **`build.gradle.kts`** | `app/build.gradle.kts` (Module: app) | Manages dependencies (requires `androidx.webkit:webkit:1.12.1`) and SDK versioning. |
| **`AndroidManifest.xml`** | `app/src/main/AndroidManifest.xml` | Declares Internet permissions and enables `usesCleartextTraffic` for HTTP proxies. |

> 💡 **Important:** When copying `MainActivity.kt` into a new project, make sure to change the very first line (`package com.example.adguardbrowser`) to match your new project's package name!

---

## 🛠️ Step-by-Step Setup & Build Guide

Follow these steps to build your own去廣告瀏覽器 APK:

### Step 1: Create a New Project

1. Open **Android Studio**.
2. Click **New Project** and select **Empty Views Activity**.
3. Name your project (e.g., `AdGuardBrowser`) and select **Kotlin** as the language.
4. Click **Finish** and wait for the project to initialize.

### Step 2: Set Up Dependencies

1. Open `app/build.gradle.kts` (Module: app) and paste your configured Gradle settings.
2. Ensure `implementation("androidx.webkit:webkit:1.16.0")` is included under `dependencies`.
3. Click **Sync Now** in the top right yellow bar.

### Step 3: Set Up Manifest & Permissions

1. Open `app/src/main/AndroidManifest.xml`.
2. Add the internet permission tag above the `<application>` tag:
```xml
<uses-permission android:name="android.permission.INTERNET" />

```


3. Inside the `<application>` tag, add the cleartext traffic attribute:
```xml
android:usesCleartextTraffic="true"

```



### Step 4: Paste Core Logic & Edit Server IP

1. Open your `MainActivity.kt`.
2. Replace its entire contents with the provided `MainActivity.kt` code.
3. Update the package name at the top to match yours.
4. **Change the `mbpIp` to your AdGuard Home / Debian server IP.**

### Step 5: Build the APK

1. In the top menu, go to **Build** -> **Build Bundle(s) / APK(s)** -> **Build APK(s)**.
2. Wait for Android Studio to compile the project.
3. Once completed, click **Locate** in the popup notification at the bottom right.
4. Transfer the `app-debug.apk` to your legacy tablet and install it!

---

## 🖥️ Server-Side Requirement (MBP / Debian)

For this browser to block ads, your server must have a running HTTP Proxy (e.g., **Tinyproxy**) that routes DNS requests through **AdGuard Home**.

Ensure your Tinyproxy configuration (`/etc/tinyproxy/tinyproxy.conf`) allows connections from your home subnet:

```text
Allow 192.168.50.0/24  # Replace with your local Wi-Fi subnet

```

And make sure your server's system DNS `/etc/resolv.conf` is pointed to `127.0.0.1` so that all proxy traffic is automatically filtered by AdGuard Home.

```

```
