package com.example.restaurantapp

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import android.widget.TextView

class LoginActivity : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper
    private lateinit var etUsername: TextInputEditText
    private lateinit var etPassword: TextInputEditText
    private lateinit var btnLogin: MaterialButton
    private lateinit var tvError: TextView
    private lateinit var layoutUsername: TextInputLayout
    private lateinit var layoutPassword: TextInputLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        dbHelper = DatabaseHelper(this)

        // Material Design views
        etUsername = findViewById(R.id.etUsername)
        etPassword = findViewById(R.id.etPassword)
        btnLogin = findViewById(R.id.btnLogin)
        tvError = findViewById(R.id.tvError)
        layoutUsername = findViewById(R.id.layoutUsername)
        layoutPassword = findViewById(R.id.layoutPassword)

        btnLogin.setOnClickListener {
            val user = etUsername.text.toString().trim()
            val pass = etPassword.text.toString().trim()

            if (user.isEmpty() || pass.isEmpty()) {
                layoutUsername.error = if (user.isEmpty()) "Username required" else null
                layoutPassword.error = if (pass.isEmpty()) "Password required" else null
                return@setOnClickListener
            } else {
                layoutUsername.error = null
                layoutPassword.error = null
            }

            val role = dbHelper.validateUser(user, pass)

            if (!role.isNullOrEmpty()) {
                Toast.makeText(this, "Welcome $user!", Toast.LENGTH_SHORT).show()

                when (role) {
                    "admin" -> startActivity(Intent(this, AdminActivity::class.java))
                    "kitchen" -> startActivity(Intent(this, KitchenActivity::class.java))
                    "server" -> startActivity(Intent(this, HomeActivity::class.java))
                    "welcome" -> startActivity(Intent(this, WelcomeActivity::class.java))
                    "till" -> Toast.makeText(this, "Till view pending", Toast.LENGTH_SHORT).show()
                    else -> Toast.makeText(this, "Unknown role: $role", Toast.LENGTH_SHORT).show()
                }

                finish()
            } else {
                tvError.text = "Invalid username or password"
                tvError.visibility = View.VISIBLE
            }
        }
    }
}
