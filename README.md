# DermaVision: Intelligent Skin Disease Screening & Health Platform

**DermaVision** is an enterprise-grade, privacy-first Android application designed for intelligent on-device dermatological skin lesion screening, real-time image quality evaluation, and automated clinical report generation. Built using modern Android architecture, computer vision algorithms, on-device machine learning with **TensorFlow Lite**, and multimodal cloud intelligence with **Google Gemini 3.5 Flash**, DermaVision empowers users with fast, reliable, and accessible skin health insights.

---

## 📌 Project Overview & Vision

Skin conditions and malignant skin lesions are among the most common human illnesses worldwide. Early detection of conditions like **Melanoma** and **Basal Cell Carcinoma (BCC)** significantly improves clinical outcomes. However, access to dermatological specialists is often limited by geographical, financial, or scheduling barriers.

**DermaVision** bridges this gap by offering:
- **Instant On-Device AI Classification**: Immediate 10-class skin condition screening using compressed edge ML models that operate 100% offline.
- **Pre-Inference Quality Validation**: Automated verification of image resolution, focus, and illumination before model inference to minimize false diagnostics.
- **Multimodal Clinical Insights**: Detailed qualitative severity categorization, ABCDE risk breakdown, home-care recommendations, and automated PDF medical summary generation.
- **Secure Cloud Sync & History**: Local encrypted storage backed by Firebase Cloud Firestore for multi-device sync and longitudinal tracking over time.

---

## ✨ Key Features

### 1. Real-Time Camera Acquisition & Quality Check
- **CameraX Integration**: Customized live viewfinder with tap-to-focus, linear zoom controls, torch toggle, and dynamic alignment crosshairs.
- **Computer Vision Pre-Check**: Evaluates images for motion blur, sub-optimal brightness/lighting, and spatial resolution before running AI inference.
- **Automated Image Pre-Processing**: Applies contrast normalization, noise reduction, and center-cropped lesion isolation for superior feature extraction.

### 2. Dual-Engine Dermatological Screening
- **On-Device Vision Engine (TensorFlow Lite)**:
  - Uses `dermavision_v2.tflite` for rapid, zero-latency local classification without transmitting sensitive medical images over the network.
  - Extracts key visual descriptors (erythema index, melanin density, border regularity, texture variance).
- **Multimodal Cloud AI Engine (Google Gemini 3.5 Flash)**:
  - Executes comprehensive differential analysis when network connectivity is available.
  - Generates human-readable clinical narratives, risk profiles, and contextual medical guidance.

### 3. Qualitative Severity & ABCDE Dermatological Criteria
- **3-Tier Severity Spectrum**: Categorized into **Mild**, **Moderate**, and **Severe** clinical stages without misleading numerical percentages.
- **ABCDE Lesion Profiling**:
  - **Asymmetry**: Visual balance evaluation across major axes.
  - **Border**: Edge sharpness and irregularity detection.
  - **Color**: Pigment uniformity and variegation assessment.
  - **Diameter**: Size evaluation against dermatological standards.
  - **Evolution**: Historical tracking across past saved screenings.

### 4. Automated PDF Clinical Summary Reports
- **Native Android PDF Engine**: Built using Android `PdfDocument` Canvas API.
- **Formatted Medical Summaries**: Generates exportable PDF documents containing patient metadata, high-resolution lesion thumbnails, top differential diagnoses, severity markers, and medical disclaimers for consultation with healthcare providers.

### 5. Scan History, Analytics & Tracking
- **Local Room Database**: Encrypted local SQLite persistence for instant offline search, category filtering, and scan deletion.
- **Visual Analytics Dashboard**: Interactive statistics showing disease category distributions, screening frequency over time, and risk-level breakdowns.

### 6. Authentication, Privacy & Security
- **Firebase Auth & Credentials API**: Supports Email/Password authentication, One-Tap Google Sign-In via Credential Manager API, and an instant offline Guest Mode.
- **Privacy-First Design**: Users can run complete screenings offline in Guest Mode without creating an account or uploading images to cloud servers.

---

## 📋 ISIC-Aligned Disease Taxonomy

DermaVision classifies skin lesions across **10 standard ISIC (International Skin Imaging Collaboration)** categories:

