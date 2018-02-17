package me.fru1t.sqlite.clause

import com.google.common.truth.Truth.assertThat
import me.fru1t.sqlite.LocalSqliteException
import me.fru1t.sqlite.Order.DESC
import me.fru1t.sqlite.TableColumns
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
    assertThat(result.columns).containsExactly(IndexedColumn(Table::a), IndexedColumn(Table::b))
  }

  @Test
  fun kProperty1_and_indexedColumn() {
    val result = Table::a and (Table::b order DESC)
    assertThat(result.columns)
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
  fun overrideToString() {
    assertThat(indexedColumn.toString()).isEqualTo("`a` DESC")
  }

  @Test
  fun toStringWithoutOrder() {
    assertThat(indexedColumn.toStringWithoutOrder()).isEqualTo("`a`")
  }
}

class IndexedColumnGroupTest {
  private lateinit var indexedColumnGroup: IndexedColumnGroup<Table>

  @BeforeEach
  fun setUp() {
    indexedColumnGroup =
        IndexedColumnGroup(listOf(IndexedColumn(Table::a), IndexedColumn(Table::b, DESC)))
  }

  @Test
  fun constructor_kProperty1() {
    val result = IndexedColumnGroup(Table::a)
    assertThat(result.columns).containsExactly(IndexedColumn(Table::a))
  }

  @Test
  fun constructor_indexedColumn() {
    val result = IndexedColumnGroup(IndexedColumn(Table::a, DESC))
    assertThat(result.columns).containsExactly(IndexedColumn(Table::a, DESC))
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
  fun getClauseWithoutOrder() {
    assertThat(indexedColumnGroup.getClauseWithoutOrder()).isEqualTo("(`a`, `b`)")
  }

  @Test
  fun getClause() {
    assertThat(indexedColumnGroup.getClause()).isEqualTo("(`a`, `b` DESC)")
  }

  @Test
  fun and_indexedColumn() {
    val result = indexedColumnGroup and (Table::c order DESC)
    assertThat(result.columns)
        .containsExactly(
            IndexedColumn(Table::a), IndexedColumn(Table::b, DESC), IndexedColumn(Table::c, DESC))
  }

  @Test
  fun and_kProperty1() {
    val result = indexedColumnGroup and Table::c
    assertThat(result.columns)
        .containsExactly(
            IndexedColumn(Table::a), IndexedColumn(Table::b, DESC), IndexedColumn(Table::c))
  }
}

private data class Table(val a: Int, val b: String, val c: String) : TableColumns<Table>()
