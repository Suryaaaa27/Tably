package com.example.restaurantapp

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DB_NAME, null, DB_VERSION) {

    companion object {
        const val DB_NAME = "RestaurantDB.db"
        const val DB_VERSION = 1

        const val TABLE_USERS = "users"
        const val TABLE_ORDERS = "orders"
        const val TABLE_ITEMS = "items"
    }

    override fun onCreate(db: SQLiteDatabase) {
        // Create Users Table
        db.execSQL("""
            CREATE TABLE $TABLE_USERS (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                username TEXT UNIQUE,
                password TEXT,
                role TEXT
            )
        """.trimIndent())

        // Create Orders Table
        db.execSQL("""
            CREATE TABLE $TABLE_ORDERS (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                tableNumber INTEGER,
                items TEXT,
                username TEXT,
                status TEXT,
                timestamp TEXT
            )
        """.trimIndent())

        // Create Menu Items Table
        db.execSQL("""
            CREATE TABLE $TABLE_ITEMS (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT,
                available INTEGER
            )
        """.trimIndent())

        // Pre-populate users
        insertUser(db, "waitress", "p455w0rd", "server")
        insertUser(db, "waiter", "p455w0rd", "server")
        insertUser(db, "chef", "p455w0rd", "kitchen")
        insertUser(db, "admin", "p455w0rd", "admin")
        insertUser(db, "welcome", "p455w0rd", "welcome")

        // Pre-populate menu items
        insertMenuItem(db, "Pizza")
        insertMenuItem(db, "Burger")
        insertMenuItem(db, "Pasta")
        insertMenuItem(db, "Salad")
        insertMenuItem(db, "Soup")
        insertMenuItem(db, "Fries")
        insertMenuItem(db, "Soda")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_USERS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_ORDERS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_ITEMS")
        onCreate(db)
    }

    // ---------------------
    // USER MANAGEMENT
    // ---------------------

    private fun insertUser(db: SQLiteDatabase, username: String, password: String, role: String) {
        val values = ContentValues().apply {
            put("username", username)
            put("password", password)
            put("role", role)
        }
        db.insert(TABLE_USERS, null, values)
    }

    fun validateUser(username: String, password: String): String? {
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT role FROM $TABLE_USERS WHERE username = ? AND password = ?",
            arrayOf(username, password)
        )
        var role: String? = null
        if (cursor.moveToFirst()) {
            role = cursor.getString(0)
        }
        cursor.close()
        return role
    }

    fun addUser(username: String, password: String, role: String): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("username", username)
            put("password", password)
            put("role", role)
        }
        return try {
            db.insertOrThrow(TABLE_USERS, null, values)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun getAllUsers(): List<Pair<String, String>> {
        val users = mutableListOf<Pair<String, String>>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT username, role FROM $TABLE_USERS", null)
        while (cursor.moveToNext()) {
            users.add(cursor.getString(0) to cursor.getString(1))
        }
        cursor.close()
        return users
    }

    // ---------------------
    // ORDER MANAGEMENT
    // ---------------------

    fun insertOrder(
        tableNumber: Int,
        items: String,
        username: String,
        timestamp: String,
        status: String
    ): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("tableNumber", tableNumber)
            put("items", items)
            put("username", username)
            put("status", status)
            put("timestamp", timestamp)
        }
        return db.insert(TABLE_ORDERS, null, values) != -1L
    }

    fun getActiveOrders(): Cursor {
        val db = readableDatabase
        return db.rawQuery(
            "SELECT id, tableNumber, items, status, timestamp FROM $TABLE_ORDERS WHERE status IN ('placed', 'ready') ORDER BY timestamp DESC",
            null
        )
    }

    fun getPlacedOrders(): Cursor {
        val db = readableDatabase
        return db.rawQuery(
            "SELECT id, tableNumber, items, timestamp FROM $TABLE_ORDERS WHERE status = 'placed' ORDER BY timestamp ASC",
            null
        )
    }

    fun markOrderAsReady(orderId: Int): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("status", "ready")
        }
        val rows = db.update(TABLE_ORDERS, values, "id = ?", arrayOf(orderId.toString()))
        return rows > 0
    }

    fun isTableOccupied(tableNumber: Int): Boolean {
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT id FROM $TABLE_ORDERS WHERE tableNumber = ? AND status IN ('assigned', 'placed')",
            arrayOf(tableNumber.toString())
        )
        val occupied = cursor.count > 0
        cursor.close()
        return occupied
    }

    fun getOrderSummary(tableNumber: Int): Cursor {
        val db = readableDatabase
        return db.rawQuery(
            "SELECT items, timestamp FROM $TABLE_ORDERS WHERE tableNumber = ? ORDER BY timestamp DESC LIMIT 1",
            arrayOf(tableNumber.toString())
        )
    }

    // ---------------------
    // MENU MANAGEMENT
    // ---------------------

    private fun insertMenuItem(db: SQLiteDatabase, name: String) {
        val values = ContentValues().apply {
            put("name", name)
            put("available", 1)
        }
        db.insert(TABLE_ITEMS, null, values)
    }

    fun getAvailableMenuItems(): List<String> {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT name FROM $TABLE_ITEMS WHERE available = 1", null)
        val items = mutableListOf<String>()
        while (cursor.moveToNext()) {
            items.add(cursor.getString(0))
        }
        cursor.close()
        return items
    }

    fun hideMenuItem(name: String): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply { put("available", 0) }
        return db.update(TABLE_ITEMS, values, "name = ?", arrayOf(name)) > 0
    }

    fun removeMenuItem(name: String): Boolean {
        val db = writableDatabase
        return db.delete(TABLE_ITEMS, "name = ?", arrayOf(name)) > 0
    }

    fun addNewMenuItem(name: String): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("name", name)
            put("available", 1)
        }
        return db.insert(TABLE_ITEMS, null, values) != -1L
    }
}
