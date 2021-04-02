package ro.code4.deurgenta.helper

import android.annotation.SuppressLint
import android.view.View
import android.widget.ImageView
import androidx.databinding.BindingAdapter

object BindingAdapters {

    @SuppressLint("ResourceType")
    @JvmStatic
    @BindingAdapter(value = ["drawableImage"], requireAll = false)
    fun bindingDrawableImage(imageView: ImageView, resId: Int) {
        imageView.setImageResource(resId)
    }

    @JvmStatic
    @BindingAdapter("visibleGone")
    fun showHide(view: View, show: Boolean) {
        view.visibility = if (show) View.VISIBLE else View.GONE
    }
}