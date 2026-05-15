# Mane Kelsa

Mane Kelsa is a beginner-friendly Android internship project for finding nearby home-service workers. It uses Kotlin, Jetpack Compose, Material 3, Clean Architecture, Firebase Firestore realtime updates, DataStore, Hilt, and bilingual string resources.

## Languages

- English: `app/src/main/res/values/strings.xml`
- Kannada: `app/src/main/res/values-kn/strings.xml`

The app starts in Kannada by default. Users can switch between Kannada and English from the first role-selection screen or from Settings.

## Firebase setup

1. Create a Firebase project in the Firebase Console.
2. Add an Android app with package name:

   ```text
   com.example.manekelsa
   ```

3. Download `google-services.json`.
4. Put the file here:

   ```text
   app/google-services.json
   ```

5. Enable Cloud Firestore.
6. For development, you can start with test rules, then lock them down before release.

## Firestore collection structure

Collection path:

```text
workers/{workerId}
```

Worker document fields:

```text
id: string
name: string
phoneNumber: string
photoUrl: string or null
serviceType: string
area: string
street: string or null
dailyRate: number
twoHourRate: number
experienceYears: number
description: string
isAvailableToday: boolean
thumbsUpCount: number
ratedBy: array of device/user ids
updatedAt: number
```

## How to run

1. Open the project in Android Studio.
2. Sync Gradle.
3. Add `app/google-services.json`.
4. Run the `app` configuration on an emulator or Android device.

The project applies the Google Services plugin only when `google-services.json` exists, so the code can still sync before Firebase is added. Without Firebase setup, the UI shows an error state instead of crashing.

## Test Worker flow

1. Open the app and choose Worker.
2. Tap Edit profile.
3. Add name, valid mobile number, area, service, rates, and an optional profile photo from Gallery or Camera.
4. Tap Generate description if you want an auto-filled description.
5. Save.
6. Return to Worker Home and toggle Today availability.

## Test Resident flow

1. Open the app on another device/emulator, or reset role in Settings.
2. Choose Resident.
3. Type an area or street.
4. Use service chips to filter.
5. Tap a worker card.
6. Tap Call to open the phone dialer.
7. Tap Thumbs up to rate the worker once from that device.

## Verify realtime availability

1. Use two devices or emulators.
2. Device A: choose Worker and save a profile.
3. Device B: choose Resident and open the worker list.
4. Device A: toggle availability on Worker Home.
5. Device B should update the worker card immediately because the app listens to Firestore snapshots with Flow.

## Architecture

```text
data/
  FirebaseWorkerDataSource
  LocalPreferencesDataSource
  WorkerRepositoryImpl
domain/
  model/
  repository/
  usecase/
presentation/
  navigation/
  screens/
  viewmodel/
  common/
di/
  AppModule
  FirebaseModule
  RepositoryModule
  UseCaseModule
```

## Notes for internship review

- All visible static UI text comes from string resources.
- Kannada is the primary app language, with an in-app English/Kannada switch.
- Phone numbers are validated before saving and before enabling Call.
- Resident worker list is realtime through Firestore snapshot listeners and sorted by closest street/area match.
- Availability updates use `workers/{workerId}`, update local cache immediately, and sync through Firestore when online.
- Thumbs-up updates local cache immediately and syncs to Firestore, while preventing repeat likes from the same local device.
