import fs from "node:fs";
import {
  assertFails,
  assertSucceeds,
  initializeTestEnvironment,
} from "@firebase/rules-unit-testing";
import {
  writeBatch,
  doc,
  getDoc,
  setDoc,
  deleteDoc,
} from "firebase/firestore";

const projectId = "telerehab-b6103";
const rules = fs.readFileSync(new URL("../firestore.rules", import.meta.url), "utf8");

const testEnv = await initializeTestEnvironment({
  projectId,
  firestore: {
    rules,
    host: "127.0.0.1",
    port: 8085,
  },
});

const ownerUid = "patient-owner";
const otherUid = "patient-other";
const sessionId = "session-123";
const therapistUid = "therapist-assigned";
const unassignedTherapistUid = "therapist-unassigned";

const validDocument = {
  id: sessionId,
  assignmentId: "assignment-456",
  patientId: ownerUid,
  startedAtEpochMillis: 1791108000000,
  completedAtEpochMillis: 1791108900000,
  sessionStatus: "COMPLETED",
};

try {
  await testEnv.clearFirestore();

  const ownerDb =
    testEnv.authenticatedContext(ownerUid).firestore();

  const otherDb =
    testEnv.authenticatedContext(otherUid).firestore();

  const anonymousDb =
    testEnv.unauthenticatedContext().firestore();

  const ownerPath =
    doc(
      ownerDb,
      "patients",
      ownerUid,
      "exerciseSessions",
      sessionId,
    );

  const otherUserSamePath =
    doc(
      otherDb,
      "patients",
      ownerUid,
      "exerciseSessions",
      sessionId,
    );

  const anonymousSamePath =
    doc(
      anonymousDb,
      "patients",
      ownerUid,
      "exerciseSessions",
      sessionId,
    );

  console.log("1. Owner create should succeed");
  await assertSucceeds(
    setDoc(ownerPath, validDocument),
  );

  console.log("2. Owner read should succeed");
  await assertSucceeds(
    getDoc(ownerPath),
  );

  console.log("3. Different authenticated user read should fail");
  await assertFails(
    getDoc(otherUserSamePath),
  );

  console.log("4. Different authenticated user write should fail");
  await assertFails(
    setDoc(otherUserSamePath, validDocument),
  );

  console.log("5. Unauthenticated read should fail");
  await assertFails(
    getDoc(anonymousSamePath),
  );

  console.log("6. Unauthenticated write should fail");
  await assertFails(
    setDoc(anonymousSamePath, validDocument),
  );

  console.log("7. patientId mismatch should fail");
  await assertFails(
    setDoc(
      ownerPath,
      {
        ...validDocument,
        patientId: otherUid,
      },
    ),
  );

  console.log("8. document ID mismatch should fail");
  await assertFails(
    setDoc(
      ownerPath,
      {
        ...validDocument,
        id: "different-session-id",
      },
    ),
  );

  console.log("9. Unexpected field should fail");
  await assertFails(
    setDoc(
      ownerPath,
      {
        ...validDocument,
        syncStatus: "PENDING",
      },
    ),
  );

  console.log("10. Delete should fail");
  await assertFails(
    deleteDoc(ownerPath),
  );

  await testEnv.withSecurityRulesDisabled(
    async (context) => {
      const adminDb = context.firestore();

      await setDoc(
        doc(adminDb, "users", therapistUid),
        {
          role: "THERAPIST",
        },
      );

      await setDoc(
        doc(adminDb, "users", unassignedTherapistUid),
        {
          role: "THERAPIST",
        },
      );
    },
  );

  const therapistDb =
    testEnv.authenticatedContext(therapistUid).firestore();

  const secondTherapistDb =
    testEnv.authenticatedContext(
      unassignedTherapistUid,
    ).firestore();

  const therapistPatientPath =
    doc(
      therapistDb,
      "therapists",
      therapistUid,
      "patients",
      ownerUid,
    );

  const nonTherapistPatientPath =
    doc(
      otherDb,
      "therapists",
      otherUid,
      "patients",
      ownerUid,
    );

  const crossTherapistPatientPath =
    doc(
      secondTherapistDb,
      "therapists",
      therapistUid,
      "patients",
      otherUid,
    );

  const relationshipDocument = {
    active: true,
    displayName: "Test Patient",
  };

  console.log("11. Therapist can link patient under own account");
  await assertSucceeds(
    setDoc(
      therapistPatientPath,
      relationshipDocument,
    ),
  );

  console.log("12. Non-therapist cannot link patient");
  await assertFails(
    setDoc(
      nonTherapistPatientPath,
      relationshipDocument,
    ),
  );

  console.log("13. Therapist cannot write another therapist relationship");
  await assertFails(
    setDoc(
      crossTherapistPatientPath,
      relationshipDocument,
    ),
  );

  const linkedPatientProfilePath =
    doc(
      therapistDb,
      "users",
      ownerUid,
    );

  const unlinkedPatientProfilePath =
    doc(
      therapistDb,
      "users",
      otherUid,
    );

  const nonTherapistProfilePath =
    doc(
      otherDb,
      "users",
      ownerUid,
    );

  console.log("14. Therapist can create linked PATIENT profile");
  await assertSucceeds(
    setDoc(
      linkedPatientProfilePath,
      {
        role: "PATIENT",
      },
    ),
  );

  console.log("15. Therapist cannot create profile for unlinked patient");
  await assertFails(
    setDoc(
      unlinkedPatientProfilePath,
      {
        role: "PATIENT",
      },
    ),
  );

  console.log("16. Non-therapist cannot create patient profile");
  await assertFails(
    setDoc(
      nonTherapistProfilePath,
      {
        role: "PATIENT",
      },
    ),
  );

  console.log("17. Therapist cannot create THERAPIST profile");
  await assertFails(
    setDoc(
      doc(
        therapistDb,
        "users",
        ownerUid,
      ),
      {
        role: "THERAPIST",
      },
    ),
  );

  const batchPatientUid = "patient-batch";

  const provisioningBatch =
    writeBatch(therapistDb);

  provisioningBatch.set(
    doc(
      therapistDb,
      "therapists",
      therapistUid,
      "patients",
      batchPatientUid,
    ),
    {
      active: true,
      displayName: "Batch Patient",
    },
  );

  provisioningBatch.set(
    doc(
      therapistDb,
      "users",
      batchPatientUid,
    ),
    {
      role: "PATIENT",
    },
  );

  console.log("18. Therapist can provision linked patient atomically");
  await assertSucceeds(
    provisioningBatch.commit(),
  );

  console.log("");
  console.log("TELEREHAB FIRESTORE RULES TESTS GREEN");
} finally {
  await testEnv.cleanup();
}
