# Tech-a-Breath: AI Acoustic Monitoring & Optimization

This document details the architecture of the real-time PTSD trigger detection system and the battery optimization strategies implemented in the app.

---

## 1. Dual-Stage Monitoring Architecture

To provide "Always-on" protection without draining the battery, the system operates in two distinct stages:

### Stage 1: The "Sentinel" (Low-Power Volume Check)
*   **Mechanism**: Continuously monitors raw PCM audio from the microphone.
*   **Logic**: Calculates the **Root Mean Square (RMS)** of the audio buffer and converts it to **Decibels (dB)**.
*   **Power Saving**: If the volume is below a user-defined threshold (e.g., -50 dB), the system performs no further processing. The heavy AI model remains dormant, saving approximately 80-90% of CPU cycles during quiet periods.

### Stage 2: The "Classifier" (Precision AI)
*   **Mechanism**: Triggered only when Stage 1 detects a volume spike.
*   **AI Model**: Uses **YAMNet** (TensorFlow Lite), a deep neural network that can recognize 521 different audio classes.
*   **Latency**: Processed in **100ms chunks** to ensure the total response time (detection to masking) stays well below the **1-second red-line**.

---

## 2. Trigger Precision & False Positive Reduction

The app is tuned to ignore "Nature" sounds (birds, wind) and focus strictly on PTSD triggers using three layers of logic:

### A. Specific Keyword Mapping
The system maps YAMNet labels to internal trigger groups:
*   **Sirens**: Siren, Ambulance, Police Car, Fire Engine, Emergency Vehicle, Alarm.
*   **Dog Barks**: Barking, Howl, Dog, Growling, Bow-wow.
*   **Motorcycles**: Motorcycle, Motorbike.

### B. Temporal Smoothing (2-out-of-3 Rule)
To prevent "random" noises from triggering the masking engine:
*   For **Continuous sounds** (Sirens/Motorcycles), the AI must detect the sound in **at least 2 out of the last 3** 1-second windows.
*   This ensures that a brief "blip" or a misclassified bird chirp doesn't cause a false alarm.

### C. Impact Sound Handling
*   For **Short sounds** (Dog Barks/Fireworks), the system is configured to trigger **instantly** (1-out-of-3) once a high-confidence bark is detected, prioritizing safety for sudden triggers.

---

## 3. Battery Optimization Techniques

1.  **XNNPACK Acceleration**: Enabled the XNNPACK delegate in TFLite to utilize optimized CPU instructions (SIMD), reducing inference time and heat generation.
2.  **Async Initialization**: The AI model is loaded into memory on a background thread during the Settings screen, preventing the main UI thread from being blocked.
3.  **Variable Polling**: The system sleeps for 500ms-700ms between checks during the Sentinel phase, reducing the "wake-up" frequency of the CPU.
4.  **Foreground Service Type**: Uses `foregroundServiceType="microphone"` to comply with Android power management policies while maintaining active listening when the screen is off.

---

## 4. Current Configuration
*   **Sample Rate**: 16,000 Hz (Mono)
*   **Inference Window**: 0.975 seconds
*   **Volume Threshold**: -50.0 dB
*   **Inference Threshold**: 18% (Siren) / 20% (Bark) / 30% (Others)
