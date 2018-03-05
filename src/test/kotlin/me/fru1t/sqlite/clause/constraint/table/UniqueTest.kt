package me.fru1t.sqlite.clause.constraint.table

import com.google.common.truth.Truth.assertThat
import me.fru1t.sqlite.TableColumns
import me.fru1t.sqlite.clause.IndexedColumn
import me.fru1t.sqlite.clause.Order
import me.fru1t.sqlite.clause.resolutionstrategy.OnConflict
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class UniqueTest {
  private lateinit var unique: Unique<UniqueTestTable>

  @BeforeEach
  fun setUp() {
    unique = Unique on UniqueTestTable::a and UniqueTestTable::b onConflict OnConflict.ROLLBACK
  }

  @Test
  fun on() {
    val result = PrimaryKey on UniqueTestTable::a
    assertThat(result.columns())
        .containsExactly(IndexedColumn(UniqueTestTable::a, Order.DEFAULT))
  }

  @Test
  fun onConflict() {
    val result = unique onConflict OnConflict.FAIL
    assertThat(result.onConflict).isEqualTo(OnConflict.FAIL)
  }

  @Test
  fun getClause() {
    assertThat(unique.getClause())
        .isEqualTo("CONSTRAINT `uq_a_b` UNIQUE (`a` ASC, `b` ASC) ON CONFLICT ROLLBACK")
  }

  @Test
  fun getConstraintName() {
    assertThat(unique.getConstraintName()).isEqualTo("uq_a_b")
  }

  @Test
  fun overrideToString() {
    val result = unique.toString()
    assertThat(result).contains("Unique on")
    assertThat(result).contains("onConflict")
  }
}

private data class UniqueTestTable(val a: Int, val b: Int) : TableColumns()
