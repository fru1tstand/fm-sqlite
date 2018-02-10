package me.fru1t.sqlite

import com.google.common.truth.Truth.assertThat
import me.fru1t.sqlite.annotation.Column
import me.fru1t.sqlite.annotation.DataType.INTEGER
import org.junit.jupiter.api.Assertions.fail
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
  fun getColumnAnnotation() {
    val resultAnnotation = TableColumns.getColumnAnnotation(ValidTable::id)
    assertThat(resultAnnotation.dataType).isEqualTo(INTEGER)
  }

  @Test
  fun getColumnAnnotation_noAnnotation() {
    try {
      TableColumns.getColumnAnnotation(TableWithoutColumnAnnotation::id)
      fail<Unit>(
          "#getColumnAnnotation should have thrown a LocalSqliteException about not having a " +
              "column annotation")
    } catch (e: LocalSqliteException) {
      assertThat(e).hasMessageThat().contains("Missing @Column annotation on")
    }
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

private data class ValidTable(@Column(INTEGER) val id: Int) : TableColumns<ValidTable>()
private data class TableWithoutColumnAnnotation(val id: Int): TableColumns<TableWithoutColumnAnnotation>()
