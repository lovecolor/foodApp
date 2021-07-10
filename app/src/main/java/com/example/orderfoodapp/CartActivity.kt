package com.example.orderfoodapp

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import com.example.orderfoodapp.models.CartMeal
import com.example.orderfoodapp.models.Coupon
import com.example.orderfoodapp.models.Order
import com.example.orderfoodapp.models.Restaurant
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.activity_cart.*
import kotlinx.android.synthetic.main.address_layout_cart.*
import kotlinx.android.synthetic.main.cart_row.view.*
import kotlinx.android.synthetic.main.coupon_view_pager.view.*
import kotlinx.android.synthetic.main.footer_cart.*
import kotlinx.android.synthetic.main.footer_cart.view.*
import org.w3c.dom.Text
import java.text.SimpleDateFormat
import java.util.*

class CartActivity : AppCompatActivity() {
    var textTypePay: String = "Cash"
    var icTypePay: Int = R.drawable.ic_cash

    companion object {

        val TEXT_TYPE_PAY = "TEXT_TYPE_PAY"
        val IC_TYPE_PAY = "IC_TYPE_PAY"
        val ORDER_KEY = "ORDER_KEY"
        var listMeal: MutableList<CartMeal> = mutableListOf()
        var adapter = GroupAdapter<GroupieViewHolder>()
        var restaurant: Restaurant? = null
        var recyclerView: RecyclerView? = null
        var textView_total: TextView? = null
        var total: Int = 0
        var qty: Int = 0
        var distance: Double = 0.0
        var currentCoupon:Coupon?=null
var totalTextView:TextView?=null
        var totalDiscountedTextView:TextView?=null
        var typePayTextView:TextView?=null
        var deliverCharge:Int=0
        var backgroundErrConstraint:ConstraintLayout?=null
        var containerMessConstraintLayout:ConstraintLayout?=null
        var textViewMess:TextView?=null
        var textViewLabelCoupon:TextView?=null
        var textViewDiscount:TextView?=null
        var btnAddCoupon:Button?=null
        var btnRemoveCoupon:ImageButton?=null
        var adapterCoupon:CouponsAdapter?=null

    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cart)
        supportActionBar?.title = "Your Cart"

        btnRemoveCoupon=imageButton_remove_coupon_cart
        btnAddCoupon=button_add_promo_cart
        backgroundErrConstraint=background_err_cart
        containerMessConstraintLayout=container_mess_cart
        textViewMess=textView_mess_cart
        typePayTextView=textView_type_pay_cart
        totalDiscountedTextView=textView_total_cart
        totalTextView=textView_label_total_cart
        val text = intent.getStringExtra(TEXT_TYPE_PAY)
        val ic: Int = intent.getIntExtra(IC_TYPE_PAY, 0)
        if (text != null) textTypePay = text
        if (ic != 0) icTypePay = ic

        recyclerView = recycler_cart
        textView_total = textView_total_cart
        adapter.clear()

        setUpEvent()
        loadData()