| # | Disease / Condition Category | Clinical Description | Priority Level |
|---|------------------------------|----------------------|----------------|
| **1** | **Melanoma** | Malignant melanocytic tumor requiring immediate medical evaluation | High Priority |
| **2** | **Basal Cell Carcinoma (BCC)** | Common non-melanoma skin cancer originating in basal cells | High Priority |
| **3** | **Atopic Dermatitis** | Chronic inflammatory skin disease causing itchy, erythematous lesions | Moderate |
| **4** | **Eczema** | Common pruritic skin inflammation with redness and peeling | Moderate |
| **5** | **Melanocytic Nevi (NV)** | Benign moles composed of melanocytes | Normal / Low |
| **6** | **Benign Keratosis-like Lesions (BKL)** | Seborrheic keratoses, solar lentigines, and lichen planus-like keratoses | Normal / Low |
| **7** | **Psoriasis & Lichen Planus** | Autoimmune inflammatory conditions marked by scaly plaques | Moderate |
| **8** | **Seborrheic Keratoses** | Non-cancerous skin growth common in older adults | Normal / Low |
| **9** | **Warts, Molluscum & Viral Infections** | Viral epidermal growths caused by HPV or Molluscum contagiosum | Moderate |
| **10** | **Tinea, Ringworm & Fungal Infections** | Superficial dermatophyte fungal infections of the skin | Moderate |

---

## 🏗️ System Architecture

DermaVision follows **Clean Architecture** principles and the standard **MVVM (Model-View-ViewModel)** architectural pattern.

```
                  +----------------------------------------------+
                  |              UI Layer (Compose)              |
                  |  Screens * Components * Theme * Navigation   |
                  +----------------------+-----------------------+
                                         |
                                   StateFlow / UIState
                                         |
                  +----------------------+-----------------------+
                  |               ViewModel Layer                |
                  |            DermaVisionViewModel              |
                  +----------------------+-----------------------+
                                         |
                              Coroutines / Suspend Calls
                                         |
     +-----------------------------------+-----------------------------------+
     |                                   |                                   |
+----+--------------------------+  +-----+--------------------------+  +-----+--------------------------+
|     TFLite & Vision Engine    |  |   Room Database Repository     |  |    Firebase & Gemini Cloud    |
|  - SkinDiseaseClassifier.kt   |  |  - ScanHistoryDao.kt           |  |  - GeminiVisionService.kt     |
|  - ImageQualityChecker.kt     |  |  - AppDatabase.kt              |  |  - AuthRepository.kt          |
|  - PreProcessor.kt            |  |  - ScanHistoryEntity.kt        |  |  - FirestoreSyncService.kt    |
+-------------------------------+  +--------------------------------+  +--------------------------------+
```

### 📂 Directory & Package Structure

```
app/src/main/java/com/example/
├── data/
│   ├── auth/                 # Firebase Auth, Google Credential Manager & User Session
│   ├── firestore/            # Cloud Firestore sync for scan statistics & backup
│   ├── gemini/               # Google Gemini 3.5 Flash Multimodal Vision API service
│   ├── local/                # Room DB Database, DAO, Entities & History Repository
│   ├── model/                # Core Data Models (SkinDiseaseClass, PredictionResult, QualityReport)
│   └── tflite/               # TensorFlow Lite interpreter & on-device classifier
├── ui/
│   ├── components/           # Reusable Compose UI elements (BottomBar, Dialogs, Cards)
│   ├── navigation/           # Compose Navigation routes & graph definition
│   ├── screens/              # App Screens (Splash, Auth, Dashboard, Camera, Result, History, Analytics, Settings, Profile)
│   ├── theme/                # Material 3 Colors, Shapes, and Typography
│   ├── viewmodel/            # Central ViewModel managing state streams via StateFlow
│   └── DermaVisionApp.kt     # Main App entry composable & Navigation Host
└── util/
    ├── AppStrings.kt         # Localized string resources & medical descriptions
    ├── DermatologicalPreProcessor.kt # Bitmap cropping, contrast enhancement & feature extraction
    ├── ImageQualityChecker.kt # Blur, resolution & brightness validator
    ├── NetworkConnectivityMonitor.kt # Real-time online/offline network monitor
    ├── NotificationHelper.kt # System notifications for scan completion & reminders
    ├── PdfReportGenerator.kt # Native Android PDF report builder
    └── ThemePreferences.kt  # User preferences via DataStore
```

---

## 🛠️ Technology Stack & Libraries

