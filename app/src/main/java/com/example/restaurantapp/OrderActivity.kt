package com.example.restaurantapp

import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import java.text.SimpleDateFormat
import java.util.*

class OrderActivity : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper
    private lateinit var itemLayout: LinearLayout
    private lateinit var orderSummary: TextView
    private lateinit var btnDone: Button

    private var selectedItems = mutableMapOf<String, Int>()
    private var tableNumber = 0
    private lateinit var placedBy: String

    private val menuItems = listOf("Pizza", "Burger", "Pasta", "Fries", "Salad", "Soup")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_order)

        dbHelper = DatabaseHelper(this)
        tableNumber = intent.getIntExtra("tableNumber", 0)
        placedBy = intent.getStringExtra("username") ?: "unknown"

        itemLayout = findViewById(R.id.itemLayout)
        orderSummary = findViewById(R.id.tvOrderSummary)
        btnDone = findViewById(R.id.btnDone)

        setupMenuButtons()
        btnDone.setOnClickListener { submitOrder() }
    }

    private fun setupMenuButtons() {
        for (item in menuItems) {
            val button = MaterialButton(this).apply {
                text = item
                textSize = 18f
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(0, 12, 0, 12)
                }
                setOnClickListener {
                    selectedItems[item] = selectedItems.getOrDefault(item, 0) + 1
                    updateSummary()
                }
            }
            itemLayout.addView(button)
        }
    }

    private fun updateSummary() {
        val summary = selectedItems.entries.joinToString("\n") {
            "${it.key} x${it.value}"
        }
        orderSummary.text = summary
    }

    private fun submitOrder() {
        if (selectedItems.isEmpty()) {
            Toast.makeText(this, "No items selected", Toast.LENGTH_SHORT).show()
            return
        }

        val timestamp = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
        val itemString = selectedItems.entries.joinToString(", ") { "${it.key} x${it.value}" }

        val inserted = dbHelper.insertOrder(
            tableNumber,
            itemString,
            placedBy,
            timestamp,
            "placed"
        )

        if (inserted) {
            Toast.makeText(this, "Order placed successfully!", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, HomeActivity::class.java))
            finish()
        } else {
            Toast.makeText(this, "Failed to place order. Try again.", Toast.LENGTH_SHORT).show()
        }
    }
}
