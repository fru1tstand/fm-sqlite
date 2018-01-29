package me.fru1t.sqlite.constraint

import com.google.common.truth.Truth.assertThat
import me.fru1t.sqlite.LocalSqliteException
import me.fru1t.sqlite.Table
import me.fru1t.sqlite.annotation.Column
import me.fru1t.sqlite.annotation.DataType.INTEGER
import me.fru1t.sqlite.annotation.DataType.TEXT
import me.fru1t.sqlite.constraint.resolutionstrategy.OnForeignKeyConflict
import org.junit.jupiter.api.Assertions.fail
import org.junit.jupiter.api.Test
import kotlin.reflect.KProperty1

class ForeignKeyTest {
  @Test
  fun of() {
    val foreignKey = ForeignKey.of(
        ValidChildTable::parentId,
        ValidParentTable::id,
        OnForeignKeyConflict.SET_NULL,
        OnForeignKeyConflict.CASCADE)
    assertThat(foreignKey.localColumn).isEqualTo(ValidChildTable::parentId)
    assertThat(foreignKey.foreignColumn).isEqualTo(ValidParentTable::id)
    assertThat(foreignKey.onUpdate).isEqualTo(OnForeignKeyConflict.SET_NULL)
    assertThat(foreignKey.onDelete).isEqualTo(OnForeignKeyConflict.CASCADE)
  }

  @Test
  fun of_invalid() {
    try {
      ForeignKey.of(NonMatchingColumnChildTable::parentId, ValidParentTable::id)
      fail<Unit>("Expected #of to throw a LocalSqliteException about non matching column types")
    } catch (e: LocalSqliteException) {
      // expected
    }
  }

  @Test
  fun getConstraintClause() {
    val foreignKey = ForeignKey.of(ValidChildTable::parentId, ValidParentTable::id)
    assertThat(foreignKey.getConstraintClause())
        .isEqualTo("CONSTRAINT `fk_valid_child_table_valid_parent_table_id` FOREIGN KEY " +
            "(`parent_id`) REFERENCES `valid_parent_table`(`id`) " +
            "ON UPDATE NO ACTION ON DELETE NO ACTION")
  }

  @Test
  fun getConstraintName() {
    val foreignKey = ForeignKey.of(ValidChildTable::parentId, ValidParentTable::id)
    assertThat(foreignKey.getConstraintName())
        .isEqualTo("fk_valid_child_table_valid_parent_table_id")
  }

  @Test
  fun validate_nonMatchingColumnTypes() {
    val foreignKey =
      createForeignKeyWithoutValidating(NonMatchingColumnChildTable::parentId, ValidParentTable::id)
    try {
      foreignKey.validate()
      fail<Unit>(
          "Expected #validate to throw a LocalSqliteException about non matching column types")
    } catch (e: LocalSqliteException) {
      assertThat(e).hasMessageThat().contains("Local column type must match foreign column type.")
    }
  }

  @Test
  fun validate_nonMatchingAnnotationDataTypes() {
    val foreignKey =
      createForeignKeyWithoutValidating(
          NonMatchingColumnAnnotationChildTable::parentId, ValidParentTable::id)
    try {
      foreignKey.validate()
      fail<Unit>(
          "Expected #validate to throw a LocalSqliteException about non matching column " +
              "annotation types")
    } catch (e: LocalSqliteException) {
      assertThat(e).hasMessageThat()
          .contains("Local column annotation type must match foreign column annotation type.")
    }
  }

  @Test
  fun validate() {
    val foreignKey =
      createForeignKeyWithoutValidating(ValidChildTable::parentId, ValidParentTable::id)
    foreignKey.validate()
  }

  @Test
  fun getLocalTable() {
    val foreignKey =
      createForeignKeyWithoutValidating(ValidChildTable::parentId, ValidParentTable::id)
    assertThat(foreignKey.getLocalTable()).isEqualTo(ValidChildTable::class)
  }

  @Test
  fun getForeignTable() {
    val foreignKey =
      createForeignKeyWithoutValidating(ValidChildTable::parentId, ValidParentTable::id)
    assertThat(foreignKey.getForeignTable()).isEqualTo(ValidParentTable::class)
  }

  companion object {
    fun <T : Table<T>, O : Table<O>> createForeignKeyWithoutValidating(
        localColumn: KProperty1<T, *>,
        foreignColumn: KProperty1<O, *>): ForeignKey<T, O> {
      return ForeignKey(
          localColumn,
          foreignColumn,
          OnForeignKeyConflict.NO_ACTION,
          OnForeignKeyConflict.NO_ACTION)
    }
  }
}

private data class ValidParentTable(@Column(INTEGER) val id: Int) : Table<ValidParentTable>()
private data class ValidChildTable(
    @Column(INTEGER) val id: Int,
    @Column(INTEGER) val parentId: Int) : Table<ValidChildTable>()

private data class NonMatchingColumnChildTable(
    @Column(INTEGER) val id: Int, @Column(INTEGER) val parentId: String) :
    Table<NonMatchingColumnChildTable>()
private data class NonMatchingColumnAnnotationChildTable(
    @Column(INTEGER) val id: Int, @Column(TEXT) val parentId: Int) :
    Table<NonMatchingColumnAnnotationChildTable>()
