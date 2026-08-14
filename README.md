# Myra — Real-Time Voice AI Companion 🎙️✨

Myra is a real-time voice-to-voice AI companion and personal assistant powered by Google Gemini, Android SpeechRecognizer, and natural Text-to-Speech synthesis.

## Features
- **Real-Time Voice Conversation**: Hands-free voice recognition with dynamic speech synthesis.
- **Dynamic Personalities & Vibes**: Sassy, Flirty, Playful, Confident, Sweet, and Teasing modes.
- **Glowing Visualizer & Equalizer**: Responsive glowing orb and waveform audio visualizer.
- **Browser & App Automation**: Automatically opens YouTube, Instagram, Spotify, or Google searches on command.
- **Continuous Mode**: Natural conversational flow without pressing buttons repeatedly.

---

## 🚀 Instant APK Build via GitHub
When you push this repository to GitHub:
1. GitHub Actions automatically triggers `.github/workflows/build-apk.yml`.
2. Go to the **Actions** tab on your GitHub repository.
3. Select the latest build workflow and download **`Myra-Voice-Assistant-APK`** from the **Artifacts** section.
4. Install the `.apk` directly on any Android device (Android 7.0+ / SDK 24+).

---

## 🛠️ Local Build Instructions

### Prerequisites
- Android Studio Ladybug (2024.2.1+) or newer
- JDK 17+
- Android SDK 36 (minSdk 24)

### Setup & Run
1. Clone this repository:
   ```bash
   git clone <your-repo-url>
   cd <repo-folder>
   ```
2. Set your Gemini API Key in `.env`:
   ```bash
   cp .env.example .env
   # Add your Gemini API key:
   # GEMINI_API_KEY=your_actual_key_here
   ```
3. Build the APK via command line:
   ```bash
   ./gradlew assembleDebug
   ```
   The APK will be generated at: `app/build/outputs/apk/debug/app-debug.apk`
4. Or simply open the project in **Android Studio** and click **Run** (▶️).
