package com.example.data.repository

import com.example.base.FirestoreEmulatorTestBase
import com.example.model.CartItem
import com.example.model.Customer
import com.example.model.Product
import com.example.model.Sale
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import java.util.UUID

class DokanRepositoryRuleTest : FirestoreEmulatorTestBase() {

  @Test
  fun addProduct_validPayload_createsDocumentAndReturnsId() = runBlocking {
    val uid: String = signInTestUser("merchant_${UUID.randomUUID()}@test.com")
    val repo = DokanRepository(firestore)

    val product = Product(
      userId = uid,
      name = "Nazirshail Rice",
      category = "Grocery",
      buyPrice = 70.0,
      sellPrice = 80.0,
      stockQuantity = 50.0,
      unit = "কেজি",
      lowStockThreshold = 10.0
    )

    val productId = withTimeout(DEFAULT_TIMEOUT_MS) {
      repo.addProduct(uid, product)
    }
    assertTrue(productId.isNotEmpty())
  }

  @Test
  fun observeProducts_authenticatedOwner_emitsProducts() = runBlocking {
    val uid: String = signInTestUser("merchant_${UUID.randomUUID()}@test.com")
    val repo = DokanRepository(firestore)

    val product = Product(
      userId = uid,
      name = "Mustard Oil",
      category = "Grocery",
      buyPrice = 180.0,
      sellPrice = 210.0,
      stockQuantity = 25.0,
      unit = "লিটার",
      lowStockThreshold = 5.0
    )

    val productId = withTimeout(DEFAULT_TIMEOUT_MS) {
      repo.addProduct(uid, product)
    }

    val emittedProducts = withTimeout(FLOW_TIMEOUT_MS) {
      repo.observeProducts(uid).first { list -> list.any { it.id == productId } }
    }
    assertTrue(emittedProducts.any { it.id == productId })
  }

  @Test
  fun recordSale_validSale_createsSaleDocument() = runBlocking {
    val uid: String = signInTestUser("merchant_${UUID.randomUUID()}@test.com")
    val repo = DokanRepository(firestore)

    val product = Product(
      userId = uid,
      name = "Soap",
      category = "Toiletries",
      buyPrice = 45.0,
      sellPrice = 55.0,
      stockQuantity = 20.0,
      unit = "পিস",
      lowStockThreshold = 5.0
    )
    val productId = repo.addProduct(uid, product)
    val savedProduct = product.copy(id = productId)

    val cartItem = CartItem(product = savedProduct, quantity = 2.0, customPrice = 55.0)
    val sale = Sale(
      userId = uid,
      invoiceNumber = "INV-101",
      customerName = "Cash Buyer",
      itemsSummary = "Soap x2",
      totalAmount = 110.0,
      discount = 0.0,
      grandTotal = 110.0,
      paidAmount = 110.0,
      dueAmount = 0.0,
      paymentMethod = "Cash",
      profit = 20.0
    )

    val saleId = withTimeout(DEFAULT_TIMEOUT_MS) {
      repo.recordSale(uid, sale, listOf(cartItem))
    }
    assertTrue(saleId.isNotEmpty())
  }

