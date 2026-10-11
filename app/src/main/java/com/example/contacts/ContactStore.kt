package com.example.contacts

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.text.Normalizer
import java.util.UUID

data class Contact(val id: String, val name: String, val phone: String, val email: String)

object ContactStore {
    private fun prefs(context: Context) = context.getSharedPreferences("contacts_demo", Context.MODE_PRIVATE)

    fun all(context: Context): List<Contact> {
        val raw = prefs(context).getString("items", null)
        if (raw.isNullOrBlank()) {
            val samples = listOf(
                Contact(UUID.randomUUID().toString(), "Nguyễn Minh Anh", "0901234567", "minhanh@example.com"),
                Contact(UUID.randomUUID().toString(), "Trần Gia Bảo", "0912345678", "giabao@example.com"),
                Contact(UUID.randomUUID().toString(), "Lê Ngọc Hà", "0923456789", ""),
                Contact(UUID.randomUUID().toString(), "Phạm Tuấn Kiệt", "0934567890", ""),
                Contact(UUID.randomUUID().toString(), "Đặng Khánh Linh", "0945678901", "linh@example.com"),
                Contact(UUID.randomUUID().toString(), "Võ Hoàng Nam", "0956789012", ""),
                Contact(UUID.randomUUID().toString(), "Bùi Thu Trang", "0967890123", "")
            )
            write(context, samples)
            return samples.sortedBy { normalize(it.name) }
        }
        return try {
            val array = JSONArray(raw)
            (0 until array.length()).map { index ->
                val item = array.getJSONObject(index)
                Contact(
                    item.optString("id"),
                    item.optString("name"),
                    item.optString("phone"),
                    if (item.isNull("email")) "" else item.optString("email", "")
                )
            }.sortedBy { normalize(it.name) }
        } catch (_: Exception) {
            prefs(context).edit().remove("items").apply()
            all(context)
        }
    }

    fun get(context: Context, id: String?) = all(context).find { it.id == id }

    fun save(context: Context, contact: Contact) {
        val items = all(context).toMutableList()
        val index = items.indexOfFirst { it.id == contact.id }
        if (index >= 0) items[index] = contact else items.add(contact)
        write(context, items)
    }

    fun delete(context: Context, id: String) {
        val items = all(context).filterNot { it.id == id }
        write(context, items)
    }

    private fun write(context: Context, contacts: List<Contact>) {
        val array = JSONArray()
        contacts.forEach { c ->
            array.put(JSONObject().put("id", c.id).put("name", c.name).put("phone", c.phone).put("email", c.email))
        }
        prefs(context).edit().putString("items", array.toString()).apply()
    }

    fun normalize(value: String): String = Normalizer.normalize(value.lowercase(java.util.Locale.ROOT), Normalizer.Form.NFD)
        .replace(Regex("\\p{M}+"), "").replace('đ', 'd').replace('Đ', 'd')
}