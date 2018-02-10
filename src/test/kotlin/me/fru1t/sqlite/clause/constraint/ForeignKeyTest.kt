package me.fru1t.sqlite.clause.constraint

import com.google.common.truth.Truth.assertThat
import me.fru1t.sqlite.TableColumns
import me.fru1t.sqlite.clause.constraint.resolutionstrategy.OnForeignKeyConflict
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class ForeignKeyTest {
  private lateinit var foreignKey: ForeignKey<ChildTable, ParentTable, Int>

  @BeforeEach
  fun setUp() {
    foreignKey =
        ForeignKey(
            ChildTable::parentTableId,
            ParentTable::id,
            OnForeignKeyConflict.NO_ACTION,
            OnForeignKeyConflict.NO_ACTION)
  }

  @Test
  fun references() {
    val result = ChildTable::parentTableId references ParentTable::id
    assertThat(result.childColumn).isEqualTo(ChildTable::parentTableId)
    assertThat(result.parentColumn).isEqualTo(ParentTable::id)
    assertThat(result.onUpdate).isEqualTo(OnForeignKeyConflict.NO_ACTION)
    assertThat(result.onDelete).isEqualTo(OnForeignKeyConflict.NO_ACTION)
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
  fun getConstraintClause() {
    assertThat(foreignKey.getClause())
        .isEqualTo("CONSTRAINT `fk_child_table_parent_table_id` FOREIGN KEY " +
            "(`parent_table_id`) REFERENCES `parent_table`(`id`) " +
            "ON UPDATE NO ACTION ON DELETE NO ACTION")
  }

  @Test
  fun getConstraintName() {
    assertThat(foreignKey.getConstraintName()).isEqualTo("fk_child_table_parent_table_id")
  }

  @Test
  fun getLocalTable() {
    assertThat(foreignKey.getLocalTable()).isEqualTo(ChildTable::class)
  }

  @Test
  fun getForeignTable() {
    assertThat(foreignKey.getForeignTable()).isEqualTo(ParentTable::class)
  }
}

private data class ParentTable(val id: Int) : TableColumns<ParentTable>()
private data class ChildTable(val id: Int, val parentTableId: Int) : TableColumns<ChildTable>()
