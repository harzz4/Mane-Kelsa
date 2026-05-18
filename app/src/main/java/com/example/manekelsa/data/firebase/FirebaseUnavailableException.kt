package com.example.manekelsa.data.firebase

class FirebaseUnavailableException : IllegalStateException(
    "Firebase is not available. Add google-services.json and check Firebase setup.",
)
