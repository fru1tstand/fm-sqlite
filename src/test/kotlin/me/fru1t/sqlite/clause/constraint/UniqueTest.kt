package me.fru1t.sqlite.clause.constraint

import com.google.common.truth.Truth.assertThat
import me.fru1t.sqlite.TableColumns
import me.fru1t.sqlite.clause.IndexedColumnGroup
import me.fru1t.sqlite.clause.and
import me.fru1t.sqlite.clause.resolutionstrategy.OnConflict
import me.fru1t.sqlite.clause.resolutionstrategy.OnConflict.FAIL
import me.fru1t.sqlite.clause.resolutionstrategy.OnConflict.ROLLBACK
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class UniqueTest {
  private lateinit var unique: Unique<UniqueTestTable>

  @BeforeEach
  fun setUp() {
    unique = Unique from (UniqueTestTable::a and UniqueTestTable::b) onConflict ROLLBACK
  }

  @Test
  fun from_indexedColumnGroup() {
    val result = Unique from (UniqueTestTable::a and UniqueTestTable::b)
    assertThat(result.columnGroup).isEqualTo(UniqueTestTable::a and UniqueTestTable::b)
    assertThat(result.onConflict).isEqualTo(OnConflict.DEFAULT)
  }

  @Test
  fun from_kProperty1() {
    val result = Unique from UniqueTestTable::a
    assertThat(result.columnGroup).isEqualTo(IndexedColumnGroup(UniqueTestTable::a))
    assertThat(result.onConflict).isEqualTo(OnConflict.DEFAULT)
  }

  @Test
  fun onConflict() {
    val result = unique onConflict FAIL
    assertThat(result.onConflict).isEqualTo(FAIL)
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
}

private data class UniqueTestTable(val a: Int, val b: Int) : TableColumns<UniqueTestTable>()
