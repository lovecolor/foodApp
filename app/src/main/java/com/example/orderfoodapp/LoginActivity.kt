package com.example.orderfoodapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        supportActionBar?.title="Login"
        backtoregistration_text_view.setOnClickListener{
            val intent=Intent(this,RegisterActivity::class.java)
            startActivity(intent)
        }
        login_button_login.setOnClickListener {

            val email=email_edittext_login.text.toString()
            val password=password_edittext_login.text.toString()
            FirebaseAuth.getInstance().signInWithEmailAndPassword(email,password).addOnSuccessListener {

                val intent=Intent(this,RestaurantsActivity::class.java)
                intent.flags=Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            }
                    .addOnFailureListener{
                        Toast.makeText(this, "Email or Password is Wrong!", Toast.LENGTH_SHORT).show()
                    }
        }
    }
}