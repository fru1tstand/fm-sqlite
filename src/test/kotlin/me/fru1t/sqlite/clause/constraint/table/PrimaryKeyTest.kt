package me.fru1t.sqlite.clause.constraint.table

import com.google.common.truth.Truth.assertThat
import me.fru1t.sqlite.TableColumns
import me.fru1t.sqlite.clause.IndexedColumn
import me.fru1t.sqlite.clause.Order
import me.fru1t.sqlite.clause.resolutionstrategy.OnConflict
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class PrimaryKeyTest {
  private lateinit var primaryKey: PrimaryKey<PrimaryKeyTestTable>

  @BeforeEach
  fun setUp() {
    primaryKey = (
        PrimaryKey on PrimaryKeyTestTable::a order Order.ASC
            and PrimaryKeyTestTable::b order Order.DESC
            onConflict OnConflict.ROLLBACK)
  }

  @Test
  fun on() {
    val result = PrimaryKey on PrimaryKeyTestTable::a
    assertThat(result.columns())
        .containsExactly(IndexedColumn(PrimaryKeyTestTable::a, Order.DEFAULT))
  }

  @Test
  fun onConflict() {
    val result = primaryKey onConflict OnConflict.FAIL
    assertThat(result.onConflict).isEqualTo(OnConflict.FAIL)
  }

  @Test
  fun getClause() {
    assertThat(primaryKey.getClause())
        .isEqualTo("CONSTRAINT PRIMARY KEY(`a` ASC, `b` DESC) ON CONFLICT ROLLBACK")
  }

  @Test
  fun getConstraintName() {
    assertThat(primaryKey.getConstraintName()).isNull()
  }

  @Test
  fun overrideToString() {
    val result = primaryKey.toString()
    assertThat(result).contains("Primary Key")
    assertThat(result).contains("onConflict")
  }
}

private data class PrimaryKeyTestTable(val a: Int, val b: Int) : TableColumns()
