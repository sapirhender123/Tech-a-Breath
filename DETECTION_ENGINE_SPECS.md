# Tech-a-Breath: AI Acoustic Monitoring & Optimization

This document details the architecture of the real-time PTSD trigger detection system and the battery optimization strategies implemented in the app.

---

## 1. Dual-Stage Monitoring Architecture

To provide "Always-on" protection without draining the battery, the system operates in two distinct stages:

### Stage 1: The "Sentinel" (Low-Power Volume Check)
*   **Mechanism**: Continuously monitors raw PCM audio from the microphone in 100ms buffers.
*   **Logic**: Calculates the **Root Mean Square (RMS)** of the audio buffer and converts it to **Decibels (dB)**.
*   **Power Saving**: If the ambient noise is below **-50 dB**, the system performs no further processing. The heavy AI model remains dormant, saving approximately 85% of CPU cycles during quiet periods.

### Stage 2: The "Classifier" (Precision AI)
*   **Mechanism**: Triggered only when the Sentinel detects a volume spike above the threshold.
*   **AI Model**: Uses **YAMNet** (TensorFlow Lite), a deep neural network capable of recognizing 521 audio classes.
*   **Context window**: Analyzes a **0.975s** sliding window of audio to ensure high-fidelity classification.
*   **Latency**: The entire pipeline from detection to masking initiation is optimized for **sub-800ms** response time.

---

## 2. Trigger Precision & False Positive Reduction

The app uses a multi-layered verification system to ensure accuracy and minimize interruptions from non-trigger sounds (like music or car ringtones).

### A. Confidence Thresholding
The AI must reach a specific confidence score (probability) before a sound is even considered a candidate:
*   **Ambulance / Siren**: 35% (Higher threshold to filter out melodic ringtones)
*   **Dog Barking**: 25% (Lowered to catch impulsive, short barks)
*   **Baby Crying**: 30%

### B. Temporal Smoothing (Voting System)
The system maintains a sliding history of the **last 5 detections** to verify the stability of the sound:
*   **Continuous sounds (Sirens)**: Requires a **3-out-of-5** match. This ensures that brief siren-like electronic blips do not cause a trigger.
*   **Impulsive sounds (Dog Barks)**: Requires a **1-out-of-5** match. This prioritizes safety for sudden triggers that may not repeat immediately.
*   **Rhythmic sounds (Baby Crying)**: Requires a **2-out-of-5** match.

### C. Top-Sound Exclusion
If the AI identifies **"Music"**, **"Ringtone"**, or **"Telephone"** as the dominant sound in the environment, it will auto-reject any secondary trigger classifications. This is a critical safety layer for users in cars or near electronics.

---

## 3. Battery Optimization Techniques

1.  **XNNPACK Acceleration**: Utilizes the XNNPACK delegate in TFLite to leverage mobile-optimized CPU instructions (SIMD), reducing inference time and heat generation.
2.  **Async Initialization**: The AI model is pre-loaded on a background thread to prevent UI jank.
3.  **Gated Monitoring**: By skipping AI inference during quiet periods, we significantly extend device battery life for all-day use.
4.  **Foreground Service Type**: Uses `foregroundServiceType="microphone"` to maintain active listening priority while complying with Android's strict power management policies.

---

## 4. Current Configuration Summary
*   **Sample Rate**: 16,000 Hz (Mono)
*   **AI Inference Window**: 0.975 seconds
*   **History Buffer Size**: 5 samples
*   **Volume Gate**: -50.0 dB
*   **Inference Thresholds**: 35% (Siren) / 25% (Bark) / 30% (Crying)
