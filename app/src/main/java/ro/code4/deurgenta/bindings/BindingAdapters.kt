package ro.code4.deurgenta.bindings

import android.R
import android.annotation.SuppressLint
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide


object BindingAdapters {

    @JvmStatic
    @BindingAdapter("visibleGone")
    fun visible(view: View, show: Boolean) {
        view.visibility = if (show) View.VISIBLE else View.GONE
    }

    @SuppressLint("ResourceType")
    @JvmStatic
    @BindingAdapter(value = ["imageRes"], requireAll = false)
    fun bindImageResource(imageView: ImageView, resId: Int?) {
        if (resId != null) {
            Glide.with(imageView.context)
                .load(resId)
                .into(imageView)
        }
    }

    @SuppressLint("ResourceType")
    @JvmStatic
    @BindingAdapter(value = ["enabled"], requireAll = false)
    fun bindEnabled(layout: ViewGroup, flag: Boolean) {
        for (i in 0 until layout.childCount) {
            val child = layout.getChildAt(i)
            child.isEnabled = flag
        }
    }
}