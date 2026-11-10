package com.example.data.local

import kotlinx.coroutines.flow.Flow

/**
 * Repository abstracting access to local Room [Product] storage.
 */
class LocalProductRepository(private val productDao: ProductDao) {
    val allProducts: Flow<List<Product>> = productDao.getAllProducts()
    val lowStockProducts: Flow<List<Product>> = productDao.getLowStockProducts()

    fun searchProducts(query: String): Flow<List<Product>> = productDao.searchProducts(query)

    suspend fun getProductById(id: Long): Product? = productDao.getProductById(id)

    suspend fun getProductByBarcode(barcode: String): Product? = productDao.getProductByBarcode(barcode)

    suspend fun insertProduct(product: Product): Long = productDao.insertProduct(product)

    suspend fun insertProducts(products: List<Product>): List<Long> = productDao.insertProducts(products)

    suspend fun updateProduct(product: Product) = productDao.updateProduct(product)

    suspend fun deleteProduct(product: Product) = productDao.deleteProduct(product)

    suspend fun deleteProductById(id: Long) = productDao.deleteProductById(id)

    suspend fun clearAllProducts() = productDao.clearAllProducts()
}
