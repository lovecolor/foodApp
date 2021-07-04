package com.example.orderfoodapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_edit_infomation.*

class EditInfomationActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_infomation)
        supportActionBar?.title="Infomation"
        loadUser()

        signout_textview_edit_info.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            val intent= Intent(this,LoginActivity::class.java)
            intent.flags=Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }
    }
    private fun loadUser()
    {
        phone_textview_edit_info.text=RestaurantsActivity.currentUser?.phone
        editTextTextPersonName_edit_ifo.setText(RestaurantsActivity.currentUser?.username)

    }
}