package com.example.contacts

import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import java.util.UUID

class EditContactActivity : BaseActivity() {
    private lateinit var contactId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        screen(R.layout.activity_edit)
        val existing = ContactStore.get(this, intent.getStringExtra("id"))
        contactId = savedInstanceState?.getString("contactId") ?: existing?.id ?: UUID.randomUUID().toString()

        val name = findViewById<EditText>(R.id.name)
        val phone = findViewById<EditText>(R.id.phone)
        val email = findViewById<EditText>(R.id.email)

        findViewById<TextView>(R.id.title).text = if (existing == null) "Thêm liên hệ" else "Sửa liên hệ"

        if (savedInstanceState == null && existing != null) {
            name.setText(existing.name)
            phone.setText(existing.phone)
            email.setText(existing.email)
        }

        findViewById<View>(R.id.back).setOnClickListener { finish() }
        findViewById<View>(R.id.save).setOnClickListener {
            val n = name.text.toString().trim()
            val p = phone.text.toString().trim()
            val e = email.text.toString().trim()
            when {
                n.isEmpty() -> { name.error = "Nhập họ và tên"; name.requestFocus() }
                !Regex("^\\+?[0-9 ()-]{5,25}$").matches(p) || p.count { it.isDigit() } < 5 -> {
                    phone.error = "Nhập số điện thoại hợp lệ"; phone.requestFocus()
                }
                e.isNotEmpty() && !Patterns.EMAIL_ADDRESS.matcher(e).matches() -> {
                    email.error = "Email chưa hợp lệ"; email.requestFocus()
                }
                else -> {
                    ContactStore.save(this, Contact(contactId, n, p, e))
                    Toast.makeText(
                        this,
                        if (existing == null) "Đã thêm liên hệ" else "Đã cập nhật liên hệ",
                        Toast.LENGTH_SHORT
                    ).show()
                    finish()
                }
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString("contactId", contactId)
        super.onSaveInstanceState(outState)
    }
}