        recycler_cart.adapter = adapter
        recycler_cart.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
        textView_order_cart.setOnClickListener addOnSuccessListener@{
            if (editText_address_cart.text.trim().length == 0) {
                Toast.makeText(this, "You need enter the address!!!", Toast.LENGTH_SHORT).show()
                return@addOnSuccessListener
            }
            val uid = FirebaseAuth.getInstance().uid
            val ref = FirebaseDatabase.getInstance().getReference("/orders/$uid").push()
            val time = Date()


            val sdf = SimpleDateFormat("dd, MM, yyyy")
            val currentDate = sdf.format(Date())


            val order = Order(
                    ref.key!!,
                    FirebaseAuth.getInstance().uid!!,
                    restaurant?.id!!,
                    currentDate,
                    "${time.hours}:${time.minutes}",
                    total + 15000,
                    qty,
                    textView_type_pay_cart.text.toString(), "Preparing",
                    editText_address_cart.text.toString(),
                    distance
            )

            ref.setValue(order).addOnSuccessListener {


                listMeal.forEach {
                    val refOrderDetails =
                            FirebaseDatabase.getInstance().getReference("/orderDetails/${order.id}")
                                    .push()
                    val meal = it
                    refOrderDetails.setValue(meal)

                }
                total = 0
                qty = 0
                listMeal.clear()


            }.addOnFailureListener {
                Log.d("Order", it.message.toString())
            }

            val intent = Intent(this, StatusOrderActivity::class.java)
            intent.putExtra(ORDER_KEY, order)
            startActivity(intent)
        }
    }

    private fun setUpEvent() {
        imageButton_remove_coupon_cart.setOnClickListener {
            currentCoupon=null
            adapterCoupon?.notifyDataSetChanged()

        }
        textView_ok_cart.setOnClickListener {
            background_err_cart.isVisible=false
            container_mess_cart.isVisible=false

        }
        textView_type_pay_cart.text = textTypePay
        textView_type_pay_cart.setCompoundDrawablesWithIntrinsicBounds(icTypePay, 0, R.drawable.ic_baseline_keyboard_arrow_up_24, 0)

        textView_type_pay_cart.setOnClickListener {
            val intent = Intent(this, TypePayActivity::class.java)
            intent.putExtra(TEXT_TYPE_PAY, textTypePay)
            startActivity(intent)
        }
    }

    fun loadData() {


        adapter.add(AddressItem())

        listMeal.forEach {
            adapter.add(CartRowItem(it, true))

        }

        textView_total_cart.text = (total + 15000).toString() + "đ"

        distance = Math.ceil(Math.random() * 50) / 10
        adapter.add(FooterCartItem(qty, total, distance))
        adapter.add(CouponViewPageItem())

    }

    class CartRowItem(val cartMeal: CartMeal, val isHaveBtnCancel: Boolean) :
            Item<GroupieViewHolder>() {
        override fun bind(viewHolder: GroupieViewHolder, position: Int) {
            viewHolder.itemView.textView_qty_cart_row.text = cartMeal.qty.toString() + "x"
            viewHolder.itemView.textView_name_meal_cart.text = cartMeal.name
            viewHolder.itemView.textView_price_meal_cart.text =
                    (cartMeal.price * cartMeal.qty).toString() + "đ"
            val note = cartMeal.note
            if (note != "") {
                viewHolder.itemView.textView_note_cart_row.isVisible = true
                viewHolder.itemView.textView_note_cart_row.text = cartMeal.note
            } else {
                viewHolder.itemView.textView_note_cart_row.isVisible = false
            }

            if (isHaveBtnCancel) {
                viewHolder.itemView.imageButton_remove_meal_cart.setOnClickListener {


                    listMeal.removeAt(position - 1)
                    qty -= cartMeal.qty
                    total -= (cartMeal.qty * cartMeal.price)
                    if (listMeal.size == 0) {
                        val intent = Intent(viewHolder.itemView.context, MenuActivity::class.java)
                        intent.putExtra(RestaurantsActivity.RESTAURANT_KEY, MenuActivity.restaurant)


                        MenuActivity.btn_cart?.isVisible = false
                        viewHolder.itemView.context.startActivity(intent)

                    }

                    adapter.clear()
                    adapter.add(AddressItem())
                    listMeal.forEach {
                        adapter.add(CartRowItem(it, true))
                    }


                    adapter.add(FooterCartItem(qty, total, distance))
                    adapter.add(CouponViewPageItem())

                    recyclerView?.adapter = adapter
                    textView_total?.text = (total + deliverCharge).toString() + "đ"
                    MenuActivity.textView_qty?.text = qty.toString() + " Meals"
                    MenuActivity.textView_total?.text = total.toString() + "đ"


                }
            } else {
                viewHolder.itemView.imageButton_remove_meal_cart.isVisible = false
            }

        }

        override fun getLayout(): Int {
            return R.layout.cart_row
        }


    }

}

class AddressItem : Item<GroupieViewHolder>() {
    override fun bind(viewHolder: GroupieViewHolder, position: Int) {

    }

    override fun getLayout(): Int {
        return R.layout.address_layout_cart
    }
}

class FooterCartItem(val qty: Int, val total: Int, val distance: Double) : Item<GroupieViewHolder>() {
    override fun bind(viewHolder: GroupieViewHolder, position: Int) {

        val itemView = viewHolder.itemView
        CartActivity.textViewLabelCoupon=itemView.textView_label_coupon_foot_cart
        CartActivity.textViewDiscount=itemView.textView_discount_foot_cart
        itemView.textView_total_qty_footer_order_detail.text =
                "(" + qty.toString() + " meals)"
        itemView.textView_temporary_expense_foot_order_detail.text =
                total.toString() + "đ"
        itemView.textView_distance_foot_cart.text = distance.toString()+"km"
        var deliverCharge=0
        if(distance<=3)
        {
            deliverCharge=15000
        }
        else{
            deliverCharge= (((distance -3)*5000)+15000).toInt()
        }
        itemView.textView_delivery_charges_foot_cart.text=deliverCharge.toString()+"đ"
        CartActivity.deliverCharge=deliverCharge
    }

    override fun getLayout(): Int {
        return R.layout.footer_cart
    }
}

class CouponViewPageItem() : Item<GroupieViewHolder>() {

    override fun bind(viewHolder: GroupieViewHolder, position: Int) {

        val ref = FirebaseDatabase.getInstance().getReference("/coupons")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val coupons = mutableListOf<Coupon>()
                snapshot.children.forEach {
                    val coupon = it.getValue(Coupon::class.java)
                    coupons.add(coupon!!)
                }
                val adapter = CouponsAdapter(coupons)
                CartActivity.adapterCoupon=adapter
                viewHolder.itemView.viewpager_coupon_cart.adapter = adapter
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })

    }

    override fun getLayout(): Int {
        return R.layout.coupon_view_pager
    }

}