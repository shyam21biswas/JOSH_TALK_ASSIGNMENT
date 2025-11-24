# Android Intern Task — README

## Project Objective
Create a Kotlin Multiplatform (KMM) + Compose Multiplatform (CMP) prototype that allows the user to perform different sample recording tasks (text reading, image description, and photo capture) and view all completed tasks in a local **Task History** list.

---

## Demo Video

[Video Demo](https://drive.google.com/file/d/1ZASUFhb3B3FIqe-P4kRRujEhfvR2ZQV5/view?usp=drivesdk)


## Download App

[Download App](https://drive.google.com/file/d/1iILrZDY9xAiaRsF4yn5x-FuNBtyjcve0/view?usp=sharing)





## 📱 Screenshots!
[Screenshot](https://drive.google.com/uc?export=view&id=1bAJjNqfmGIrQkCEuyW7FTq__hMKxB7t6)

![Screenshot](https://drive.google.com/uc?export=view&id=FILE_ID)

![Screenshot](https://drive.google.com/uc?export=view&id=FILE_ID)



### App Flow
1. **Start Screen** - Welcome with gradient background
2. **Noise Test** - Real-time dB meter with animated circle
3. **Task Selection** - Three task cards with icons
4. **Text Reading** - Product description + mic button
5. **Audio Player** - Music-style UI with waveform
6. **Image Description** - Product image + recording
7. **Photo Capture** - Camera integration + description
8. **Task History** - List with stats and playback

---

### Build & Run
```bash
# Clone repository
https://github.com/shyam21biswas/JOSH_TALK_ASSIGNMENT.git

# Open in Android Studio
# Sync Gradle files
# Run on emulator or physical device
```


## Features / Flow Summary
1. **Start Screen**
   - Heading: “Let’s start with a Sample Task for practice.”
   - Sub-text: “Pehele hum ek sample task karte hain.”
   - Button: **Start Sample Task** → navigates to Noise Test Screen

2. **Noise Test Screen**
   - Decibel meter (0–60 dB)
   - Button: **Start Test** (simulate or read mic input)
   - If average dB < 40 → "Good to proceed" → navigate to Task Selection
   - If average dB ≥ 40 → show: "Please move to a quieter place"

3. **Task Selection Screen**
   - Options: Text Reading, Image Description, Photo Capture
   - Each option opens the respective task screen

4. **Text Reading Task**
   - Load sample text (use `https://dummyjson.com/products` description)
   - Instruction: “Read the passage aloud in your native language.”
   - Mic button (press & hold): start-record on press, stop-record on release
   - Validate duration: 10–20 seconds (reject otherwise, with inline error)
   - After recording: playback bar, three checkboxes:
     - No background noise
     - No mistakes while reading
     - Beech me koi galti nahi hai
   - Buttons: **Record again**, **Submit** (enabled only if checkboxes checked)
   - On submit: save JSON locally and navigate back to Task Selection

5. **Image Description Task**
   - Show image (API or local sample)
   - Instruction: “Describe what you see in your native language.”
   - Mic (press & hold) — same recording rules and validation (10–20 s)
   - After recording: playback, **Submit** → save JSON locally

6. **Photo Capture Task**
   - Request camera permission
   - **Capture Image** → camera → preview
   - Text field: describe the photo
   - Optional mic (press & hold) with same validation
   - Buttons: **Retake Photo**, **Submit**
   - On submit: save JSON locally

7. **Task History Screen**
   - Header: left — **Total Tasks**; right — **Total Recording Duration**
   - List items (RecyclerView-style): Task ID, Task Type, Duration, Timestamp, Preview (text snippet or thumbnail)

---

## Data Model (Local JSON example)
```json
{
  "task_type": "text_reading",
  "text": "Mega long lasting fragrance...",
  "audio_path": "/local/path/audio.mp3",
  "duration_sec": 15,
  "timestamp": "2025-11-12T10:00:00"
}
```
Other task types use similar structures (see `samples/` folder for examples).

---

## Tech Stack
- Kotlin Multiplatform Mobile (KMM) for shared business logic
- Compose Multiplatform (CMP) for UI (Android target + Desktop prototype if desired)
- Kotlinx.serialization for JSON (local persistence)
- SQLDelight or simple file-based storage for local Task History
- Audio recording: Android `MediaRecorder` / CMP-compatible abstraction
- Camera: Android `CameraX` (Android target)
- Networking (for sample images/text): Ktor or `ktor-client` with `https://dummyjson.com`

---

## Important Implementation Notes and Constraints
### Noise Test
- Implement a lightweight decibel meter calculation:
  - For prototype: simulate decibel values or sample microphone RMS to map to dB
  - Visualize 0–60 dB scale and show pass/fail message depending on threshold (40 dB)

### Recording UX
- Press-and-hold mic button semantics:
  - Start recording on `ACTION_DOWN` (or equivalent)
  - Stop recording on `ACTION_UP` / finger release
  - Enforce duration limits: if `duration < 10` show `Recording too short (min 10 s)`; if `>20` show `Recording too long (max 20 s)`
  - Automatically stop recording if user hits 20s
- Playback bar: local audio playback with seek

### Validation
- For Text Reading task: require the 3 checkboxes to be ticked before enabling Submit
- For Image / Photo tasks: Submit allowed after valid recording (or description text in photo capture)

### Local Storage
- Suggested schema: store tasks as an append-only JSON array in a file `tasks.json` or use SQLDelight table `tasks`
- Task fields: id (UUID), task_type, text/image_url/image_path, audio_path, duration_sec, timestamp
- Calculate `Total Recording Duration` by summing `duration_sec`

### Permissions
- Microphone permission (record audio)
- Camera permission (photo capture)
- File access or app internal storage only (avoid external storage write unless necessary)

---

## API usage
- Text sample and images: `https://dummyjson.com/products` or `https://dummyjson.com/products/{id}`
- Example image in spec: `https://cdn.dummyjson.com/product-images/14/2.jpg`

---

## Build & Run (Android)
1. Clone the repo.
2. Open in Android Studio (Arctic Fox or later recommended for KMM and CMP support).
3. Configure KMM targets (Android), add Compose Multiplatform plugin.
4. Add required permissions in `AndroidManifest.xml`:
   - `RECORD_AUDIO`, `CAMERA` (and request at runtime)
5. Run on an Android device (emulator may not provide real microphone/camera behavior)

---

## Testing / QA
- Test noise test with simulated dB streams and with real environment
- Verify press-and-hold recordings with edge durations (9.9s, 10s, 20s, 20.1s)
- Test camera capture, retake flow, and that image & audio get recorded and saved
- Verify Task History sums and correct preview rendering

---
## 🧪 Testing

### Manual Testing Checklist
- [ ] All permissions granted correctly
- [ ] Noise test passes/fails appropriately
- [ ] Recording duration validation works
- [ ] Audio playback functions correctly
- [ ] Image loading from API successful
- [ ] Camera capture and preview working
- [ ] Task history displays all tasks
- [ ] Data persists after app restart
- [ ] Back navigation works correctly
- [ ] Error messages display properly
---


---
# This app is build in native  Android  using Android Studio .

