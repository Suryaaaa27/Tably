package com.example.restaurantapp

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText

class AdminActivity : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin)

        dbHelper = DatabaseHelper(this)

        // Accounts UI Elements
        val etUsername: TextInputEditText = findViewById(R.id.etUsername)
        val etPassword: TextInputEditText = findViewById(R.id.etPassword)
        val spnRole: Spinner = findViewById(R.id.spnRole)
        val btnAddUser: Button = findViewById(R.id.btnAddUser)
        val tvAccounts: TextView = findViewById(R.id.tvAccounts)

        // Menu UI Elements
        val etNewItem: TextInputEditText = findViewById(R.id.etNewItem)
        val btnAddItem: Button = findViewById(R.id.btnAddItem)
        val lvMenuItems: ListView = findViewById(R.id.lvMenuItems)

        // Spinner role setup
        val roles = arrayOf("server", "kitchen", "welcome", "admin")
        spnRole.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, roles)

        // Add user functionality
        btnAddUser.setOnClickListener {
            val user = etUsername.text.toString().trim()
            val pass = etPassword.text.toString().trim()
            val role = spnRole.selectedItem.toString()

            if (user.isEmpty() || pass.isEmpty()) {
                Toast.makeText(this, "Username and Password required ⚠️", Toast.LENGTH_SHORT).show()
            } else {
                val added = dbHelper.addUser(user, pass, role)
                if (added) {
                    Toast.makeText(this, "✅ User '$user' added", Toast.LENGTH_SHORT).show()
                    etUsername.text?.clear()
                    etPassword.text?.clear()
                    loadAccounts(tvAccounts)
                } else {
                    Toast.makeText(this, "❌ User already exists or failed", Toast.LENGTH_SHORT).show()
                }
            }
        }

        // Add menu item functionality
        btnAddItem.setOnClickListener {
            val item = etNewItem.text.toString().trim()
            if (item.isEmpty()) {
                Toast.makeText(this, "Menu item cannot be empty 🍽️", Toast.LENGTH_SHORT).show()
            } else {
                val success = dbHelper.addNewMenuItem(item)
                if (success) {
                    Toast.makeText(this, "✅ '$item' added to menu", Toast.LENGTH_SHORT).show()
                    etNewItem.text?.clear()
                    loadMenuItems(lvMenuItems)
                } else {
                    Toast.makeText(this, "❌ Failed to add item", Toast.LENGTH_SHORT).show()
                }
            }
        }

        // Menu item long-click for action
        lvMenuItems.setOnItemClickListener { _, _, pos, _ ->
            val itemName = lvMenuItems.getItemAtPosition(pos).toString()
            val options = arrayOf("Hide", "Remove")

            AlertDialog.Builder(this)
                .setTitle("Manage: $itemName")
                .setItems(options) { _, index ->
                    when (index) {
                        0 -> {
                            dbHelper.hideMenuItem(itemName)
                            Toast.makeText(this, "👻 '$itemName' hidden", Toast.LENGTH_SHORT).show()
                        }
                        1 -> {
                            dbHelper.removeMenuItem(itemName)
                            Toast.makeText(this, "❌ '$itemName' removed", Toast.LENGTH_SHORT).show()
                        }
                    }
                    loadMenuItems(lvMenuItems)
                }.show()
        }

        // Initial load
        loadAccounts(tvAccounts)
        loadMenuItems(lvMenuItems)
    }

    private fun loadAccounts(tv: TextView) {
        val accounts = dbHelper.getAllUsers()
        tv.text = accounts.joinToString("\n") { "👤 ${it.first} → ${it.second}" }
    }

    private fun loadMenuItems(listView: ListView) {
        val items = dbHelper.getAvailableMenuItems()
        listView.adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, items)
    }
}
