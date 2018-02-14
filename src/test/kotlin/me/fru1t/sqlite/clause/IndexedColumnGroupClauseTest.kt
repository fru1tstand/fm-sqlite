package me.fru1t.sqlite.clause

import com.google.common.truth.Truth.assertThat
import me.fru1t.sqlite.Order.DESC
import me.fru1t.sqlite.TableColumns
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class FileTest {
  @Test
  fun kProperty1_order() {
    val result = Table::a order DESC
    assertThat(result.column).isEqualTo(Table::a)
    assertThat(result.order).isEqualTo(DESC)
  }

  @Test
  fun kProperty1_and_kProperty1() {
    val result = Table::a and Table::b
    assertThat(result.getIndexedColumns())
        .containsExactly(IndexedColumn(Table::a), IndexedColumn(Table::b))
  }

  @Test
  fun kProperty1_and_indexedColumn() {
    val result = Table::a and (Table::b order DESC)
    assertThat(result.getIndexedColumns())
        .containsExactly(IndexedColumn(Table::a), IndexedColumn(Table::b, DESC))
  }
}

class IndexedColumnTest {
  private lateinit var indexedColumn: IndexedColumn<Table>

  @BeforeEach
  fun setUp() {
    indexedColumn = IndexedColumn(Table::a, DESC)
  }

  @Test
  fun getClause() {
    assertThat(indexedColumn.getClause()).isEqualTo("(`a` DESC)")
  }

  @Test
  fun getClauseWithoutOrder() {
    assertThat(indexedColumn.getClauseWithoutOrder()).isEqualTo("(`a`)")
  }

  @Test
  fun getIndexedColumns() {
    assertThat(indexedColumn.getIndexedColumns()).containsExactly(indexedColumn)
  }

  @Test
  fun getClauseWithoutGroup() {
    assertThat(indexedColumn.getClauseWithoutGroup()).isEqualTo("`a` DESC")
  }

  @Test
  fun getClauseWithoutGroup_nullOrder() {
    val result = IndexedColumn(Table::a, null)
    assertThat(result.getClauseWithoutGroup()).isEqualTo("`a`")
  }

  @Test
  fun and_indexedColumn() {
    val result = indexedColumn and (Table::b order DESC)
    assertThat(result.getIndexedColumns()).containsExactly(indexedColumn, IndexedColumn(Table::b, DESC))
  }

  @Test
  fun and_column() {
    val result = indexedColumn and Table::b
    assertThat(result.getIndexedColumns()).containsExactly(indexedColumn, IndexedColumn(Table::b))
  }
}

class IndexedColumnGroupTest {
  private lateinit var indexedColumnGroup: IndexedColumnGroup<Table>

  @BeforeEach
  fun setUp() {
    indexedColumnGroup = IndexedColumnGroup(Table::a).and(IndexedColumn(Table::b, DESC))
  }

  @Test
  fun getClause() {
    assertThat(indexedColumnGroup.getClause()).isEqualTo("(`a`, `b` DESC)")
  }

  @Test
  fun getClauseWithoutOrder() {
    assertThat(indexedColumnGroup.getClauseWithoutOrder()).isEqualTo("(`a`, `b`)")
  }

  @Test
  fun getIndexedColumns() {
    assertThat(indexedColumnGroup.getIndexedColumns())
        .containsExactly(IndexedColumn(Table::a), IndexedColumn(Table::b, DESC))
  }

  @Test
  fun and_indexedColumn() {
    val result = indexedColumnGroup and (Table::c order DESC)
    assertThat(result.getIndexedColumns())
        .containsExactly(
            IndexedColumn(Table::a), IndexedColumn(Table::b, DESC), IndexedColumn(Table::c, DESC))
  }

  @Test
  fun and_column() {
    val result = indexedColumnGroup and Table::c
    assertThat(result.getIndexedColumns())
        .containsExactly(
            IndexedColumn(Table::a), IndexedColumn(Table::b, DESC), IndexedColumn(Table::c))
  }
}

private data class Table(val a: Int, val b: String, val c: String) : TableColumns<Table>()
