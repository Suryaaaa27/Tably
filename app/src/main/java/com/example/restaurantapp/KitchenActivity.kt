package com.example.restaurantapp

import android.os.Bundle
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import com.google.android.material.button.MaterialButton

class KitchenActivity : AppCompatActivity() {

    private lateinit var layout: LinearLayout
    private lateinit var dbHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_kitchen)

        layout = findViewById(R.id.kitchenLayout)
        dbHelper = DatabaseHelper(this)

        loadPlacedOrders()
    }

    private fun loadPlacedOrders() {
        layout.removeAllViews()

        val cursor = dbHelper.readableDatabase.rawQuery(
            "SELECT id, tablenumber, items, timestamp FROM ${DatabaseHelper.TABLE_ORDERS} WHERE status = 'placed'",
            null
        )

        if (cursor.moveToFirst()) {
            do {
                val orderId = cursor.getInt(0)
                val table = cursor.getInt(1)
                val items = cursor.getString(2)
                val time = cursor.getString(3)

                val card = CardView(this).apply {
                    radius = 16f
                    cardElevation = 8f
                    useCompatPadding = true
                    setCardBackgroundColor(ContextCompat.getColor(context, android.R.color.white))
                    setContentPadding(32, 24, 32, 24)

                    val contentLayout = LinearLayout(context).apply {
                        orientation = LinearLayout.VERTICAL
                    }

                    val summary = TextView(context).apply {
                        text = "🪑 Table $table\n🕒 Time: $time\n🍽️ Items: $items"
                        textSize = 16f
                        setTextColor(ContextCompat.getColor(context, android.R.color.black))
                    }

                    val readyBtn = MaterialButton(context).apply {
                        text = "Mark as Ready"
                        setTextColor(ContextCompat.getColor(context, android.R.color.white))
                        setBackgroundColor(ContextCompat.getColor(context, R.color.teal_700))
                        setOnClickListener {
                            val updated = dbHelper.markOrderAsReady(orderId)
                            if (updated) {
                                Toast.makeText(this@KitchenActivity, "Order marked as Ready", Toast.LENGTH_SHORT).show()
                                loadPlacedOrders()
                            } else {
                                Toast.makeText(this@KitchenActivity, "Failed to update", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }

                    contentLayout.addView(summary)
                    contentLayout.addView(readyBtn)
                    addView(contentLayout)
                }

                val params = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(16, 16, 16, 16)
                }

                layout.addView(card, params)

            } while (cursor.moveToNext())
        } else {
            val empty = TextView(this).apply {
                text = "🍽️ No placed orders currently."
                textSize = 16f
                setPadding(16, 16, 16, 16)
            }
            layout.addView(empty)
        }

        cursor.close()
    }
}
