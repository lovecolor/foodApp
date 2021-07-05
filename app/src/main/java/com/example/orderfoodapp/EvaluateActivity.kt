package com.example.orderfoodapp

import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import com.example.orderfoodapp.models.Restaurant
import com.google.firebase.database.FirebaseDatabase
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_evaluate.*
import kotlinx.android.synthetic.main.header_status_order.*

class EvaluateActivity : AppCompatActivity() {
    var rate:Int=5
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_evaluate)
        supportActionBar?.title="Evaluate"
        setUp()
    }


    private fun setUp()
    {
        Picasso.get().load(StatusOrderActivity.infoShipper?.profileImageUrl).into(imageView_img_evalute)
        textView_name_evalute.text=StatusOrderActivity.infoShipper?.username

        val mutableList=mutableListOf(
                imageButton_rate_1,
                imageButton_rate_2,
                imageButton_rate_3,
                imageButton_rate_4,
                imageButton_rate_5
        )
        fun setRate(){
            for(j in 0..mutableList.size-1)
            {
                var color=Color.parseColor("#EEEEEE")
                if(j<rate) color=Color.parseColor("#FFD700")
                mutableList.get(j).setColorFilter(color)
            }
        }
        for(i in 0..mutableList.size-1){
            mutableList.get(i).setOnClickListener {
                rate=i+1
                setRate()
            }
        }
        button_submit_evaluate_rider.setOnClickListener {

            rate=5
            setRate()
            val restaurant=intent.getParcelableExtra<Restaurant>(RestaurantsActivity.RESTAURANT_KEY)
            textView_title_evaluate.text="How were our restaurant serve?"
            Picasso.get().load(restaurant?.profileImageUrl).into(imageView_img_evalute)
            textView_name_evalute.text=restaurant?.name
            button_submit_evaluate_rider.setOnClickListener {
                val refRate=FirebaseDatabase.getInstance().getReference("/restaurants/${restaurant?.id}/rate")
                val refCountPeopleRate=FirebaseDatabase.getInstance().getReference("/restaurants/${restaurant?.id}/countRate")
                var currentRate=restaurant?.rate
                if(restaurant?.countRate==0) {
                    currentRate=0.0
                }
                val countRate=restaurant?.countRate
                val newRate=(currentRate!!* countRate!! + rate)/(countRate!! +1)

                refRate.setValue(newRate)
                refCountPeopleRate.setValue(countRate+1)

                val intent=Intent(this,RestaurantsActivity::class.java)
                intent.flags=Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            }

        }
    }

}