package com.example.restaurantapp

import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat

class HomeActivity : AppCompatActivity() {

    private lateinit var gridTables: GridLayout
    private lateinit var lvActiveOrders: LinearLayout
    private lateinit var dbHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        gridTables = findViewById(R.id.gridTables)
        lvActiveOrders = findViewById(R.id.lvActiveOrders)
        dbHelper = DatabaseHelper(this)

        setupTableButtons()
        loadActiveOrders()
    }

    private fun setupTableButtons() {
        for (i in 1..10) {
            val button = Button(this).apply {
                text = "Table $i"
                textSize = 16f
                setBackgroundColor(ContextCompat.getColor(context, R.color.teal_700))
                setTextColor(ContextCompat.getColor(context, android.R.color.white))
                setPadding(20, 20, 20, 20)

                setOnClickListener {
                    val intent = Intent(this@HomeActivity, OrderActivity::class.java)
                    intent.putExtra("tableNumber", i)
                    intent.putExtra("username", "waiter") // Can be fetched from login session
                    startActivity(intent)
                }
            }

            val params = GridLayout.LayoutParams().apply {
                width = 0
                height = ViewGroup.LayoutParams.WRAP_CONTENT
                columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
                setMargins(16, 16, 16, 16)
            }

            gridTables.addView(button, params)
        }
    }

    private fun loadActiveOrders() {
        lvActiveOrders.removeAllViews()
        val cursor = dbHelper.getActiveOrders()

        while (cursor.moveToNext()) {
            val table = cursor.getInt(cursor.getColumnIndexOrThrow("tableNumber"))
            val items = cursor.getString(cursor.getColumnIndexOrThrow("items"))
            val status = cursor.getString(cursor.getColumnIndexOrThrow("status"))
            val time = cursor.getString(cursor.getColumnIndexOrThrow("timestamp"))

            val card = CardView(this).apply {
                radius = 16f
                setCardBackgroundColor(ContextCompat.getColor(context, R.color.white))
                cardElevation = 8f
                useCompatPadding = true
                setContentPadding(24, 24, 24, 24)

                val orderText = TextView(this@HomeActivity).apply {
                    text = "Table $table • $time\n$status → $items"
                    textSize = 16f
                    setTextColor(ContextCompat.getColor(context, android.R.color.black))
                }

                addView(orderText)
            }

            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(16, 16, 16, 16)
            }

            lvActiveOrders.addView(card, params)
        }

        cursor.close()
    }

    override fun onResume() {
        super.onResume()
        loadActiveOrders()
    }
}
