package me.fru1t.sqlite

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test

class TableColumnsTest {
  @Test
  fun getTableName() {
    assertThat(TableColumns.getTableName(LongTableNameClassForUseInTesting::class))
        .isEqualTo("long_table_name_class_for_use_in_testing")
  }

  @Test
  fun getColumnName() {
    assertThat(TableColumns.getColumnName(LongColumnClass::thisHasAReallyLongColumnName))
        .isEqualTo("this_has_a_really_long_column_name")
  }

  @Test
  fun getTableFromColumn() {
    val resultTable = TableColumns.getTableFromColumn(ValidTable::id)
    assertThat(resultTable).isEqualTo(ValidTable::class)
  }
}

private data class LongTableNameClassForUseInTesting(val id: Int) :
    TableColumns<LongTableNameClassForUseInTesting>()
private data class LongColumnClass(val thisHasAReallyLongColumnName: String) :
    TableColumns<LongColumnClass>()

private data class ValidTable(val id: Int) : TableColumns<ValidTable>()
