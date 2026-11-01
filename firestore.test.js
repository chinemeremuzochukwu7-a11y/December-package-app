const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_123";
const BOB_UID = "bob_456";

const [emulatorHost, emulatorPortStr] = (process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085").split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

// --- SECURITY BOUNDS TESTS ---

test("Unauthenticated user: cannot read another user's profile", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).get());
});

test("Unauthenticated user: cannot write to user profile", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).set({
    userId: ALICE_UID,
    credits: 25,
    isPro: false
  }));
});

test("Authenticated user: can create and read their own profile", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertSucceeds(aliceDb.collection("users").doc(ALICE_UID).set({
    userId: ALICE_UID,
    credits: 25,
    isPro: false,
    displayName: "Alice",
    email: "alice@example.com"
  }));
  await assertSucceeds(aliceDb.collection("users").doc(ALICE_UID).get());
});

test("Authenticated user: cannot read another user's profile (cross-user isolation)", async () => {
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context.firestore().collection("users").doc(BOB_UID).set({
      userId: BOB_UID,
      credits: 25,
      isPro: false
    });
  });

  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertFails(aliceDb.collection("users").doc(BOB_UID).get());
});

test("Authenticated user: can create and observe saved cards in their subcollection", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("saved_cards").doc("card_1").set({
      id: "card_1",
      userId: ALICE_UID,
      templateId: "tpl_xmas_1",
      title: "Merry Christmas",
      recipientName: "Mom",
      message: "Wishing you peace and warmth!",
      senderName: "Alice"
    })
  );
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("saved_cards").doc("card_1").get()
  );
});

test("Authenticated user: cannot access another user's saved cards", async () => {
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context.firestore().collection("users").doc(BOB_UID).collection("saved_cards").doc("bob_card").set({
      id: "bob_card",
      userId: BOB_UID,
      templateId: "tpl_1",
      title: "Bob Card",
      message: "Secret note"
    });
  });

  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertFails(
    aliceDb.collection("users").doc(BOB_UID).collection("saved_cards").doc("bob_card").get()
  );
});

test("Claimed devices: can read and write hardware fingerprint ledger", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertSucceeds(
    unauthDb.collection("claimed_devices").doc("dev_123456789").set({
      deviceId: "dev_123456789",
      currentCredits: 25,
      initialCreditsGranted: 25,
      isPro: false
    })
  );
  await assertSucceeds(
    unauthDb.collection("claimed_devices").doc("dev_123456789").get()
  );
});
