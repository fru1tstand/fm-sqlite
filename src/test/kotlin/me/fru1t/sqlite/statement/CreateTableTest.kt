package me.fru1t.sqlite.statement

import com.google.common.truth.Truth.assertThat
import me.fru1t.sqlite.LocalSqliteException
import me.fru1t.sqlite.TableColumns
import me.fru1t.sqlite.clause.Constraint
import org.junit.jupiter.api.Assertions.fail
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class CreateTableTest {
  private lateinit var builder: CreateTable.Builder<CreateTableTestTable>

  @BeforeEach
  fun setUp() {
    builder = CreateTable.Builder(CreateTableTestTable::class)
  }

  @Test
  fun from() {
    val result = CreateTable.from(CreateTableTestTable::class).build()
    assertThat(result.columnsClass).isEqualTo(CreateTableTestTable::class)
  }

  @Test
  fun builder() {
    val result = builder.build()
    assertThat(result.withoutRowId).isFalse()
    assertThat(result.columnsClass).isEqualTo(CreateTableTestTable::class)
    assertThat(result.constraints).isEmpty()
    assertThat(result.defaults).isEmpty()
    assertThat(result.autoIncrementColumn).isNull()
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
  fun builder_declaredMemberProperty() {
    try {
      CreateTable.Builder(CreateTableTestTableWithMemberProperty::class)
      fail<Unit>("Expecting LocalSqliteException for having a declared member property.")
    } catch (e: LocalSqliteException) {
      assertThat(e).hasMessageThat().contains("may not have declared member properties")
      assertThat(e).hasMessageThat()
          .contains(CreateTableTestTableWithMemberProperty::class.simpleName!!)
      assertThat(e).hasMessageThat().contains(CreateTableTestTableWithMemberProperty::b.name)
    }
  }

  @Test
  fun builder_withoutRowId_boolean() {
    val result = builder.withoutRowId(true).build()
    assertThat(result.withoutRowId).isTrue()
  }

  @Test
  fun builder_constraint() {
    val exampleConstraint =
      object : Constraint<CreateTableTestTable> {
        override fun getClause(): String = ""
        override fun getConstraintName(): String? = null
      }
    val result = builder.constraint(exampleConstraint).build()
    assertThat(result.constraints).containsExactly(exampleConstraint)
  }

  @Test
  fun builder_default() {
    val result =
      CreateTable.from(CreateTableTestTableWithDefault::class)
          .default(CreateTableTestTableWithDefault::b, CreateTableTestTableWithDefault.DEFAULT)
          .build()
    assertThat(result.defaults)
        .containsExactly(
            CreateTableTestTableWithDefault::b,
            CreateTableTestTableWithDefault.DEFAULT)
  }

  @Test
  fun builder_default_nonOptionalParameter() {
    try {
      builder.default(CreateTableTestTable::a, 0)
      fail<Unit>("Expected LocalSqliteException for defaulting a non-optional parameter")
    } catch (e: LocalSqliteException) {
      assertThat(e).hasMessageThat().contains(CreateTableTestTable::a.name)
      assertThat(e).hasMessageThat().contains(CreateTableTestTable::class.simpleName!!)
      assertThat(e).hasMessageThat().contains("must be an optional parameter")
    }
  }

  @Test
  fun builder_autoIncrement() {
    val result = builder.autoIncrement(CreateTableTestTable::a).build()
    assertThat(result.autoIncrementColumn).isEqualTo(CreateTableTestTable::a)
  }

  @Test
  fun builder_build_noDefaultDefined() {
    try {
      CreateTable.Builder(CreateTableTestTableWithDefault::class).build()
      fail<Unit>("Expecting LocalSqliteException for no default for optional parameter")
    } catch (e: LocalSqliteException) {
      assertThat(e).hasMessageThat().contains(CreateTableTestTable::b.name)
      assertThat(e).hasMessageThat().contains(CreateTableTestTable::class.simpleName!!)
      assertThat(e).hasMessageThat().contains("must have its default value passed")
    }
  }
}

private data class CreateTableTestTable(
    val a: Int, val b: Int) : TableColumns<CreateTableTestTable>()

private data class CreateTableTestTableWithDefault(
    val a: Int, val b: Int = DEFAULT) : TableColumns<CreateTableTestTableWithDefault>() {
  companion object {
    const val DEFAULT = 30
  }
}

private data class CreateTableTestTableWithMemberProperty(
    val a: Int) : TableColumns<CreateTableTestTableWithMemberProperty>() {
  val b: Int = 30
}

private class CreateTableTestInvalidTable : TableColumns<CreateTableTestInvalidTable>()
