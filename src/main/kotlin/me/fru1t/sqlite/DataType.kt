package me.fru1t.sqlite

/** The type of data to be stored in a column. For example: integers, text, raw blobs, etc. */
enum class DataType(val value: String) {
  /** A signed integer (eg. 1, 2, 3, 4) stored in 1, 2, 3, 4, 6, or 8 bytes. */
  INTEGER("INTEGER"),

  /** A text string encoded using the database encoding (UTF-8, UTF-16, etc). */
  TEXT("TEXT"),

  /** A blob of data, could be raw, could be anything really. Stored as-is. */
  BLOB("BLOB"),

  /** A floating point number (eg. 1.52, 4.23) stored as an 8-byte IEEE floating point number. */
  REAL("REAL"),

  /** Everything else. Usually a fixed decimal point field, but that's currently unsupported. */
  NUMERIC("NUMERIC")
}
