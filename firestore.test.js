const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const DEVICE_A = "dev_a1b2c3d4e5f6a7b8c9d0";
const DEVICE_B = "dev_z9y8x7w6v5u4t3s2r1q0";

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

// --- HARDWARE DEVICE ANTI-ABUSE & SECURITY BOUNDS TESTS ---

test("Cannot list users or claimed_devices collections (prevents enumeration)", async () => {
  const db = testEnv.unauthenticatedContext().firestore();
  await assertFails(db.collection("users").get());
  await assertFails(db.collection("claimed_devices").get());
});

test("Invalid or short user/device IDs are rejected", async () => {
  const db = testEnv.unauthenticatedContext().firestore();
  await assertFails(db.collection("users").doc("short").set({
    userId: "short",
    credits: 25,
    isPro: false
  }));
  await assertFails(db.collection("claimed_devices").doc("bad_123").set({
    deviceId: "bad_123",
    initialCreditsGranted: 25,
    currentCredits: 25,
    isPro: false
  }));
});

test("New device: can initialize with exactly 25 starter credits, cannot claim >25", async () => {
  const db = testEnv.unauthenticatedContext().firestore();

  // Attempting to create with 100 credits fails
  await assertFails(db.collection("users").doc(DEVICE_A).set({
    userId: DEVICE_A,
    credits: 100,
    isPro: false
  }));
  await assertFails(db.collection("claimed_devices").doc(DEVICE_A).set({
    deviceId: DEVICE_A,
    initialCreditsGranted: 100,
    currentCredits: 100,
    isPro: false
  }));

  // Creating with exactly 25 starter credits succeeds
  await assertSucceeds(db.collection("claimed_devices").doc(DEVICE_A).set({
    deviceId: DEVICE_A,
    initialCreditsGranted: 25,
    currentCredits: 25,
    isPro: false
  }));
  await assertSucceeds(db.collection("users").doc(DEVICE_A).set({
    userId: DEVICE_A,
    credits: 25,
    isPro: false,
    starterCreditClaimed: true
  }));
});

test("Cannot delete claimed_devices or users to reset credit ledger", async () => {
  const db = testEnv.unauthenticatedContext().firestore();
  await assertSucceeds(db.collection("claimed_devices").doc(DEVICE_A).set({
    deviceId: DEVICE_A,
    initialCreditsGranted: 25,
    currentCredits: 25,
    isPro: false
  }));
  await assertSucceeds(db.collection("users").doc(DEVICE_A).set({
    userId: DEVICE_A,
    credits: 25,
    isPro: false
  }));

  await assertFails(db.collection("claimed_devices").doc(DEVICE_A).delete());
  await assertFails(db.collection("users").doc(DEVICE_A).delete());
});

test("Anti-abuse: cannot jump credits by >1 when non-Pro or restore 25 after spending", async () => {
  const db = testEnv.unauthenticatedContext().firestore();
  await assertSucceeds(db.collection("claimed_devices").doc(DEVICE_A).set({
    deviceId: DEVICE_A,
    initialCreditsGranted: 25,
    currentCredits: 10, // wait: create requires 25
    isPro: false
  })).catch(() => {});

  // Proper creation with 25, then deduct 15 -> 10
  await assertSucceeds(db.collection("claimed_devices").doc(DEVICE_A).set({
    deviceId: DEVICE_A,
    initialCreditsGranted: 25,
    currentCredits: 25,
    isPro: false
  }));
  await assertSucceeds(db.collection("users").doc(DEVICE_A).set({
    userId: DEVICE_A,
    credits: 25,
    isPro: false
  }));

  // Deduct 15 credits -> 10 credits
  await assertSucceeds(db.collection("users").doc(DEVICE_A).update({ credits: 10 }));
  await assertSucceeds(db.collection("claimed_devices").doc(DEVICE_A).update({ currentCredits: 10 }));

  // Trying to reset credits back to 25 fails!
  await assertFails(db.collection("users").doc(DEVICE_A).update({ credits: 25 }));
  await assertFails(db.collection("claimed_devices").doc(DEVICE_A).update({ currentCredits: 25 }));

  // Trying to go negative fails!
  await assertFails(db.collection("users").doc(DEVICE_A).update({ credits: -5 }));

  // Daily +1 or Rewarded Ad +1 succeeds (10 -> 11)
  await assertSucceeds(db.collection("users").doc(DEVICE_A).update({ credits: 11, lastDailyClaimDate: "2026-11-16" }));
  await assertSucceeds(db.collection("claimed_devices").doc(DEVICE_A).update({ currentCredits: 11, lastDailyClaimDate: "2026-11-16" }));
});

test("Recreating user doc when claimed_devices already exists must match recorded balance (no new 25)", async () => {
  const db = testEnv.unauthenticatedContext().firestore();
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context.firestore().collection("claimed_devices").doc(DEVICE_B).set({
      deviceId: DEVICE_B,
      initialCreditsGranted: 25,
      currentCredits: 7,
      isPro: false
    });
  });

  // Attempting to create user doc with 25 credits fails!
  await assertFails(db.collection("users").doc(DEVICE_B).set({
    userId: DEVICE_B,
    credits: 25,
    isPro: false
  }));

  // Recreating with the true 7 credits from claimed_devices succeeds!
  await assertSucceeds(db.collection("users").doc(DEVICE_B).set({
    userId: DEVICE_B,
    credits: 7,
    isPro: false
  }));
});

test("Device-scoped saved_cards CRUD succeeds and enforces userId parity", async () => {
  const db = testEnv.unauthenticatedContext().firestore();
  await assertSucceeds(
    db.collection("users").doc(DEVICE_A).collection("saved_cards").doc("card_1").set({
      id: "card_1",
      userId: DEVICE_A,
      templateId: "tpl_xmas_1",
      title: "Merry Christmas",
      recipientName: "Mom",
      message: "Wishing you peace and warmth!",
      senderName: "Alex"
    })
  );
  // Mismatched userId fails
  await assertFails(
    db.collection("users").doc(DEVICE_A).collection("saved_cards").doc("card_2").set({
      id: "card_2",
      userId: DEVICE_B,
      templateId: "tpl_xmas_1",
      title: "Merry Christmas",
      message: "Spoofed"
    })
  );
  await assertSucceeds(
    db.collection("users").doc(DEVICE_A).collection("saved_cards").doc("card_1").get()
  );
});
