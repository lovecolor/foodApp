package com.example.orderfoodapp

import android.content.Context
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
import com.example.orderfoodapp.models.CartMeal

import com.example.orderfoodapp.models.Order
import com.example.orderfoodapp.models.Restaurant
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
    var infoShipper: User? = null
    var order: Order? = null
    var context:Context?=null
    companion object {
        val USER_KEY = "USER_KEY"
        val adapter = GroupAdapter<GroupieViewHolder>()

        var isCancel: Boolean = false
        var timeMap = HashMap<String, String>()
        var listStatus: MutableList<String> = mutableListOf()


    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_status_order)
        supportActionBar?.title = "Status Order"

        listStatus.add("preparing")
        listStatus.add("delivering")
        listStatus.add("arrived")
        context=this
        isCancel = false
        order = intent.getParcelableExtra(CartActivity.ORDER_KEY)
        val refOrder = FirebaseDatabase.getInstance().getReference("/orders/${FirebaseAuth.getInstance().uid}/${order?.id}")
        refOrder.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {

            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                val status:String = snapshot.getValue() as String
                if(status=="Delivered")
                {
                    val ref=FirebaseDatabase.getInstance().getReference("/restaurants/${order?.restaurantId}")
                    ref.addListenerForSingleValueEvent(object :ValueEventListener{
                        override fun onDataChange(snapshot: DataSnapshot) {
                            val restaurant=snapshot.getValue(Restaurant::class.java)
                            val intent=Intent(context,EvaluateActivity::class.java)
                            intent.flags =
                                Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                            intent.putExtra(USER_KEY, infoShipper)
                            intent.putExtra(RestaurantsActivity.RESTAURANT_KEY,
                                restaurant
                            )
                            startActivity(intent)
                        }

                        override fun onCancelled(error: DatabaseError) {

                        }

                    })


                }
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {

            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {

            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
        fetchTimeStatus()

        verifyOrderIsPending()
        setUpAdapter()


    }
    val n: Long = 5000
    fun changeStatus(i: Int, order: Order) {

        Handler(Looper.getMainLooper()).postDelayed(object : Runnable {
            override fun run() {
                if (i >= listStatus.size) {

                    val ref =
                        FirebaseDatabase.getInstance().getReference("/delivers/${infoShipper?.uid}")
                    ref.removeValue()

                    val refOrder = FirebaseDatabase.getInstance()
                        .getReference("/orders/${FirebaseAuth.getInstance().uid}/${order?.id}/status")
                    refOrder.setValue("Delivered")




                } else {

                    val ref = FirebaseDatabase.getInstance()
                        .getReference("/timeStatus/${order?.id}/${listStatus[i]}")
                    val date = Date()
                    ref.setValue(date.hours.toString() + ":" + date.minutes.toString())
                    changeStatus(i + 1, order)
                }

            }

        }, n)
    }
    var isFindSuccess: Boolean = false
    fun findDriver() {
        if (isCancel) return

        val ref = FirebaseDatabase.getInstance().getReference("/users")


        ref.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val driverId = snapshot.key
                val refDeliver =
                    FirebaseDatabase.getInstance().getReference("/delivers/${driverId}")
                refDeliver.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {

                        if (snapshot.children.count() == 0 && !isFindSuccess && driverId != FirebaseAuth.getInstance().uid) {

                            refDeliver.setValue(order)
                            isFindSuccess = true
                            val refShipper = FirebaseDatabase.getInstance()
                                .getReference("/users/${snapshot.key}")
                            refShipper.addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(snapshot: DataSnapshot) {

                                    val user = snapshot.getValue(User::class.java)
                                    infoShipper = user
                                    adapter.clear()
                                    adapter.add(
                                        HeaderStatusItem(
                                            "Find success",
                                            order!!,
                                            infoShipper!!
                                        )
                                    )
                                    adapter.add(ShipperItem(user!!))
                                    changeStatus(0,order!!)


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

    private fun verifyOrderIsPending() {
        infoShipper = null
        val ref = FirebaseDatabase.getInstance().getReference("/delivers")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                snapshot.children.forEach {
                    val orderOfCurrentDriver = it.getValue(Order::class.java)
                    if (orderOfCurrentDriver?.id == order?.id) {
                        val ref = FirebaseDatabase.getInstance().getReference("/users/${it.key}")
                        ref.addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                infoShipper = snapshot.getValue(User::class.java)
                                adapter.clear()
                                adapter.add(
                                    HeaderStatusItem(
                                        "Find success",
                                        order!!,
                                        infoShipper!!
                                    )
                                )
                                adapter.add(ShipperItem(infoShipper!!))
                            }

                            override fun onCancelled(error: DatabaseError) {

                            }

                        })
                        return
                    }
                }
                if (infoShipper == null) {
                    val ref = FirebaseDatabase.getInstance()
                        .getReference("/timeStatus/${order?.id}/checking")
                    val date = Date()
                    ref.setValue(date.hours.toString() + ":" + date.minutes.toString())
                    Handler(Looper.getMainLooper()).postDelayed(object : Runnable {
                        override fun run() {
                            findDriver()
                        }

                    }, 2000)
                }

            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }

    private fun fetchTimeStatus() {
        timeMap.clear()
        val ref = FirebaseDatabase.getInstance().getReference("/timeStatus/${order?.id}")
        ref.addChildEventListener(object : ChildEventListener {


            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val timeStatus = snapshot.getValue() as String
                timeMap[snapshot.key!!] = timeStatus
                adapter.notifyItemChanged(0)


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
        adapter.clear()
        adapter.add(HeaderStatusItem("Finding driver", order!!))
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

class HeaderStatusItem(val status: String, val order: Order? = null, val user: User? = null) :
    Item<GroupieViewHolder>() {

    override fun bind(viewHolder: GroupieViewHolder, position: Int) {



        val itemView = viewHolder.itemView
        var textViewMap = HashMap<String, TextView>()
        textViewMap["arrived"] = itemView.textView_arrived_head
        textViewMap["checking"] = itemView.textView_checking_header_status_order
        textViewMap["delivering"] = itemView.textView_delivering_head
        textViewMap["preparing"] = itemView.textView_preparing_head
        var textViewTimeMap = HashMap<String, TextView>()
        textViewTimeMap["arrived"] = itemView.textView_time_arived_header
        textViewTimeMap["delivering"] = itemView.textView__time_delivering_header
        textViewTimeMap["preparing"] = itemView.textView_time_preparing_header
        textViewTimeMap["checking"] = itemView.textView_time_checking_header_status
        fun setTime(key: String, value: String) {
            textViewTimeMap[key]?.setText(value)
        }

        fun handleText(key: String) {
            textViewMap[key]?.setTextColor(Color.BLACK)
            textViewMap[key]?.setTextSize(18F)

        }


        StatusOrderActivity.timeMap.forEach {
            handleText(it.key!!)
            setTime(it.key!!, it.value)
        }
        viewHolder.itemView.textView_status_header_status_order.text = status
        val sizeTimeMap = StatusOrderActivity.timeMap.size
        var listStatus=StatusOrderActivity.listStatus
        if (sizeTimeMap > 1) {
            val textStatus = listStatus[sizeTimeMap - 2]


            itemView.textView_status_header_status_order.text =
                textStatus.get(0).toUpperCase() + textStatus.substring(1)
        }


        //event_btn-cancel
        viewHolder.itemView.btn_cancel_order_header_status_order.setOnClickListener {


            StatusOrderActivity.isCancel = true

            val ref = FirebaseDatabase.getInstance()
                .getReference("/orders/${FirebaseAuth.getInstance().uid}/${order?.id}/status")

            ref.setValue("Cancelled")


            val intent = Intent(viewHolder.itemView.context, RestaurantsActivity::class.java)
            viewHolder.itemView.context.startActivity(intent)

        }

        //btn-close
        viewHolder.itemView.imageButton_close_head_status.setOnClickListener {
            val intent = Intent(viewHolder.itemView.context, RestaurantsActivity::class.java)
            viewHolder.itemView.context.startActivity(intent)

        }



        if (status == "Find success") {
            viewHolder.itemView.textview_notification_header_status.text = ""
            viewHolder.itemView.btn_cancel_order_header_status_order.isVisible = false
//            if (StatusOrderActivity.timeMap.size <= 1) changeStatus(0, order!!)


        }


    }

    override fun getLayout(): Int {
        return R.layout.header_status_order
    }

}