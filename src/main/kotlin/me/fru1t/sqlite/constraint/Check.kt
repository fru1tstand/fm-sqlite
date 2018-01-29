package me.fru1t.sqlite.constraint

import me.fru1t.sqlite.Table

// Placeholder
data class Check<T : Table<T>>(val int: Int)
