//HỌ và tên: Huỳnh THị Ngọc Hoài
//MSSV: 123220152
//Lớp 123220152

package com.example.contacts

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.EditText
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog

class MainActivity : BaseActivity() {
    private var items = emptyList<Contact>()
    private lateinit var adapter: BaseAdapter
    private var selectedFilter = 0
    private val filterLabels = arrayOf("Tất cả", "Có email", "Chưa có email")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        screen(R.layout.activity_main)
        selectedFilter = (savedInstanceState?.getInt("selectedFilter") ?: 0)
            .coerceIn(filterLabels.indices)

        val list = findViewById<ListView>(R.id.list)
        list.emptyView = findViewById(R.id.empty)
        adapter = object : BaseAdapter() {
            override fun getCount() = items.size
            override fun getItem(position: Int) = items[position]
            override fun getItemId(position: Int) = position.toLong()
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val row = convertView ?: layoutInflater.inflate(R.layout.item_contact, parent, false)
                val contact = items[position]
                row.findViewById<TextView>(R.id.name).text = contact.name
                row.findViewById<TextView>(R.id.phone).text = contact.phone
                row.findViewById<TextView>(R.id.avatar).apply {
                    text = contact.name.trim().split(Regex("\\s+"))
                        .lastOrNull()?.take(1)?.uppercase()?.ifEmpty { "?" } ?: "?"
                    val colors = arrayOf("#DCE8FF", "#E5F2E9", "#FCE8DF", "#EDE5FA")
                    background = GradientDrawable().apply {
                        shape = GradientDrawable.OVAL
                        setColor(Color.parseColor(colors[(contact.id.hashCode() and Int.MAX_VALUE) % colors.size]))
                    }
                }
                return row
            }
        }
        list.adapter = adapter
        list.setOnItemClickListener { _, _, position, _ ->
            if (position in items.indices) {
                startActivity(Intent(this, ContactDetailActivity::class.java)
                    .putExtra("id", items[position].id))
            }
        }
        findViewById<View>(R.id.add).setOnClickListener {
            startActivity(Intent(this, EditContactActivity::class.java))
        }
        findViewById<View>(R.id.filter).setOnClickListener { showFilter() }
        findViewById<View>(R.id.filterLabel).setOnClickListener { showFilter() }
        findViewById<View>(R.id.clearSearch).setOnClickListener {
            findViewById<EditText>(R.id.search).text.clear()
        }
        findViewById<EditText>(R.id.search).addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) { refresh() }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    override fun onResume() {
        super.onResume()
        refresh()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putInt("selectedFilter", selectedFilter)
        super.onSaveInstanceState(outState)
    }

    private fun showFilter() {
        AlertDialog.Builder(this)
            .setTitle("Lọc liên hệ")
            .setSingleChoiceItems(filterLabels, selectedFilter) { dialog, which ->
                selectedFilter = which
                refresh()
                dialog.dismiss()
            }
            .setNegativeButton("Đóng", null)
            .show()
    }

    private fun refresh() {
        val searchText = findViewById<EditText>(R.id.search).text.toString()
        val query = ContactStore.normalize(searchText.trim())
        val phoneQuery = query.replace(Regex("[\\s()-]"), "")
        val allContacts = ContactStore.all(this)
        items = allContacts.filter { contact ->
            val matchesFilter = when (selectedFilter) {
                1 -> contact.email.isNotBlank()
                2 -> contact.email.isBlank()
                else -> true
            }
            val matchesSearch = query.isEmpty() ||
                ContactStore.normalize(contact.name).contains(query) ||
                (phoneQuery.isNotEmpty() &&
                    contact.phone.replace(Regex("[\\s()-]"), "").contains(phoneQuery)) ||
                (contact.email.isNotEmpty() && ContactStore.normalize(contact.email).contains(query))
            matchesFilter && matchesSearch
        }
        findViewById<View>(R.id.clearSearch).visibility =
            if (searchText.isEmpty()) View.GONE else View.VISIBLE
        findViewById<TextView>(R.id.filterLabel).text = filterLabels[selectedFilter]
        findViewById<View>(R.id.filter).contentDescription = "Lọc liên hệ: ${filterLabels[selectedFilter]}"
        findViewById<TextView>(R.id.count).text = "LIÊN HỆ · ${items.size}"
        findViewById<TextView>(R.id.empty).text = if (allContacts.isEmpty()) {
            "Chưa có liên hệ\nNhấn + để thêm liên hệ mới"
        } else {
            "Không tìm thấy liên hệ phù hợp\nThử đổi từ khóa hoặc bộ lọc"
        }
        adapter.notifyDataSetChanged()
    }
}