  @Test
  fun recordSale_withTaxAndDiscount_persistsProperly() = runBlocking {
    val uid: String = signInTestUser("merchant_${UUID.randomUUID()}@test.com")
    val repo = DokanRepository(firestore)

    val product = Product(
      userId = uid,
      name = "Mustard Oil",
      category = "Grocery",
      buyPrice = 180.0,
      sellPrice = 220.0,
      stockQuantity = 10.0,
      unit = "লিটার",
      lowStockThreshold = 2.0
    )
    val productId = repo.addProduct(uid, product)
    val savedProduct = product.copy(id = productId)
    val cartItem = CartItem(product = savedProduct, quantity = 1.0, customPrice = 220.0)

    val subtotal = 220.0
    val discount = 20.0
    val taxRate = 5.0
    val taxAmount = 10.0
    val grandTotal = 210.0

    val sale = Sale(
      userId = uid,
      invoiceNumber = "INV-TAX-101",
      customerName = "VAT Buyer",
      itemsSummary = "Mustard Oil x1",
      totalAmount = subtotal,
      discount = discount,
      taxRate = taxRate,
      taxAmount = taxAmount,
      grandTotal = grandTotal,
      paidAmount = grandTotal,
      dueAmount = 0.0,
      paymentMethod = "নগদ (Cash)",
      profit = (220.0 - 180.0) - discount
    )

    val saleId = withTimeout(DEFAULT_TIMEOUT_MS) {
      repo.recordSale(uid, sale, listOf(cartItem))
    }
    assertTrue(saleId.isNotEmpty())

    val sales = withTimeout(FLOW_TIMEOUT_MS) {
      repo.observeSales(uid).first { list -> list.any { it.id == saleId } }
    }
    val savedSale = sales.first { it.id == saleId }
    assertEquals(5.0, savedSale.taxRate, 0.01)
    assertEquals(10.0, savedSale.taxAmount, 0.01)
    assertEquals(210.0, savedSale.grandTotal, 0.01)
  }

  @Test
  fun addCustomerAndTransaction_updatesCustomerDue() = runBlocking {
    val uid: String = signInTestUser("merchant_${UUID.randomUUID()}@test.com")
    val repo = DokanRepository(firestore)

    val customer = Customer(
      userId = uid,
      name = "Kabir Hasan",
      phone = "01812345678",
      totalDue = 250.0
    )
    val custId = withTimeout(DEFAULT_TIMEOUT_MS) {
      repo.addCustomer(uid, customer)
    }
    assertTrue(custId.isNotEmpty())

    // Add additional due
    withTimeout(DEFAULT_TIMEOUT_MS) {
      repo.addCustomerTransaction(uid, custId, "DUE_GIVEN", 150.0, "বাকিতে কেনাকাটা")
    }

    val customers = withTimeout(FLOW_TIMEOUT_MS) {
      repo.observeCustomers(uid).first { list -> list.any { it.id == custId && it.totalDue == 400.0 } }
    }
    val targetCustomer = customers.first { it.id == custId }
    assertEquals(400.0, targetCustomer.totalDue, 0.01)
  }

  @Test
  fun crossUserAccess_failsWithPermissionDenied() = runBlocking {
    val aliceUid: String = signInTestUser("alice_${UUID.randomUUID()}@test.com")
    val aliceRepo = DokanRepository(firestore)
    aliceRepo.addProduct(aliceUid, Product(
      userId = aliceUid,
      name = "Alice Tea",
      category = "Grocery",
      buyPrice = 100.0,
      sellPrice = 120.0,
      stockQuantity = 10.0,
      unit = "প্যাকেট",
      lowStockThreshold = 2.0
    ))

    // Switch to Bob
    val bobUid: String = signInTestUser("bob_${UUID.randomUUID()}@test.com")
    val bobRepo = DokanRepository(firestore)

    try {
      withTimeout(FLOW_TIMEOUT_MS) {
        bobRepo.observeProducts(aliceUid).first()
      }
      fail("Expected exception for cross-user access")
    } catch (e: Exception) {
      val firestoreException = checkNotNull(e.unwrapFirestoreException()) {
        "Expected nested FirebaseFirestoreException, got $e"
      }
      assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, firestoreException.code)
    }
  }

  @Test
  fun unauthenticatedAccess_failsWithPermissionDenied() = runBlocking {
    val uid: String = signInTestUser("temp_${UUID.randomUUID()}@test.com")
    auth.signOut()

    val repo = DokanRepository(firestore)
    try {
      withTimeout(FLOW_TIMEOUT_MS) {
        repo.observeProducts(uid).first()
      }
      fail("Expected exception for unauthenticated access")
    } catch (e: Exception) {
      val firestoreException = checkNotNull(e.unwrapFirestoreException()) {
        "Expected nested FirebaseFirestoreException, got $e"
      }
      assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, firestoreException.code)
    }
  }

  private companion object {
    const val DEFAULT_TIMEOUT_MS = 6000L
    const val FLOW_TIMEOUT_MS = 4000L
  }
}
