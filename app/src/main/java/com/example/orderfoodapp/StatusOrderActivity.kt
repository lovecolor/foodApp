package com.example.orderfoodapp

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DividerItemDecoration
import com.example.chatrealtime.models.User

import com.example.orderfoodapp.models.Order
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.activity_status_order.*
import kotlinx.android.synthetic.main.header_status_order.view.*
import kotlinx.android.synthetic.main.info_shipper.view.*
import kotlinx.android.synthetic.main.name_restaurant_status_order.view.*
import java.util.*

class StatusOrderActivity : AppCompatActivity() {

    val adapter = GroupAdapter<GroupieViewHolder>()

    companion object {
        var infoShipper: User? = null
//        var hour: Int = 0
//        var minute: Int = 0
        var order: Order? = null
        var isCancel: Boolean = false
        var timeMap = HashMap<String, String>()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_status_order)
        supportActionBar?.title="Status Order"
        isCancel = false
        order = intent.getParcelableExtra(CartActivity.ORDER_KEY)
        setUpAdapter()
        Handler(mainLooper).postDelayed(object : Runnable {
            override fun run() {
                val ref=FirebaseDatabase.getInstance().getReference("/timeStatus/${order?.id}")
                ref.addListenerForSingleValueEvent(object :ValueEventListener{
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if(snapshot.children.count()==1){
                            findDriver()
                        }
                        snapshot.children.forEach{
                            val timeStatus=it.getValue() as String
                            timeMap[it.key!!]=timeStatus
                        }

                    }

                    override fun onCancelled(error: DatabaseError) {

                    }

                })

            }

        }, 2000)


    }

    var isFindSuccess: Boolean = false
    private fun findDriver() {
        if (isCancel) return

        val ref = FirebaseDatabase.getInstance().getReference("/users")


        ref.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {

                val refDeliver = FirebaseDatabase.getInstance().getReference("/delivers/${snapshot.key!!}")
                refDeliver.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {

                        if (snapshot.children.count() == 0 && !isFindSuccess) {

                            refDeliver.setValue(order)
                            isFindSuccess = true
                            val refShipper = FirebaseDatabase.getInstance().getReference("/users/${snapshot.key}")
                            refShipper.addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(snapshot: DataSnapshot) {

                                    val user = snapshot.getValue(User::class.java)
                                    infoShipper = user
                                    adapter.clear()
                                    adapter.add(HeaderStatusItem("Find success"))
                                    adapter.add(ShipperItem(user!!))


                                }

                                override fun onCancelled(error: DatabaseError) {

                                }

                            })
                            return
                        }

                    }

                    override fun onCancelled(error: DatabaseError) {

                    }

                })
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {

            }

            override fun onChildRemoved(snapshot: DataSnapshot) {

            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {

            }


            override fun onCancelled(error: DatabaseError) {

            }

        })
    }

    private fun setUpAdapter() {

        adapter.add(HeaderStatusItem("Finding driver"))
        adapter.add(NameRestaurantItem())
        CartActivity.listMeal.forEach {
            adapter.add(CartActivity.CartRowItem(it, false))
        }

        recycleview_status_order.adapter = adapter
        recycleview_status_order.addItemDecoration(
                DividerItemDecoration(
                        this,
                        DividerItemDecoration.VERTICAL
                )
        )
    }
}

class ShipperItem(val user: User) : Item<GroupieViewHolder>() {
    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        val itemView = viewHolder.itemView
        Picasso.get().load(user.profileImageUrl).into(itemView.imageView_avt_shipper)
        itemView.textView_name_shipper.text = user.username

    }

    override fun getLayout(): Int {
        return R.layout.info_shipper
    }

}

class NameRestaurantItem : Item<GroupieViewHolder>() {
    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        viewHolder.itemView.textView_name_restaurant_head.text = MenuActivity.restaurant?.name
    }

    override fun getLayout(): Int {
        return R.layout.name_restaurant_status_order
    }

}

class HeaderStatusItem(val status: String) : Item<GroupieViewHolder>() {

    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        val itemView = viewHolder.itemView
        var textViewMap = HashMap<String, TextView>()
        textViewMap["arrvied"] = itemView.textView_arrived_head
        textViewMap["checking"] = itemView.textView_checking_header_status_order
        textViewMap["delivering"] = itemView.textView_delivering_head
        textViewMap["preparing"] = itemView.textView_preparing_head
        var textViewTimeMap = HashMap<String, TextView>()
        textViewTimeMap["arrvied"] = itemView.textView_time_arived_header
        textViewTimeMap["delivering"] = itemView.textView__time_delivering_header
        textViewTimeMap["preparing"] = itemView.textView_time_preparing_header
        textViewTimeMap["checking"] = itemView.textView_time_checking_header_status
        fun setTime(key:String,value:String){
            textViewTimeMap[value]?.setText(value)
        }
        fun handleText(key: String) {
            textViewMap[key]?.setTextColor(Color.BLACK)
            textViewMap[key]?.setTextSize(18F)
//            if(key=="delivering") return handleText("preparing")
//            if(key=="arrvied") return handleText("delivering")
        }


