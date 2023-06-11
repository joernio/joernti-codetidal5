import io.shiftleft.codepropertygraph.schema._
import overflowdb.schema.SchemaBuilder
import overflowdb.schema.Property.ValueType

class CpgExtSchema(builder: SchemaBuilder, cpgSchema: CpgSchema) {

  // Add node types, edge types, and properties here

  val myProperty = builder
    .addProperty(name = "MYPROPERTY", valueType = ValueType.String)
    .mandatory("")

  val myNodeType = builder
    .addNodeType("MYNODETYPE")
    .addProperty(myProperty)

}

object CpgExtSchema {
  val builder   = new SchemaBuilder(domainShortName = "Cpg", basePackage = "io.shiftleft.codepropertygraph.generated")
  val cpgSchema = new CpgSchema(builder)
  val cpgExtSchema = new CpgExtSchema(builder, cpgSchema)
  val instance     = builder.build
}
