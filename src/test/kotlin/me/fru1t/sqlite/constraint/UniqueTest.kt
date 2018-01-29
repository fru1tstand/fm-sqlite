package me.fru1t.sqlite.constraint

import com.google.common.truth.Truth.assertThat
import me.fru1t.sqlite.Table
import me.fru1t.sqlite.annotation.Column
import me.fru1t.sqlite.annotation.DataType.INTEGER
import me.fru1t.sqlite.constraint.resolutionstrategy.OnConflict
import org.junit.jupiter.api.Test

class UniqueTest {
  @Test
  fun of() {
    val unique = Unique.of(OnConflict.REPLACE, ExampleTable::field, ExampleTable::field2)
    assertThat(unique.columns).asList().containsExactly(ExampleTable::field, ExampleTable::field2)
    assertThat(unique.onConflict).isEqualTo(OnConflict.REPLACE)
  }

  @Test
  fun getConstraintClause() {
    val unique = Unique(arrayOf(ExampleTable::field, ExampleTable::field2), OnConflict.ABORT)
    assertThat(unique.getConstraintClause())
        .isEqualTo("CONSTRAINT uq_field_field2 UNIQUE (`field`,`field2`) ON CONFLICT ABORT")
  }

  @Test
  fun getConstraintClause_noColumns() {
    val unique = Unique<ExampleTable>(emptyArray(), OnConflict.IGNORE)
    assertThat(unique.getConstraintClause()).isEmpty()
  }

  @Test
  fun getConstraintName() {
    val unique = Unique(arrayOf(ExampleTable::field, ExampleTable::field2), OnConflict.ABORT)
    assertThat(unique.getConstraintName()).isEqualTo("uq_field_field2")
  }

  @Test
  fun equals_sameInstance() {
    val unique = Unique(arrayOf(ExampleTable::field, ExampleTable::field2), OnConflict.ABORT)
    assertThat(unique == unique).isTrue()
  }

  @Test
  fun equals_differentClass() {
    val unique = Unique(arrayOf(ExampleTable::field, ExampleTable::field2), OnConflict.ABORT)
    val other = arrayOf(ExampleTable::field, ExampleTable::field2)
    @Suppress("ReplaceCallWithComparison")
    assertThat(unique.equals(other)).isFalse()
  }

  @Test
  fun equals_differentColumns() {
    val unique = Unique(arrayOf(ExampleTable::field, ExampleTable::field2), OnConflict.ABORT)
    val otherUnique = Unique(arrayOf(ExampleTable::field), OnConflict.ABORT)
    assertThat(unique == otherUnique).isFalse()
  }

  @Test
  fun equals_differentOnConflict() {
    val unique = Unique(arrayOf(ExampleTable::field, ExampleTable::field2), OnConflict.ABORT)
    val otherUnique = Unique(arrayOf(ExampleTable::field, ExampleTable::field2), OnConflict.IGNORE)
    assertThat(unique == otherUnique).isFalse()
  }

  @Test
  fun equals() {
    val unique = Unique(arrayOf(ExampleTable::field, ExampleTable::field2), OnConflict.ABORT)
    val otherUnique = Unique(arrayOf(ExampleTable::field, ExampleTable::field2), OnConflict.ABORT)
    assertThat(unique == otherUnique).isTrue()
  }

  @Test
  fun hashCode_valid() {
    val unique = Unique(arrayOf(ExampleTable::field, ExampleTable::field2), OnConflict.ABORT)
    val otherUnique = Unique(arrayOf(ExampleTable::field, ExampleTable::field2), OnConflict.ABORT)
    assertThat(unique.hashCode()).isEqualTo(otherUnique.hashCode())
  }
}

private data class ExampleTable(@Column(INTEGER) val field: Int, @Column(INTEGER) val field2: Int) :
    Table<ExampleTable>()
