import fs from "node:fs";
import {
  assertFails,
  assertSucceeds,
  initializeTestEnvironment,
} from "@firebase/rules-unit-testing";
import {
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

  console.log("");
  console.log("TELEREHAB FIRESTORE RULES TESTS GREEN");
} finally {
  await testEnv.cleanup();
}
