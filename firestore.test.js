const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_OWNER_UID = "alice_owner";
const BOB_STAFF_UID = "bob_staff";
const CHARLIE_STRANGER_UID = "charlie_stranger";

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

// 1. Unauthenticated Security Boundary
test("Unauthenticated: cannot read or write any user data", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("users").doc(ALICE_OWNER_UID).collection("products").get());
});

// 2. Multi-User Isolation: Stranger cannot access Alice's shop
test("Cross-User: Stranger Charlie cannot access Alice's products", async () => {
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context.firestore().collection("users").doc(ALICE_OWNER_UID).collection("products").doc("prod1").set({
      userId: ALICE_OWNER_UID,
      name: "Chini",
      category: "Grocery",
      buyPrice: 120,
      sellPrice: 135,
      stockQuantity: 50,
      unit: "কেজি",
      lowStockThreshold: 10,
      memberUids: [BOB_STAFF_UID],
      createdAt: new Date(),
    });
  });

  const charlieDb = testEnv.authenticatedContext(CHARLIE_STRANGER_UID).firestore();
  await assertFails(charlieDb.collection("users").doc(ALICE_OWNER_UID).collection("products").doc("prod1").get());
});

// 3. Authorized Staff Member Access: Bob (in memberUids) CAN read product
test("Staff Access: Bob staff member can read products", async () => {
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context.firestore().collection("users").doc(ALICE_OWNER_UID).collection("products").doc("prod1").set({
      userId: ALICE_OWNER_UID,
      name: "Chini",
      category: "Grocery",
      buyPrice: 120,
      sellPrice: 135,
      stockQuantity: 50,
      unit: "কেজি",
      lowStockThreshold: 10,
      memberUids: [BOB_STAFF_UID],
      createdAt: new Date(),
    });
  });

  const bobDb = testEnv.authenticatedContext(BOB_STAFF_UID).firestore();
  await assertSucceeds(bobDb.collection("users").doc(ALICE_OWNER_UID).collection("products").doc("prod1").get());
});

// 4. Staff Member can record sale
test("Staff Access: Bob staff can record a sale", async () => {
  const bobDb = testEnv.authenticatedContext(BOB_STAFF_UID).firestore();
  await assertSucceeds(
    bobDb.collection("users").doc(ALICE_OWNER_UID).collection("sales").doc("sale_1").set({
      userId: ALICE_OWNER_UID,
      invoiceNumber: "INV-101",
      customerName: "Walk-in",
      itemsSummary: "Chini x1",
      totalAmount: 135,
      discount: 0,
      grandTotal: 135,
      paidAmount: 135,
      dueAmount: 0,
      paymentMethod: "CASH",
      profit: 15,
      staffName: "Bob Staff",
      memberUids: [BOB_STAFF_UID],
      createdAt: new Date(),
    })
  );
});

// 5. Store Code Lookup
test("Store Codes: Owner can register code, anyone signed in can lookup", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_OWNER_UID).firestore();
  await assertSucceeds(
    aliceDb.collection("store_codes").doc("NSB-1234").set({
      ownerId: ALICE_OWNER_UID,
      shopName: "Alice Dokan",
      code: "NSB-1234",
      createdAt: new Date(),
    })
  );

  const bobDb = testEnv.authenticatedContext(BOB_STAFF_UID).firestore();
  await assertSucceeds(bobDb.collection("store_codes").doc("NSB-1234").get());
});

// 6. Custom Role & Granular Permissions Roster
test("Staff Roles: Owner can add partner with customTitle and granular permissions", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_OWNER_UID).firestore();
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_OWNER_UID).collection("staff").doc("staff_partner_1").set({
      userId: ALICE_OWNER_UID,
      name: "Rafiq Partner",
      role: "PARTNER",
      customTitle: "ব্যবসায়িক অংশীদার",
      permissions: ["perm_pos_sales", "perm_dues_book", "perm_stock_manage", "perm_reports"],
      pin: "5678",
      isActive: true,
      createdAt: new Date(),
    })
  );
});

// 7. Custom Role Definitions
test("Custom Roles: Owner can define new custom role with permissions", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_OWNER_UID).firestore();
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_OWNER_UID).collection("custom_roles").doc("role_storekeeper").set({
      userId: ALICE_OWNER_UID,
      roleName: "স্টোর কিপার (Storekeeper)",
      roleCode: "STOREKEEPER",
      description: "ইনভেন্টরি ও পণ্য মজুত তদারকি",
      colorHex: "#1565C0",
      permissions: ["perm_stock_manage"],
      isDefault: false,
      createdAt: new Date(),
    })
  );
});

