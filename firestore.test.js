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
const ADMIN_UID = "admin_789";

const [emulatorHost, emulatorPortStr] = (process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085").split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

function nowTs() {
  return new Date();
}

function makeValidUser(uid, overrides = {}) {
  const ts = nowTs();
  return {
    userId: uid,
    nickname: "보물여우",
    avatar: "🦊",
    level: 1,
    exp: 0,
    coins: 100,
    score: 0,
    weeklyScore: 0,
    treasureCount: 0,
    banned: false,
    createdAt: ts,
    updatedAt: ts,
    ...overrides,
  };
}

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

test("Unauthenticated user cannot read or write any collection", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).get());
  await assertFails(unauthDb.collection("leaderboard").get());
  await assertFails(unauthDb.collection("treasures").get());
  await assertFails(unauthDb.collection("items").get());
  await assertFails(unauthDb.collection("notifications").get());
});

test("Alice can create her initial profile and read her own profile", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID, { email: "alice@test.com", email_verified: true }).firestore();
  await assertSucceeds(aliceDb.collection("users").doc(ALICE_UID).set(makeValidUser(ALICE_UID)));
  await assertSucceeds(aliceDb.collection("users").doc(ALICE_UID).get());
  await assertSucceeds(aliceDb.collection("users").where("userId", "==", ALICE_UID).get());
});

test("Alice cannot create profile with cheated coins or banned=true or ghost field", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertFails(aliceDb.collection("users").doc(ALICE_UID).set(makeValidUser(ALICE_UID, { coins: 9999 })));
  await assertFails(aliceDb.collection("users").doc(ALICE_UID).set(makeValidUser(ALICE_UID, { level: 10 })));
  await assertFails(aliceDb.collection("users").doc(ALICE_UID).set(makeValidUser(ALICE_UID, { banned: true })));
  await assertFails(aliceDb.collection("users").doc(ALICE_UID).set({ ...makeValidUser(ALICE_UID), isAdmin: true }));
});

test("Cross-user isolation: Bob cannot read or modify Alice profile, inventory, or collection", async () => {
  await testEnv.withSecurityRulesDisabled(async (ctx) => {
    const db = ctx.firestore();
    await db.collection("users").doc(ALICE_UID).set(makeValidUser(ALICE_UID));
    await db.collection("users").doc(ALICE_UID).collection("inventory").doc("item_1").set({
      itemId: "item_1",
      count: 2,
      updatedAt: nowTs(),
    });
  });

  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  await assertFails(bobDb.collection("users").doc(ALICE_UID).get());
  await assertFails(bobDb.collection("users").doc(ALICE_UID).update({ coins: 500 }));
  await assertFails(bobDb.collection("users").doc(ALICE_UID).collection("inventory").get());
  await assertFails(bobDb.collection("users").doc(ALICE_UID).collection("collection").get());
});

test("Max-delta & banned guards: user cannot jump coins/score outrageously or write when banned", async () => {
  await testEnv.withSecurityRulesDisabled(async (ctx) => {
    const db = ctx.firestore();
    await db.collection("users").doc(ALICE_UID).set(makeValidUser(ALICE_UID));
    await db.collection("users").doc(BOB_UID).set(makeValidUser(BOB_UID, { banned: true }));
  });

  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  // Normal round reward succeeds
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).update({
      coins: 250,
      score: 400,
      weeklyScore: 400,
      treasureCount: 4,
      updatedAt: nowTs(),
    })
  );
  // Excessive coin jump fails
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).update({
      coins: 100000,
      updatedAt: nowTs(),
    })
  );
  // Self-ban toggle fails
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).update({
      banned: true,
      updatedAt: nowTs(),
    })
  );

  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  await assertFails(
    bobDb.collection("users").doc(BOB_UID).update({
      banned: false,
      updatedAt: nowTs(),
    })
  );
  await assertFails(
    bobDb.collection("users").doc(BOB_UID).update({
      coins: 110,
      updatedAt: nowTs(),
    })
  );
  await assertFails(
    bobDb.collection("users").doc(BOB_UID).collection("inventory").doc("item_1").set({
      itemId: "item_1",
      count: 1,
      updatedAt: nowTs(),
    })
  );
});

test("Email spoofing test: unverified admin email cannot write catalog or ban users", async () => {
  const spoofAdminDb = testEnv.authenticatedContext(ADMIN_UID, {
    email: "acehwan69@gmail.com",
    email_verified: false,
  }).firestore();

  await assertFails(
    spoofAdminDb.collection("treasures").doc("t_1").set({
      name: "가짜 보물",
      desc: "테스트",
      rarity: "legend",
      minLevel: 1,
      score: 100,
      coins: 50,
      emoji: "💎",
      active: true,
      updatedAt: nowTs(),
    })
  );
});

test("Verified admin can manage treasures, items, configs, notifications, events, and ban users", async () => {
  await testEnv.withSecurityRulesDisabled(async (ctx) => {
    await ctx.firestore().collection("users").doc(ALICE_UID).set(makeValidUser(ALICE_UID));
  });

  const adminDb = testEnv.authenticatedContext(ADMIN_UID, {
    email: "acehwan69@gmail.com",
    email_verified: true,
  }).firestore();

  await assertSucceeds(
    adminDb.collection("treasures").doc("t_gold").set({
      name: "황금 동전",
      desc: "반짝이는 금화",
      rarity: "rare",
      minLevel: 5,
      score: 300,
      coins: 120,
      emoji: "🪙",
      active: true,
      updatedAt: nowTs(),
    })
  );

  await assertSucceeds(
    adminDb.collection("notifications").doc("noti_1").set({
      title: "새 보물 등장!",
      body: "주말 이벤트가 시작되었어요",
      targetMinLevel: 1,
      createdBy: ADMIN_UID,
      createdAt: nowTs(),
    })
  );

  await assertSucceeds(
    adminDb.collection("users").doc(ALICE_UID).update({
      banned: true,
      updatedAt: nowTs(),
    })
  );

  // Regular user cannot write treasures or notifications
  const aliceDb = testEnv.authenticatedContext(ALICE_UID, { email: "alice@test.com", email_verified: true }).firestore();
  await assertFails(
    aliceDb.collection("treasures").doc("t_hack").set({
      name: "해킹 보물",
      desc: "불가",
      rarity: "common",
      minLevel: 1,
      score: 10,
      coins: 5,
      emoji: "🪨",
      active: true,
      updatedAt: nowTs(),
    })
  );
});
