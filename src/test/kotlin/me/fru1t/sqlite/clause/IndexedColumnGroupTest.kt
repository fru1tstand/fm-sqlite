package me.fru1t.sqlite.clause

import com.google.common.truth.Truth.assertThat
import me.fru1t.sqlite.LocalSqliteException
import me.fru1t.sqlite.clause.Order.DESC
import me.fru1t.sqlite.TableColumns
import me.fru1t.sqlite.clause.Order.ASC
import org.junit.jupiter.api.Assertions.fail
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class FileTest {
  @Test
  fun kProperty1_order() {
    val result = Table::a order DESC
    assertThat(result.columns).hasSize(1)
    assertThat(result.columns[0].column).isEqualTo(Table::a)
    assertThat(result.columns[0].order).isEqualTo(DESC)
  }

  @Test
  fun kProperty1_and_kProperty1() {
    val result = Table::a and Table::b
    assertThat(result.columns)
        .containsExactly(
            IndexedColumn(Table::a, Order.DEFAULT), IndexedColumn(Table::b, Order.DEFAULT))
  }

  @Test
  fun kProperty1_and_indexedColumn() {
    val result = Table::a and (Table::b order DESC)
    assertThat(result.columns)
        .containsExactly(IndexedColumn(Table::a, Order.DEFAULT), IndexedColumn(Table::b, DESC))
  }
}

class IndexedColumnTest {
  @Test
  fun overrideToString() {
    val result = IndexedColumn(Table::a, DESC).toString()
    assertThat(result).isEqualTo("`a` DESC")
  }
}

class IndexedColumnGroupTest {
  private lateinit var indexedColumnGroup: IndexedColumnGroup<Table>

  @BeforeEach
  fun setUp() {
    indexedColumnGroup =
        IndexedColumnGroup(
            listOf(IndexedColumn(Table::a, Order.DEFAULT), IndexedColumn(Table::b, ASC)))
  }

  @Test
  fun constructor_kProperty1() {
    val result = IndexedColumnGroup(Table::a)
    assertThat(result.columns).containsExactly(IndexedColumn(Table::a, Order.DEFAULT))
  }

  @Test
  fun init_noColumns() {
    try {
      IndexedColumnGroup<Table>(emptyList())
      fail<Unit>("Expected a LocalSqliteException about passing zero columns")
    } catch (e: LocalSqliteException) {
      assertThat(e).hasMessageThat().contains("cannot have zero columns")
    }
  }

  @Test
  fun getClause() {
    assertThat(indexedColumnGroup.getClause()).isEqualTo("(`a` ASC, `b` ASC)")
  }

  @Test
  fun getClauseWithoutGrouping() {
    assertThat(indexedColumnGroup.getClauseWithoutGrouping()).isEqualTo("`a` ASC, `b` ASC")
  }

  @Test
  fun and_indexedColumnGroup() {
    val result = indexedColumnGroup and (Table::c order DESC)
    assertThat(result.columns)
        .containsExactly(
            IndexedColumn(Table::a, Order.DEFAULT),
            IndexedColumn(Table::b, ASC),
            IndexedColumn(Table::c, DESC))
  }

  @Test
  fun and_kProperty1() {
    val result = indexedColumnGroup and Table::c
    assertThat(result.columns)
        .containsExactly(
            IndexedColumn(Table::a, Order.DEFAULT),
            IndexedColumn(Table::b, ASC),
            IndexedColumn(Table::c, Order.DEFAULT))
  }
}

private data class Table(val a: Int, val b: String, val c: String) : TableColumns<Table>()
