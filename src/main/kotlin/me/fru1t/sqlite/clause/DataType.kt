package me.fru1t.sqlite.clause

import me.fru1t.sqlite.Clause

/** The type of data to be stored in a column. See enum values for specifics. */
enum class DataType(private val value: String): Clause {
  /** A signed integer (eg. 1, 2, 3, 4) stored in 1, 2, 3, 4, 6, or 8 bytes. */
  INTEGER("INTEGER"),

  /** A text string encoded using the database encoding (UTF-8, UTF-16, etc). */
  TEXT("TEXT"),

  /** A blob of data, could be raw, could be anything really. Stored as-is. */
  BLOB("BLOB"),

  /** A floating point number (eg. 1.52, 4.23) stored as an 8-byte IEEE floating point number. */
  REAL("REAL"),

  /** A fixed decimal-point field. */
  NUMERIC("NUMERIC");

  /** Example: `TEXT`. */
  override fun getClause(): String = value
}
