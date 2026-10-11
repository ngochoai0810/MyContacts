package com.example.contacts

import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog

class ContactDetailActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        screen(R.layout.activity_detail)
        findViewById<View>(R.id.back).setOnClickListener { finish() }
    }

    override fun onResume() {
        super.onResume()
        val contact = ContactStore.get(this, intent.getStringExtra("id"))
            ?: run { finish(); return }
        findViewById<TextView>(R.id.name).text = contact.name
        findViewById<TextView>(R.id.avatar).apply {
            text = contact.name.trim().split(Regex("\\s+"))
                .lastOrNull()?.take(1)?.uppercase()?.ifEmpty { "?" } ?: "?"
            val colors = arrayOf("#DCE8FF", "#E5F2E9", "#FCE8DF", "#EDE5FA")
            background = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setColor(Color.parseColor(colors[(contact.id.hashCode() and Int.MAX_VALUE) % colors.size]))
            }
        }
        findViewById<TextView>(R.id.phone).text = contact.phone
        findViewById<TextView>(R.id.email).text = contact.email.ifBlank { "Chưa có email" }
        findViewById<View>(R.id.edit).setOnClickListener {
            startActivity(Intent(this, EditContactActivity::class.java).putExtra("id", contact.id))
        }
        findViewById<View>(R.id.call).setOnClickListener { open(Intent.ACTION_DIAL, "tel", contact.phone) }
        findViewById<View>(R.id.sms).setOnClickListener { open(Intent.ACTION_SENDTO, "smsto", contact.phone) }
        findViewById<View>(R.id.share).setOnClickListener { shareContact(contact) }
        findViewById<View>(R.id.delete).setOnClickListener { confirmDelete(contact) }
    }

    private fun confirmDelete(contact: Contact) {
        val dialog = AlertDialog.Builder(this)
            .setTitle("Xóa liên hệ?")
            .setMessage("Bạn muốn xóa liên hệ ${contact.name}? Thao tác này không thể hoàn tác.")
            .setNegativeButton("Hủy", null)
            .setPositiveButton("Xóa") { _, _ ->
                ContactStore.delete(this, contact.id)
                Toast.makeText(this, "Đã xóa liên hệ", Toast.LENGTH_SHORT).show()
                finish()
            }
            .create()
        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.parseColor("#B3261E"))
        }
        dialog.show()
    }

    private fun shareContact(contact: Contact) {
        val text = buildString {
            append("Họ và tên: ${contact.name}\nSố điện thoại: ${contact.phone}")
            if (contact.email.isNotBlank()) append("\nEmail: ${contact.email}")
        }
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
        }
        try {
            startActivity(Intent.createChooser(shareIntent, "Chia sẻ liên hệ"))
        } catch (_: ActivityNotFoundException) {
            Toast.makeText(this, "Thiết bị chưa có ứng dụng phù hợp", Toast.LENGTH_SHORT).show()
        }
    }

    private fun open(action: String, scheme: String, phone: String) {
        val cleanPhone = phone.replace(Regex("[^0-9+]"), "")
        try {
            startActivity(Intent(action, Uri.fromParts(scheme, cleanPhone, null)))
        } catch (_: ActivityNotFoundException) {
            Toast.makeText(this, "Thiết bị chưa có ứng dụng phù hợp", Toast.LENGTH_SHORT).show()
        }
    }
}
