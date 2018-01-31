package me.fru1t.sqlite.annotation

/**
 * Denotes a column within a table. This annotation may only be attached to a data class that
 * extends the [me.fru1t.sqlite.Table] class.
 *
 * Example usage:
 * ```
 * data class ExampleTable(
 *     @Column(INTEGER) val id: Int,
 *     @Column(TEXT) val username: String,
 *     @Column(dataType = TEXT, nullable = true) val content: String
 * ) : extends Table<ExampleTable>
 * ```
 *
 * @param dataType The [DataType] (aka affinity) this column will be stored as on the database
 * @param nullable Whether or not this column can be null. See [nullable].
 * @param default The default value this column will take on if [nullable] is set to false
 * @param autoIncrement Whether or not this column should hold its own incrementation value, note
 * that this behavior is different in SQLite than it is in other SQL implementation. DO NOT USE
 * THIS FIELD UNLESS YOU UNDERSTAND THE RISKS. See [autoIncrement].
 */
@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
annotation class Column(
    val dataType: DataType,
    /**
     * A `NOT NULL` constraint may only be attached to a column definition, not specified as a table
     * constraint. Not surprisingly, a `NOT NULL` constraint dictates that the associated column may
     * not contain a `NULL` value. Attempting to set the column value to `NULL` when inserting a
     * new row or updating an existing one causes a constraint violation.
     */
    val nullable: Boolean = false,
    val default: String = "",
    /**
     * Please read [the documentation][http://www.sqlite.org/autoinc.html] before using this field.
     * TL;DR: if you want an auto incremented `id` field, declare it a column as an integer and set
     * it as the sole `PRIMARY KEY` in the table. SQLite will magically take care of it.
     */
    val autoIncrement: Boolean = false)

/** The type of data to be stored in a column. For example: integers, text, raw blobs, etc. */
enum class DataType {
  /** A signed integer (eg. 1, 2, 3, 4) stored in 1, 2, 3, 4, 6, or 8 bytes. */
  INTEGER,
  /** A text string encoded using the database encoding (UTF-8, UTF-16, etc). */
  TEXT,
  /** A blob of data, could be raw, could be anything really. Stored as-is. */
  BLOB,
  /** A floating point number (eg. 1.52, 4.23) stored as an 8-byte IEEE floating point number. */
  REAL,
  /** Everything else. Usually a fixed decimal point field, but that's currently unsupported. */
  NUMERIC
}
