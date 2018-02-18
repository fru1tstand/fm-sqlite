package me.fru1t.sqlite.statement

import com.google.common.truth.Truth.assertThat
import me.fru1t.sqlite.LocalSqliteException
import me.fru1t.sqlite.TableColumns
import me.fru1t.sqlite.clause.Constraint
import me.fru1t.sqlite.clause.Order
import me.fru1t.sqlite.clause.constraint.PrimaryKey
import me.fru1t.sqlite.clause.order
import me.fru1t.sqlite.clause.resolutionstrategy.OnConflict
import org.junit.jupiter.api.Assertions.fail
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.reflect.full.findParameterByName
import kotlin.reflect.full.primaryConstructor

class CreateTableTest {
  private lateinit var builder: CreateTable.Builder<CreateTableTestTable>

  @BeforeEach
  fun setUp() {
    builder = CreateTable.Builder(CreateTableTestTable::class).default(
        CreateTableTestTable::default, CreateTableTestTable.DEFAULT)
  }

  @Test
  fun of() {
    val result = CreateTable.from(CreateTableTestTable::class)
    assertThat(result.columnsClass).isEqualTo(CreateTableTestTable::class)
  }

  @Test
  fun builder() {
    val result = builder.build()
    assertThat(result.columnsClass).isEqualTo(CreateTableTestTable::class)
    assertThat(result.withoutRowId).isFalse()
    assertThat(result.constraints).isEmpty()
    assertThat(result.defaults).hasSize(1)
  }

  @Test
  fun builder_notADataClass() {
    try {
      CreateTable.Builder(CreateTableTestInvalidTable::class)
      fail<Unit>("Expecting LocalSqliteException for not passing a data class to the builder")
    } catch (e: LocalSqliteException) {
      assertThat(e).hasMessageThat().contains(CreateTableTestInvalidTable::class.simpleName!!)
      assertThat(e).hasMessageThat().contains("must be a kotlin data class")
    }
  }

  @Test
  fun builder_withoutRowId_boolean() {
    val result = builder.withoutRowId(true).build()
    assertThat(result.withoutRowId).isTrue()
  }

  @Test
  fun builder_withoutRowId() {
    builder.withoutRowId = true
    val result = builder.build()
    assertThat(result.withoutRowId).isTrue()
  }

  @Test
  fun builder_constraint() {
    val exampleConstraint =
      object : Constraint<CreateTableTestTable> {
      override fun getClause(): String = ""
    }
    val result = builder.constraint(exampleConstraint).build()
    assertThat(result.constraints).containsExactly(exampleConstraint)
  }

  @Test
  fun builder_primaryKey_primaryKey() {
    val result =
      builder.primaryKey(PrimaryKey(CreateTableTestTable::id, OnConflict.DEFAULT)).build()
    assertThat(result.primaryKey)
        .isEqualTo(PrimaryKey(CreateTableTestTable::id, OnConflict.DEFAULT))
  }

  @Test
  fun builder_primaryKey_indexedColumnGroup() {
    val result = builder.primaryKey(CreateTableTestTable::id order Order.DESC).build()
    assertThat(result.primaryKey)
        .isEqualTo(PrimaryKey(CreateTableTestTable::id order Order.DESC, OnConflict.DEFAULT))
  }

  @Test
  fun builder_primaryKey_kProperty1() {
    val result = builder.primaryKey(CreateTableTestTable::id).build()
    assertThat(result.primaryKey)
        .isEqualTo(PrimaryKey(CreateTableTestTable::id, OnConflict.DEFAULT))
  }

  @Test
  fun builder_primaryKey() {
    val primaryKey = PrimaryKey(CreateTableTestTable::id, OnConflict.FAIL)
    builder.primaryKey = primaryKey
    val result = builder.build()
    assertThat(result.primaryKey).isEqualTo(primaryKey)
  }

  @Test
  fun builder_default() {
    val result = builder.build()
    assertThat(result.defaults)
        .containsEntry(
            CreateTableTestTable::class.primaryConstructor!!.findParameterByName(
                CreateTableTestTable::default.name),
            CreateTableTestTable.DEFAULT)
  }

  @Test
  fun builder_default_nonOptionalParameter() {
    try {
      builder.default(CreateTableTestTable::id, 0)
      fail<Unit>("Expected LocalSqliteException for defaulting a non-optional parameter")
    } catch (e: LocalSqliteException) {
      assertThat(e).hasMessageThat().contains(CreateTableTestTable::id.name)
      assertThat(e).hasMessageThat().contains(CreateTableTestTable::class.simpleName!!)
      assertThat(e).hasMessageThat().contains("must be an optional parameter")
    }
  }

  @Test
  fun builder_default_duplicate() {
    try {
      builder.default(CreateTableTestTable::default, 0)
      fail<Unit>("Expected LocalSqliteException for duplicate default")
    } catch (e: LocalSqliteException) {
      assertThat(e).hasMessageThat().contains(CreateTableTestTable::default.name)
      assertThat(e).hasMessageThat().contains(CreateTableTestTable::class.simpleName!!)
      assertThat(e).hasMessageThat().contains(CreateTableTestTable.DEFAULT.toString())
      assertThat(e).hasMessageThat().contains(0.toString())
      assertThat(e).hasMessageThat().contains("already has the default value from")
    }
  }

  @Test
  fun builder_autoIncrement() {
    val result = builder.autoIncrement(CreateTableTestTable::id).build()
    assertThat(result.autoIncrementColumn)
        .isEqualTo(
            CreateTableTestTable::class.primaryConstructor!!.findParameterByName(
                CreateTableTestTable::id.name))
  }

  @Test
  fun builder_autoIncrement_exists() {
    try {
      builder.autoIncrement(CreateTableTestTable::id)
          .autoIncrement(CreateTableTestTable::default)
      fail<Unit>("Expected LocalSqliteException for existing auto increment column")
    } catch (e: LocalSqliteException) {
      assertThat(e).hasMessageThat().contains(CreateTableTestTable::id.name)
      assertThat(e).hasMessageThat().contains(CreateTableTestTable::default.name)
      assertThat(e).hasMessageThat().contains(CreateTableTestTable::class.simpleName!!)
      assertThat(e).hasMessageThat().contains("already has an autoincrement column")
    }
  }

  @Test
  fun builder_build_noDefaultDefined() {
    try {
      CreateTable.Builder(CreateTableTestTable::class).build()
      fail<Unit>("Expecting LocalSqliteException for no default for optional parameter")
    } catch (e: LocalSqliteException) {
      assertThat(e).hasMessageThat().contains(CreateTableTestTable::default.name)
      assertThat(e).hasMessageThat().contains(CreateTableTestTable::class.simpleName!!)
      assertThat(e).hasMessageThat().contains("must have its default value passed")
    }
  }
}

private data class CreateTableTestTable(
    val id: Int, val default: Int = DEFAULT) : TableColumns<CreateTableTestTable>() {
  companion object {
    const val DEFAULT = 30
  }
}

private class CreateTableTestInvalidTable : TableColumns<CreateTableTestInvalidTable>()
