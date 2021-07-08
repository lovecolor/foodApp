package com.example.orderfoodapp

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import com.example.orderfoodapp.models.CartMeal
import com.example.orderfoodapp.models.Order
import com.example.orderfoodapp.models.Restaurant
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.activity_cart.*
import kotlinx.android.synthetic.main.address_layout_cart.*
import kotlinx.android.synthetic.main.cart_row.view.*
import kotlinx.android.synthetic.main.footer_cart.*
import kotlinx.android.synthetic.main.footer_cart.view.*
import java.text.SimpleDateFormat
import java.util.*

class CartActivity : AppCompatActivity() {
    var textTypePay:String="Cash"
    var icTypePay:Int=R.mipmap.ic_cash
    companion object {
        val TEXT_TYPE_PAY="TEXT_TYPE_PAY"
        val IC_TYPE_PAY="IC_TYPE_PAY"
        val ORDER_KEY = "ORDER_KEY"
        var listMeal: MutableList<CartMeal> = mutableListOf()
        var adapter = GroupAdapter<GroupieViewHolder>()
        var restaurant: Restaurant? = null
        var recyclerView: RecyclerView? = null
        var textView_total: TextView? = null
        var total: Int = 0
        var qty: Int = 0



    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cart)
        supportActionBar?.title = "Your Cart"

        val text=intent.getStringExtra(TEXT_TYPE_PAY)
        val ic:Int=intent.getIntExtra(IC_TYPE_PAY,0)
        if(text!=null) textTypePay=text
        if(ic!=0) icTypePay=ic

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
                    MenuActivity.restaurant?.id!!,
                    currentDate,
                    "${time.hours}:${time.minutes}",
                    total + 15000,
                    qty,
                    textView_type_pay_cart.text.toString(), "Preparing",
                    editText_address_cart.text.toString(),

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
        textView_type_pay_cart.text=textTypePay
        textView_type_pay_cart.setCompoundDrawablesWithIntrinsicBounds(icTypePay,0,R.drawable.ic_baseline_keyboard_arrow_up_24,0)

        textView_type_pay_cart.setOnClickListener {
            val intent = Intent(this, TypePayActivity::class.java)
            intent.putExtra(TEXT_TYPE_PAY,textTypePay)
            startActivity(intent)
        }
    }

    fun loadData() {


        adapter.add(AddressItem())

        listMeal.forEach {
            adapter.add(CartRowItem(it, true))

        }
        textView_total_cart.text = (total + 15000).toString() + "đ"

        adapter.add(FooterCartItem(qty, total))


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

                    adapter.add(FooterCartItem(qty, total))


                    recyclerView?.adapter = adapter
                    textView_total?.text = (total + 15000).toString() + "đ"
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

class FooterCartItem(val qty: Int, val total: Int) : Item<GroupieViewHolder>() {
    override fun bind(viewHolder: GroupieViewHolder, position: Int) {

        viewHolder.itemView.textView_total_qty_footer_order_detail.text =
                "(" + qty.toString() + " meals)"
        viewHolder.itemView.textView_temporary_expense_foot_order_detail.text =
                total.toString() + "đ"

    }

    override fun getLayout(): Int {
        return R.layout.footer_cart
    }
}