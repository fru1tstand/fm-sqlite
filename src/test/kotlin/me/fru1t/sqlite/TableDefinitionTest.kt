package me.fru1t.sqlite

import com.google.common.truth.Truth.assertThat
import me.fru1t.sqlite.clause.Constraint
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class TableDefinitionTest {
  private lateinit var builder: TableDefinition.Builder<ExampleTable>

  @BeforeEach
  fun setUp() {
    builder = TableDefinition.Builder(ExampleTable::class)
  }

  @Test
  fun of() {
    val result = TableDefinition.of(ExampleTable::class)
    assertThat(result.table).isEqualTo(ExampleTable::class)
  }

  @Test
  fun builder() {
    val result = builder.build()
    assertThat(result.table).isEqualTo(ExampleTable::class)
    assertThat(result.withoutRowId).isFalse()
    assertThat(result.constraints).isEmpty()
  }

  @Test
  fun builder_withoutRowId() {
    val result = builder.withoutRowId(true).build()
    assertThat(result.withoutRowId).isTrue()
  }

  @Test
  fun builder_constraint() {
    val exampleConstraint =
      object : Constraint<ExampleTable> {
      override fun getClause(): String = ""
    }
    val result = builder.constraint(exampleConstraint).build()
    assertThat(result.constraints).containsExactly(exampleConstraint)
  }
}

data class ExampleTable(val id: Int) : TableColumns<ExampleTable>()
