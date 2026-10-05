package com.signaldesk.telerehab.data.therapist

import com.google.firebase.firestore.FirebaseFirestore
import com.signaldesk.telerehab.domain.therapist.TherapistPatient
import com.signaldesk.telerehab.domain.therapist.TherapistPatientRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.tasks.await

@Singleton
class FirestoreTherapistPatientRepository @Inject constructor(
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
            )
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
}
