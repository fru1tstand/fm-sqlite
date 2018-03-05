package me.fru1t.sqlite.clause.constraint.table

import com.google.common.truth.Truth.assertThat
import me.fru1t.sqlite.TableColumns
import me.fru1t.sqlite.clause.resolutionstrategy.OnForeignKeyConflict
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class ForeignKeyFileTest {
  @Test
  fun references() {
    val result = FktChildTable::parentTableId references FktParentTable::id
    assertThat(result.childColumn).isEqualTo(FktChildTable::parentTableId)
    assertThat(result.parentColumn).isEqualTo(FktParentTable::id)
    assertThat(result.onUpdate).isEqualTo(OnForeignKeyConflict.NO_ACTION)
    assertThat(result.onDelete).isEqualTo(OnForeignKeyConflict.NO_ACTION)
  }
}

class ForeignKeyTest {
  private lateinit var foreignKey: ForeignKey<FktChildTable, FktParentTable, Int>

  @BeforeEach
  fun setUp() {
    foreignKey = ForeignKey(
        FktChildTable::parentTableId,
        FktParentTable::id,
        OnForeignKeyConflict.NO_ACTION,
        OnForeignKeyConflict.NO_ACTION)
  }

  @Test
  fun onUpdate() {
    val result = foreignKey onUpdate OnForeignKeyConflict.CASCADE
    assertThat(result.onUpdate).isEqualTo(OnForeignKeyConflict.CASCADE)
  }

  @Test
  fun onDelete() {
    val result = foreignKey onDelete OnForeignKeyConflict.CASCADE
    assertThat(result.onDelete).isEqualTo(OnForeignKeyConflict.CASCADE)
  }

  @Test
  fun getChildTable() {
    assertThat(foreignKey.getChildTable()).isEqualTo(FktChildTable::class)
  }

  @Test
  fun getParentTable() {
    assertThat(foreignKey.getParentTable()).isEqualTo(FktParentTable::class)
  }

  @Test
  fun getClause() {
    assertThat(foreignKey.getClause())
        .isEqualTo("CONSTRAINT `fk_fkt_child_table_fkt_parent_table_id` FOREIGN KEY " +
            "(`parent_table_id`) REFERENCES `fkt_parent_table`(`id`) " +
            "ON UPDATE NO ACTION ON DELETE NO ACTION")
  }

  @Test
  fun getConstraintName() {
    assertThat(foreignKey.getConstraintName()).isEqualTo("fk_fkt_child_table_fkt_parent_table_id")
  }

  @Test
  fun overrideToString() {
    val result = foreignKey.toString()
    assertThat(result).contains("Foreign Key")
    assertThat(result).contains("references")
    assertThat(result).contains("onUpdate")
    assertThat(result).contains("onDelete")
  }
}

private data class FktParentTable(val id: Int) : TableColumns()
private data class FktChildTable(val id: Int, val parentTableId: Int) : TableColumns()
