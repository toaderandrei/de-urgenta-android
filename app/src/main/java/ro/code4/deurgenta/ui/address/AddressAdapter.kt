package ro.code4.deurgenta.ui.address

import android.content.Context
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import ro.code4.deurgenta.R
import ro.code4.deurgenta.data.model.AddressType

class AddressAdapter(
    context: Context
) : ArrayAdapter<AddressType>(context, 0, AddressType.values()) {
    private val layoutInflater: LayoutInflater = LayoutInflater.from(context)
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view: View
        if (convertView == null) {
            view = layoutInflater.inflate(R.layout.item_address, parent, false)
        } else {
            view = convertView
        }
        getItem(position)?.let { country ->
            setItemForCountry(view, country)
        }
        return view
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view: View
        if (position == 0) {
            view = layoutInflater.inflate(R.layout.header_address, parent, false)
            view.setOnClickListener {
                val root = parent.rootView
                root.dispatchKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_BACK))
                root.dispatchKeyEvent(KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_BACK))
            }
        } else {
            view = layoutInflater.inflate(R.layout.item_address_dropdown, parent, false)
            getItem(position)?.let { country ->
                setItemForCountry(view, country)
            }
        }
        return view
    }

    override fun getItem(position: Int): AddressType? {
        if (position == 0) {
            return null
        }
        return super.getItem(position - 1)
    }

    override fun getCount() = super.getCount() + 1
    override fun isEnabled(position: Int) = position != 0

    private fun setItemForCountry(view: View, country: AddressType) {
        val tvCountry = view.findViewById<TextView>(R.id.tvCountry)
        val ivCountry = view.findViewById<ImageView>(R.id.ivCountry)
        //val countryName = Locale("", country.countryCode).displayCountry
        tvCountry.text = countryName
        ivCountry.setBackgroundResource(country.icon)
    }
}