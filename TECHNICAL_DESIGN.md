# Tech-a-Breath: Technical Design & Architectural Deep-Dive

## 1. Core Philosophy
Tech-a-Breath is built on three non-negotiable pillars:
- **Zero-Latency Response**: For an acoustic shield to be effective against PTSD triggers, the delay between sound detection and audio masking must be imperceptible (aiming for sub-800ms).
- **Privacy by Design**: Audio is processed in volatile memory as floating-point tensors and immediately discarded. No audio data ever touches the disk or the network.
- **Battery Sustainability**: "Always-on" monitoring must not render the device unusable. We use a gated monitoring approach to minimize CPU wake-ups.

---

## 2. System Architecture: The Dual-Stage Pipeline

The most critical challenge was balancing AI accuracy with battery life. We solved this by implementing a **Gated Execution Pipeline**.

### Stage 1: The Sentinel (RMS Gating)
Instead of running a heavy Deep Neural Network (DNN) 24/7, the system uses a lightweight `Sentinel` phase.
- **Implementation**: The system captures 100ms PCM audio buffers and calculates the Root Mean Square (RMS) to determine decibel levels.
- **Logic**: If the ambient noise is below -50dB, the process stops here. The AI model is not invoked, saving significant power.
- **Benefit**: Reduces CPU utilization by ~85% in quiet environments.

### Stage 2: The Classifier (TensorFlow Lite + YAMNet)
Once the `Sentinel` detects a volume spike, the gates open for the AI.
- **Model**: We utilize **YAMNet**, a pre-trained deep net that recognizes 521 audio classes.
- **Context Window**: We feed 0.975s of audio into the model to ensure high-fidelity classification.
- **Sensitivity & Precision**: We have implemented internal, pre-tuned confidence thresholds for each trigger type to minimize false positives (e.g., from music or ringtones) while maintaining a high safety rating for emergency sounds.
- **Optimization**: We use the **XNNPACK Delegate** to accelerate inference using mobile-optimized CPU instructions.

---

## 3. Advanced Detection Logic (Temporal Smoothing)

Environmental sounds are messy. A car horn or a bird chirp can sometimes mimic a siren. To solve this, we implemented **Temporal Smoothing (Voting System)**.

- **The Problem**: Raw AI output can "flicker" between classes.
- **The Solution**: The `MonitoringService` maintains a sliding history window (last 5 detections).
- **Heuristic**:
    - **Ambulance/Siren**: Requires a **3-out-of-5** match (sustained sound).
    - **Dog Bark**: Requires a **2-out-of-5** match (sudden/impact sound).
- **Exclusion Filters**: We implemented a "Top-Sound Exclusion" layer. If the AI identifies "Music" or "Ringtone" as the primary sound, it auto-rejects any secondary "Siren" classifications to prevent false triggers from phones or car radios.

---

## 4. Audio Masking Engine

The masking engine is built on **Media3 (ExoPlayer)** for high-performance, low-overhead playback.

- **Cross-Fading Logic**: Sudden noise can be just as startling as a trigger. When a trigger is detected, the `AudioOutputManager` starts the masking sound at 0% volume and performs a linear fade-in over 1.5 seconds.
- **Masking Volume**: Users can set a custom volume level for the masking sound (white noise, brown noise, or music) independently for each trigger. This ensures the masking is loud enough to be effective without being overwhelming.
- **Dynamic Masking**: Users can choose between **White Noise** (broad spectrum), **Brown Noise** (deeper, bass-heavy), and **Calming Music**.
- **Headphone Requirement**: For safety and effectiveness, the app uses a `HeadphoneManager` to ensure masking is only active when the user is wearing headphones, preventing the app from blasting audio through external speakers in public.

---

## 5. Key Features & Implementation Attitudes

### 🟢 Active Protection (Foreground Service)
The core of the app runs as a `Foreground Service`. This ensures the Android OS gives it high priority and prevents it from being killed by battery optimizers. It uses the `microphone` usage type to stay compliant with modern Android permission models.

### 🛡️ Notification Interventions
When protection is active, the app creates a "High Importance" notification. 
- **User Agency**: Buttons like **"You are safe now"** (stops masking) and **"1m more"** (extends masking duration) are available directly on the lock screen.
- **Reassurance**: The notification explicitly states what was detected (e.g., "Protection Active: Ambulance") to ground the user.

### 📊 Room-Based History & Analytics
Every trigger event is logged into a local **Room Database**.
- **Data Points**: Detection time, trigger type, applied masking level, and latency.
- **Future Ready**: This data allows the app to show the user their progress over weeks/months, turning a reactive tool into a proactive wellness tracker.

---

## 6. Implementation Advantages

1. **Deterministic Latency**: By using fixed-size buffers and pre-allocated Tensors, we avoid the "Stop-the-world" Garbage Collection pauses that can lag audio apps.
2. **Offline-First**: The app works in a basement, a forest, or an airplane. Zero internet dependency.
3. **Calming Design Attitude**: The UI uses a "Slate & Indigo" palette with rounded corners and soft transitions, designed specifically to be non-triggering for someone in a high-stress state.

---
*Tech-a-Breath: Engineering tranquility through AI.*
