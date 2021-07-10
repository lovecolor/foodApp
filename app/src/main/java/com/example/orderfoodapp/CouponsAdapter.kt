package com.example.orderfoodapp

import android.graphics.Color
import android.graphics.Paint
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import com.asksira.loopingviewpager.LoopingPagerAdapter
import com.example.orderfoodapp.models.Coupon
import kotlinx.android.synthetic.main.coupon_item.view.*

class CouponsAdapter(private val coupons: MutableList<Coupon>) : LoopingPagerAdapter<Coupon>(coupons, true) {

    override fun bindView(convertView: View, listPosition: Int, viewType: Int) {
        fun err(mess: String) {
            CartActivity.backgroundErrConstraint?.isVisible = true
            CartActivity.containerMessConstraintLayout?.isVisible = true
            CartActivity.textViewMess?.text = mess + " Please check the program rules for better understanding!"
        }

        val currentCoupon = CartActivity.currentCoupon
        val coupon = coupons[listPosition]
        fun choose(isNotifi: Boolean = true): Boolean {
            if ((coupon.typePay == "All" || coupon.typePay == CartActivity.typePayTextView?.text) && CartActivity.total >= coupon.minPrice) {

                CartActivity.currentCoupon = coupon


                var discount = 0
                if (coupon.sale == 0) {
                    discount = coupon.maxSale
                } else {
                    val currentDiscount = CartActivity.total * coupon.sale / 100
                    if (currentDiscount > coupon.maxSale) {
                        discount = coupon.maxSale
                    } else
                        discount = currentDiscount

                }
                if (discount > CartActivity.total) discount = CartActivity.total
                var totalDiscounted = CartActivity.total - discount

                CartActivity.totalTextView?.text = (CartActivity.total + CartActivity.deliverCharge).toString() + "đ"
                CartActivity.totalDiscountedTextView?.text = (totalDiscounted + CartActivity.deliverCharge).toString() + "đ"
                CartActivity.totalTextView?.paintFlags = Paint.STRIKE_THRU_TEXT_FLAG
                CartActivity.textViewLabelCoupon?.isVisible = true
                CartActivity.textViewDiscount?.text = "-" + discount.toString() + "đ"
                CartActivity.textViewDiscount?.isVisible = true
                CartActivity.btnAddCoupon?.setText(coupon.code)
                CartActivity.btnRemoveCoupon?.isVisible = true
                convertView.card_view_coupon_item.setBackgroundResource(R.drawable.rounded_btn_choose_coupon)
                return true;
            } else {

                if (coupon.typePay != CartActivity.typePayTextView?.text && coupon.typePay != "All") {

                    err("The discount code is not applicable for this payment method.")
                } else {
                    err("Order value does not meet the conditions of the program.")
                }
                if (!isNotifi)  CartActivity.currentCoupon = null
                return false

            }

        }

        fun cancel(isNotifi: Boolean = true) {
            CartActivity.btnAddCoupon?.setText("add a promo")
            CartActivity.textViewLabelCoupon?.isVisible = false

            CartActivity.textViewDiscount?.isVisible = false

            CartActivity.currentCoupon = null
if(isNotifi)
            this.notifyDataSetChanged()
            CartActivity.totalTextView?.apply {
                paintFlags = paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
            }

            CartActivity.totalTextView?.text = "Total"
            CartActivity.totalDiscountedTextView?.text = (CartActivity.total + CartActivity.deliverCharge).toString() + "đ"
            CartActivity.btnRemoveCoupon?.isVisible=false
        }

        if (CartActivity.currentCoupon?.code === coupon.code) {

             choose(false)




        }
        if(currentCoupon==null){
            cancel(false)
        }



        convertView.textView_code_coupon_item.text = coupon.code.toUpperCase()
        convertView.textView_name_coupon_item.text = coupon.title
        convertView.textView_hsd_coupon_item.text = coupon.hsd
        convertView.textView_choose_coupon_item.setOnClickListener {
            val type = convertView.textView_choose_coupon_item.text
            if (type == "Choose") {
                choose()


            } else {

                cancel()

            }


        }

    }

    override fun inflateView(viewType: Int, container: ViewGroup, listPosition: Int): View {
        return LayoutInflater.from(container.context).inflate(R.layout.coupon_item, container, false)
    }
}