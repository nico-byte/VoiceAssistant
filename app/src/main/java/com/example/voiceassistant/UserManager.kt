package com.example.voiceassistant

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

data class User(val username: String, val password: String, val apiKey: String)

class UserManager(private val context: Context, private val gson: Gson) {
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("user_preferences", Context.MODE_PRIVATE)

    fun saveUser(user: User) {
        val users = loadUsers().toMutableList()
        users.add(user)
        sharedPreferences.edit().putString("users", gson.toJson(users)).apply()
    }

    fun loadUsers(): List<User> {
        val json = sharedPreferences.getString("users", null) ?: return emptyList()
        val type = object : TypeToken<List<User>>() {}.type
        return gson.fromJson(json, type)
    }


    fun updateUser(user: User) {
        val users = loadUsers().toMutableList()
        val index = users.indexOfFirst { it.username == user.username }

        if (index != -1) {
            users[index] = user
            sharedPreferences.edit().putString("users", gson.toJson(users)).apply()
        }
    }

    fun deleteUser(user: User) {
        val users = loadUsers().toMutableList()
        users.remove(user)

        sharedPreferences.edit().putString("users", gson.toJson(users)).apply()
    }
}