| Domain | Technology / Library | Version | Description |
|--------|----------------------|---------|-------------|
| **Language** | Kotlin | 2.2.10 | Modern, safe, and concise language for Android |
| **UI Framework** | Jetpack Compose | 2024.09.00 (BOM) | Declarative UI toolkit with Material 3 design |
| **Architecture** | MVVM + Clean Architecture | — | Separation of concerns with unidirectional data flow |
| **Machine Learning** | TensorFlow Lite | 2.16.1 | On-device ML engine for offline lesion classification |
| **Generative AI** | Google Gemini API | 3.5 Flash | Cloud multimodal vision for detailed clinical insights |
| **Database** | Room Database | 2.7.0 | SQLite ORM compiler powered by Google KSP |
| **Authentication** | Firebase Auth | 34.17.0 | Email/Password & Google Credential Manager integration |
| **Cloud Storage** | Cloud Firestore | 34.17.0 | NoSQL document database for cross-device sync |
| **Camera** | CameraX | 1.5.0 | Lifecycle-aware camera library with Preview & Capture |
| **Image Loading** | Coil | 2.7.0 | Kotlin Coroutines-backed image loader for Compose |
| **Preferences** | Jetpack DataStore | 1.1.7 | Key-value data storage replacement for SharedPreferences |
| **Networking** | Retrofit & OkHttp | 2.12.0 / 4.10.0 | Type-safe REST client & HTTP logging interceptor |
| **Serialization** | Moshi | 1.15.2 | Modern JSON library with KSP code generation |
| **PDF Generation** | Android PdfDocument | Native | Vector Canvas rendering for medical summary reports |

---

## 🚀 Installation & Setup Guide

### Prerequisites
- **Android Studio**: Android Studio Ladybug (2024.2.1) or newer recommended.
- **JDK**: Java Development Kit 17 or higher.
- **Android SDK**: Target SDK 36, Compile SDK 36, Minimum SDK 24 (Android 7.0+).
- **Physical Device or Emulator**: Android device with Camera support.

### Step-by-Step Instructions

1. **Clone the Repository**:
   ```bash
   git clone https://github.com/Sourabh-Vishwakarma/DermaVision.git
   cd DermaVision
   ```

2. **Configure API Keys & Environment Secrets**:
   Create a `.env` file in the project root directory:
   ```env
   GEMINI_API_KEY=your_google_gemini_api_key_here
   ```
   *(Note: DermaVision runs seamlessly in offline mode using the local TensorFlow Lite engine if no Gemini API key is provided.)*

3. **Configure Firebase (Optional)**:
   - Place your `google-services.json` file inside the `app/` directory if connecting to a custom Firebase project.

4. **Sync & Build**:
   - Open the project in **Android Studio**.
   - Sync project with Gradle files (`File -> Sync Project with Gradle Files`).
   - Run on an attached Android device or emulator:
     ```bash
     ./gradlew assembleDebug
     ```

---

## 🧪 Testing & Quality Assurance

DermaVision includes unit testing, Android UI testing, and snapshot regression infrastructure:

- **Unit Tests & Robolectric**:
  ```bash
  ./gradlew :app:testDebugUnitTest
  ```
- **Android Instrumentation Tests**:
  ```bash
  ./gradlew :app:connectedDebugAndroidTest
  ```

---

## ⚠️ Medical & Regulatory Disclaimer

> **IMPORTANT:** DermaVision is strictly an educational, research, and preliminary screening tool. It **does not** provide formal medical diagnoses, clinical prescriptions, or binding treatment plans. The results generated by DermaVision are intended to assist users in understanding skin health metrics and determining when to seek professional medical advice. Always consult a board-certified dermatologist or qualified healthcare provider for suspicious skin lesions or changing moles.

---

## 👥 Authors & Project Team

### Project Lead & System Architect

| Author | Role | Contact | GitHub |
|--------|------|---------|--------|
| **Sourabh Vishwakarma** | Lead Android & AI Developer | support@dermavision.ai | [@Sourabh-Vishwakarma](https://github.com/Sourabh-Vishwakarma) |

### Key Contributions:
- Core Android application architecture using Jetpack Compose, Coroutines & Flow.
- On-device TensorFlow Lite model integration and dermatological image pre-processing algorithms.
- Multimodal Google Gemini API service integration & prompt engineering.
- Native Android PDF report generation and Room DB encrypted persistence pipeline.
- Firebase Authentication, Google Identity API, and Firestore cloud synchronization.

---

## 📄 License

This project is licensed under the **MIT License** — see the `LICENSE` file for details.
