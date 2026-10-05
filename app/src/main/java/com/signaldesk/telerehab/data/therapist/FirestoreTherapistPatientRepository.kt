package com.signaldesk.telerehab.data.therapist

import android.content.Context
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.signaldesk.telerehab.domain.therapist.PatientCredentials
import com.signaldesk.telerehab.domain.therapist.TherapistPatient
import com.signaldesk.telerehab.domain.therapist.TherapistPatientRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import java.security.SecureRandom
import java.util.Collections
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.tasks.await

@Singleton
class FirestoreTherapistPatientRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val firestore: FirebaseFirestore,
) : TherapistPatientRepository {

    override suspend fun findPatientsForTherapist(
        therapistId: String,
    ): List<TherapistPatient> {
        val snapshot =
            firestore
                .collection("therapists")
                .document(therapistId)
                .collection("patients")
                .get()
                .await()

        return snapshot.documents.map { document ->
            TherapistPatient(
                therapistId = therapistId,
                patientId = document.id,
                displayName =
                    document
                        .getString("displayName")
                        ?.trim()
                        ?.takeIf { it.isNotBlank() },
            )
        }
    }

    override suspend fun addPatient(
        therapistId: String,
        email: String,
        displayName: String?,
    ): PatientCredentials {
        require(therapistId.isNotBlank())
        require(email.isNotBlank())

        val normalizedEmail =
            email.trim()

        val temporaryPassword =
            generateTemporaryPassword()

        val patientAuth =
            patientProvisioningAuth()

        var createdUserId: String? = null

        try {
            val authResult =
                patientAuth
                    .createUserWithEmailAndPassword(
                        normalizedEmail,
                        temporaryPassword,
                    )
                    .await()

            val patientUser =
                requireNotNull(authResult.user) {
                    "Patient account creation completed without a Firebase user."
                }

            createdUserId =
                patientUser.uid

            val relationship =
                firestore
                    .collection("therapists")
                    .document(therapistId)
                    .collection("patients")
                    .document(patientUser.uid)

            val profile =
                firestore
                    .collection("users")
                    .document(patientUser.uid)

            firestore
                .batch()
                .set(
                    relationship,
                    mapOf(
                        "active" to true,
                        "displayName" to
                            displayName
                                ?.trim()
                                ?.takeIf { it.isNotBlank() },
                    ),
                )
                .set(
                    profile,
                    mapOf(
                        "role" to "PATIENT",
                    ),
                )
                .commit()
                .await()

            return PatientCredentials(
                patientId = patientUser.uid,
                email = normalizedEmail,
                temporaryPassword = temporaryPassword,
            )
        } catch (error: Throwable) {
            if (createdUserId != null) {
                runCatching {
                    patientAuth.currentUser
                        ?.takeIf { it.uid == createdUserId }
                        ?.delete()
                        ?.await()
                }
            }

            throw error
        } finally {
            patientAuth.signOut()
        }
    }

    override suspend fun isAssigned(
        therapistId: String,
        patientId: String,
    ): Boolean =
        firestore
            .collection("therapists")
            .document(therapistId)
            .collection("patients")
            .document(patientId)
            .get()
            .await()
            .exists()

    private fun patientProvisioningAuth(): FirebaseAuth {
        val app =
            runCatching {
                FirebaseApp.getInstance(
                    PATIENT_PROVISIONING_APP,
                )
            }.getOrElse {
                FirebaseApp.initializeApp(
                    context,
                    FirebaseApp.getInstance().options,
                    PATIENT_PROVISIONING_APP,
                )
                    ?: error(
                        "Unable to initialize patient provisioning Firebase app.",
                    )
            }

        return FirebaseAuth.getInstance(app)
    }

    private fun generateTemporaryPassword(): String {
        val characters =
            mutableListOf<Char>()

        characters += randomCharacter(UPPERCASE)
        characters += randomCharacter(LOWERCASE)
        characters += randomCharacter(DIGITS)
        characters += randomCharacter(SYMBOLS)

        repeat(8) {
            characters +=
                randomCharacter(
                    UPPERCASE +
                        LOWERCASE +
                        DIGITS +
                        SYMBOLS,
                )
        }

        Collections.shuffle(
            characters,
            secureRandom,
        )

        return characters.joinToString("")
    }

    private fun randomCharacter(
        source: String,
    ): Char =
        source[
            secureRandom.nextInt(source.length)
        ]

    private companion object {
        const val PATIENT_PROVISIONING_APP =
            "patient-provisioning"

        const val UPPERCASE =
            "ABCDEFGHJKLMNPQRSTUVWXYZ"

        const val LOWERCASE =
            "abcdefghijkmnopqrstuvwxyz"

        const val DIGITS =
            "23456789"

        const val SYMBOLS =
            "!@#$%"

        val secureRandom =
            SecureRandom()
    }
}