        StatusOrderActivity.timeMap.forEach {
            handleText(it.key!!)
            setTime(it.key!!,it.value)
        }
        viewHolder.itemView.btn_cancel_order_header_status_order.setOnClickListener {


            StatusOrderActivity.isCancel = true
            val order = StatusOrderActivity.order
            val ref = FirebaseDatabase.getInstance().getReference("/orders/${FirebaseAuth.getInstance().uid}/${order?.id}/status")

            ref.setValue("Cancelled")


            val intent = Intent(viewHolder.itemView.context, RestaurantsActivity::class.java)
            viewHolder.itemView.context.startActivity(intent)

        }
        viewHolder.itemView.imageButton_close_head_status.setOnClickListener {
            val intent = Intent(viewHolder.itemView.context, RestaurantsActivity::class.java)
            viewHolder.itemView.context.startActivity(intent)

        }
        var time = Calendar.getInstance().getTime();
        if (status == "Finding driver") {

            val ref = FirebaseDatabase.getInstance().getReference("/timeStatus/${StatusOrderActivity.order?.id}/checking")
            ref.setValue(time.hours.toString() + ":" + time.minutes.toString())
        }



        viewHolder.itemView.textView_time_checking_header_status.text =
                time.hours.toString() + ":" + time.minutes.toString()
        viewHolder.itemView.textView_status_header_status_order.text = status


        if (status != "Finding driver") {
            viewHolder.itemView.textview_notification_header_status.text = ""
            viewHolder.itemView.btn_cancel_order_header_status_order.isVisible = false
            viewHolder.itemView.textView_status_header_status_order.text = status
            Handler(Looper.getMainLooper()).postDelayed(object : Runnable {


                override fun run() {

                    viewHolder.itemView.textView_status_header_status_order.text = "Preparing"
                    time = Calendar.getInstance().getTime();
                    val ref = FirebaseDatabase.getInstance().getReference("/timeStatus/${StatusOrderActivity.order?.id}/preparing")
                    ref.setValue(time.hours.toString() + ":" + time.minutes.toString())
                    viewHolder.itemView.textView_time_preparing_header.text =
                            time.hours.toString() + ":" + time.minutes.toString()
                    viewHolder.itemView.textView_preparing_head.setTextColor(Color.BLACK)
                    viewHolder.itemView.textView_preparing_head.setTextSize(18F)
                    Handler(Looper.getMainLooper()).postDelayed(object : Runnable {

                        override fun run() {
                            viewHolder.itemView.textView_status_header_status_order.text = "Delivering"
                            time = Calendar.getInstance().getTime();
                            val ref = FirebaseDatabase.getInstance().getReference("/timeStatus/${StatusOrderActivity.order?.id}/delivering")
                            ref.setValue(time.hours.toString() + ":" + time.minutes.toString())
                            viewHolder.itemView.textView__time_delivering_header.text =
                                    time.hours.toString() + ":" + time.minutes.toString()
                            viewHolder.itemView.textView_delivering_head.setTextColor(Color.BLACK)
                            viewHolder.itemView.textView_delivering_head.setTextSize(18F)
                            Handler(Looper.getMainLooper()).postDelayed(object : Runnable {

                                override fun run() {
                                    viewHolder.itemView.textView_status_header_status_order.text = "Arrived"
                                    time = Calendar.getInstance().getTime();
                                    val ref = FirebaseDatabase.getInstance().getReference("/timeStatus/${StatusOrderActivity.order?.id}/arrived")
                                    ref.setValue(time.hours.toString() + ":" + time.minutes.toString())
                                    viewHolder.itemView.textView_time_arived_header.text =
                                            time.hours.toString() + ":" + time.minutes.toString()
                                    viewHolder.itemView.textView_arrived_head.setTextColor(Color.BLACK)
                                    viewHolder.itemView.textView_arrived_head.setTextSize(18F)
                                    Handler(Looper.getMainLooper()).postDelayed(object : Runnable {
                                        override fun run() {

                                            val ref = FirebaseDatabase.getInstance().getReference("/delivers/${StatusOrderActivity.infoShipper?.uid}")
                                            ref.removeValue()
                                            val order = StatusOrderActivity.order
                                            val refOrder = FirebaseDatabase.getInstance().getReference("/orders/${FirebaseAuth.getInstance().uid}/${order?.id}/status")
                                            refOrder.setValue("Delivered")


                                            val intent = Intent(viewHolder.itemView.context, EvaluateActivity::class.java)
                                            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                                            viewHolder.itemView.context.startActivity(intent)
                                        }

                                    }, 2000)
                                }

                            }, 2000)
                        }

                    }, 2000)

                }

            }, 2000)
        }


    }

    override fun getLayout(): Int {
        return R.layout.header_status_order
    }

}