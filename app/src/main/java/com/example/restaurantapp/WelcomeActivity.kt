package com.example.restaurantapp

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.button.MaterialButton
import java.text.SimpleDateFormat
import java.util.*

class WelcomeActivity : AppCompatActivity() {

    private lateinit var gridTables: GridLayout
    private lateinit var dbHelper: DatabaseHelper
    private val welcomeUser = "welcome_staff" // Replace with login session if needed

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        try {
            setContentView(R.layout.activity_welcome)
            dbHelper = DatabaseHelper(this)
            gridTables = findViewById(R.id.gridTables)

            setupTableButtons()

        } catch (e: Exception) {
            Log.e("WelcomeCrash", "Crash in WelcomeActivity", e)
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun setupTableButtons() {
        gridTables.removeAllViews() // Ensure fresh buttons each time

        for (i in 1..10) {
            val status = try {
                if (dbHelper.isTableOccupied(i)) "occupied" else "free"
            } catch (e: Exception) {
                Log.e("TableStatusError", "Error checking table $i", e)
                "unknown"
            }

            val button = MaterialButton(this).apply {
                text = "Table $i"
                setTextColor(Color.WHITE)
                textSize = 16f
                cornerRadius = 16
                setPadding(24, 24, 24, 24)

                setBackgroundColor(
                    when (status) {
                        "free" -> Color.parseColor("#4CAF50")    // Green
                        "occupied" -> Color.parseColor("#F44336") // Red
                        else -> Color.GRAY
                    }
                )

                setOnClickListener {
                    assignTable(i)
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

    private fun assignTable(tableNumber: Int) {
        try {
            if (dbHelper.isTableOccupied(tableNumber)) {
                Toast.makeText(this, "Table $tableNumber is already occupied!", Toast.LENGTH_SHORT).show()
            } else {
                val time = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
                val assigned = dbHelper.insertOrder(
                    tableNumber,
                    "Assigned (No items yet)",
                    welcomeUser,
                    time,
                    "assigned"
                )

                if (assigned) {
                    Toast.makeText(this, "Table $tableNumber assigned successfully!", Toast.LENGTH_SHORT).show()
                    setupTableButtons() // Refresh without full recreate
                } else {
                    Toast.makeText(this, "Failed to assign table", Toast.LENGTH_SHORT).show()
                }
            }
        } catch (e: Exception) {
            Log.e("AssignTableError", "Error assigning table", e)
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}
