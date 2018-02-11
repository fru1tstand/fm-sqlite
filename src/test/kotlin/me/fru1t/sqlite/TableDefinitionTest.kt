package me.fru1t.sqlite

import com.google.common.truth.Truth.assertThat
import me.fru1t.sqlite.clause.Constraint
import org.junit.jupiter.api.Assertions.fail
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.reflect.full.findParameterByName
import kotlin.reflect.full.primaryConstructor

class TableDefinitionTest {
  private lateinit var builder: TableDefinition.Builder<Table>

  @BeforeEach
  fun setUp() {
    builder = TableDefinition.Builder(Table::class).default(Table::default, Table.DEFAULT)
  }

  @Test
  fun of() {
    val result = TableDefinition.of(Table::class)
    assertThat(result.columnsClass).isEqualTo(Table::class)
  }

  @Test
  fun builder() {
    val result = builder.build()
    assertThat(result.columnsClass).isEqualTo(Table::class)
    assertThat(result.withoutRowId).isFalse()
    assertThat(result.constraints).isEmpty()
    assertThat(result.defaults).hasSize(1)
  }

  @Test
  fun builder_notADataClass() {
    try {
      TableDefinition.Builder(InvalidTable::class)
      fail<Unit>("Expecting LocalSqliteException for not passing a data class to the builder")
    } catch (e: LocalSqliteException) {
      assertThat(e).hasMessageThat().contains(InvalidTable::class.simpleName!!)
      assertThat(e).hasMessageThat().contains("must be a kotlin data class")
    }
  }

  @Test
  fun builder_withoutRowId() {
    val result = builder.withoutRowId(true).build()
    assertThat(result.withoutRowId).isTrue()
  }

  @Test
  fun builder_constraint() {
    val exampleConstraint =
      object : Constraint<Table> {
      override fun getClause(): String = ""
    }
    val result = builder.constraint(exampleConstraint).build()
    assertThat(result.constraints).containsExactly(exampleConstraint)
  }

  @Test
  fun builder_default() {
    val result = builder.build()
    assertThat(result.defaults)
        .containsEntry(
            Table::class.primaryConstructor!!.findParameterByName(Table::default.name),
            Table.DEFAULT)
  }

  @Test
  fun builder_default_nonOptionalParameter() {
    try {
      builder.default(Table::id, 0)
      fail<Unit>("Expected LocalSqliteException for defaulting a non-optional parameter")
    } catch (e: LocalSqliteException) {
      assertThat(e).hasMessageThat().contains(Table::id.name)
      assertThat(e).hasMessageThat().contains(Table::class.simpleName!!)
      assertThat(e).hasMessageThat().contains("must be an optional parameter")
    }
  }

  @Test
  fun builder_default_duplicate() {
    try {
      builder.default(Table::default, 0)
      fail<Unit>("Expected LocalSqliteException for duplicate default")
    } catch (e: LocalSqliteException) {
      assertThat(e).hasMessageThat().contains(Table::default.name)
      assertThat(e).hasMessageThat().contains(Table::class.simpleName!!)
      assertThat(e).hasMessageThat().contains("default is already defined")
    }
  }

  @Test
  fun builder_autoIncrement() {
    val result = builder.autoIncrement(Table::id).build()
    assertThat(result.autoIncrementColumn)
        .isEqualTo(Table::class.primaryConstructor!!.findParameterByName(Table::id.name))
  }

  @Test
  fun builder_autoIncrement_exists() {
    try {
      builder.autoIncrement(Table::id).autoIncrement(Table::id)
      fail<Unit>("Expected LocalSqliteException for existing auto increment column")
    } catch (e: LocalSqliteException) {
      assertThat(e).hasMessageThat().contains(Table::id.name)
      assertThat(e).hasMessageThat().contains(Table::class.simpleName!!)
      assertThat(e).hasMessageThat().contains("already has AUTOINCREMENT column")
    }
  }

  @Test
  fun builder_build_noDefaultDefined() {
    try {
      TableDefinition.Builder(Table::class).build()
      fail<Unit>("Expecting LocalSqliteException for no default for optional parameter")
    } catch (e: LocalSqliteException) {
      assertThat(e).hasMessageThat().contains(Table::default.name)
      assertThat(e).hasMessageThat().contains(Table::class.simpleName!!)
      assertThat(e).hasMessageThat().contains("must have its default value passed")
    }
  }
}

private data class Table(val id: Int, val default: Int = DEFAULT) : TableColumns<Table>() {
  companion object {
    const val DEFAULT = 30
  }
}

private class InvalidTable : TableColumns<InvalidTable>()
