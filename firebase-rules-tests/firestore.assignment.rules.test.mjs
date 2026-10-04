import fs from "node:fs";

import {
  assertFails,
  assertSucceeds,
  initializeTestEnvironment,
} from "@firebase/rules-unit-testing";

import {
  deleteDoc,
  doc,
  getDoc,
  setDoc,
} from "firebase/firestore";

const projectId = "telerehab-b6103";

const rules =
  fs.readFileSync(
    new URL(
      "../firestore.rules",
      import.meta.url,
    ),
    "utf8",
  );

const testEnv =
  await initializeTestEnvironment({
    projectId,
    firestore: {
      rules,
      host: "127.0.0.1",
      port: 8085,
    },
  });

const ownerUid = "patient-owner";
const otherUid = "different-patient";
const assignmentId = "assignment-1";

const assignment = {
  id: assignmentId,
  patientId: ownerUid,
  exerciseId: "knee-flexion-extension",
  title: "Knee Flexion and Extension",
  instructions: "Bend and straighten the knee.",
  targetRepetitions: 10,
  status: "ACTIVE",
};

try {
  await testEnv.withSecurityRulesDisabled(
    async (context) => {
      await setDoc(
        doc(
          context.firestore(),
          "patients",
          ownerUid,
          "exerciseAssignments",
          assignmentId,
        ),
        assignment,
      );
    },
  );

  const ownerDb =
    testEnv
      .authenticatedContext(ownerUid)
      .firestore();

  const otherDb =
    testEnv
      .authenticatedContext(otherUid)
      .firestore();

  const anonymousDb =
    testEnv
      .unauthenticatedContext()
      .firestore();

  const ownerPath =
    doc(
      ownerDb,
      "patients",
      ownerUid,
      "exerciseAssignments",
      assignmentId,
    );

  const otherPath =
    doc(
      otherDb,
      "patients",
      ownerUid,
      "exerciseAssignments",
      assignmentId,
    );

  const anonymousPath =
    doc(
      anonymousDb,
      "patients",
      ownerUid,
      "exerciseAssignments",
      assignmentId,
    );

  console.log("1. Patient may read own assignment");

  await assertSucceeds(
    getDoc(ownerPath),
  );

  console.log("2. Other patient may not read assignment");

  await assertFails(
    getDoc(otherPath),
  );

  console.log("3. Unauthenticated client may not read assignment");

  await assertFails(
    getDoc(anonymousPath),
  );

  console.log("4. Patient may not create/update assignment");

  await assertFails(
    setDoc(
      ownerPath,
      {
        ...assignment,
        targetRepetitions: 20,
      },
    ),
  );

  console.log("5. Patient may not delete assignment");

  await assertFails(
    deleteDoc(ownerPath),
  );

  console.log("");
  console.log(
    "TELEREHAB ASSIGNMENT RULES TESTS GREEN",
  );
} finally {
  await testEnv.cleanup();
}
