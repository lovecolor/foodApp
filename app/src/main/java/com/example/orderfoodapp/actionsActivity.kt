package com.example.orderfoodapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.DividerItemDecoration
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_actions.*

class actionsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_actions)
        supportActionBar?.title="General"
        loadUser()
        setUp()




    }
    private fun setUp(){
        username_textView_action.setOnClickListener {
            val intent=Intent(this,EditInfomationActivity::class.java)
            startActivity(intent)

        }
        textView_my_order_action.setOnClickListener {
            val intent=Intent(this,HistoryOrderActivity::class.java)
            startActivity(intent)
        }
    }
    private fun loadUser(){
        Picasso.get().load(RestaurantsActivity.currentUser?.profileImageUrl).into(circleimageview_action)
        username_textView_action.text=RestaurantsActivity.currentUser?.username

    }
}
