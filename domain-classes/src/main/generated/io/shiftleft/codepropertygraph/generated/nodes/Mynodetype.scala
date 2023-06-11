package io.shiftleft.codepropertygraph.generated.nodes

import overflowdb._
import scala.jdk.CollectionConverters._

object Mynodetype {
  def apply(graph: Graph, id: Long) = new Mynodetype(graph, id)

  val Label = "MYNODETYPE"

  object PropertyNames {
    val Myproperty                       = "MYPROPERTY"
    val all: Set[String]                 = Set(Myproperty)
    val allAsJava: java.util.Set[String] = all.asJava
  }

  object Properties {
    val Myproperty = new overflowdb.PropertyKey[String]("MYPROPERTY")

  }

  object PropertyDefaults {
    val Myproperty = ""
  }

  val layoutInformation = new NodeLayoutInformation(Label, PropertyNames.allAsJava, List().asJava, List().asJava)

  object Edges {
    val Out: Array[String] = Array()
    val In: Array[String]  = Array()
  }

  val factory = new NodeFactory[MynodetypeDb] {
    override val forLabel = Mynodetype.Label

    override def createNode(ref: NodeRef[MynodetypeDb]) =
      new MynodetypeDb(ref.asInstanceOf[NodeRef[NodeDb]])

    override def createNodeRef(graph: Graph, id: Long) = Mynodetype(graph, id)
  }
}

trait MynodetypeBase extends AbstractNode {
  def asStored: StoredNode = this.asInstanceOf[StoredNode]

  def myproperty: String

}

class Mynodetype(graph_4762: Graph, id_4762: Long /*cf https://github.com/scala/bug/issues/4762 */ )
    extends NodeRef[MynodetypeDb](graph_4762, id_4762)
    with MynodetypeBase
    with StoredNode {
  override def myproperty: String = get().myproperty
  override def propertyDefaultValue(propertyKey: String) =
    propertyKey match {
      case "MYPROPERTY" => Mynodetype.PropertyDefaults.Myproperty
      case _            => super.propertyDefaultValue(propertyKey)
    }

  // In view of https://github.com/scala/bug/issues/4762 it is advisable to use different variable names in
  // patterns like `class Base(x:Int)` and `class Derived(x:Int) extends Base(x)`.
  // This must become `class Derived(x_4762:Int) extends Base(x_4762)`.
  // Otherwise, it is very hard to figure out whether uses of the identifier `x` refer to the base class x
  // or the derived class x.
  // When using that pattern, the class parameter `x_47672` should only be used in the `extends Base(x_4762)`
  // clause and nowhere else. Otherwise, the compiler may well decide that this is not just a constructor
  // parameter but also a field of the class, and we end up with two `x` fields. At best, this wastes memory;
  // at worst both fields go out-of-sync for hard-to-debug correctness bugs.

  override def fromNewNode(newNode: NewNode, mapping: NewNode => StoredNode): Unit = get().fromNewNode(newNode, mapping)
  override def canEqual(that: Any): Boolean                                        = get.canEqual(that)
  override def label: String = {
    Mynodetype.Label
  }

  override def productElementName(n: Int): String =
    n match {
      case 0 => "id"
      case 1 => "myproperty"
    }

  override def productElement(n: Int): Any =
    n match {
      case 0 => id
      case 1 => myproperty
    }

  override def productPrefix = "Mynodetype"
  override def productArity  = 2
}

class MynodetypeDb(ref: NodeRef[NodeDb]) extends NodeDb(ref) with StoredNode with MynodetypeBase {

  override def layoutInformation: NodeLayoutInformation = Mynodetype.layoutInformation

  private var _myproperty: String = Mynodetype.PropertyDefaults.Myproperty
  def myproperty: String          = _myproperty

  /** faster than the default implementation */
  override def propertiesMap: java.util.Map[String, Any] = {
    val properties = new java.util.HashMap[String, Any]
    properties.put("MYPROPERTY", myproperty)

    properties
  }

  /** faster than the default implementation */
  override def propertiesMapForStorage: java.util.Map[String, Any] = {
    val properties = new java.util.HashMap[String, Any]
    if (!(("") == myproperty)) { properties.put("MYPROPERTY", myproperty) }

    properties
  }

  import overflowdb.traversal._

  override def label: String = {
    Mynodetype.Label
  }

  override def productElementName(n: Int): String =
    n match {
      case 0 => "id"
      case 1 => "myproperty"
    }

  override def productElement(n: Int): Any =
    n match {
      case 0 => id
      case 1 => myproperty
    }

  override def productPrefix = "Mynodetype"
  override def productArity  = 2

  override def canEqual(that: Any): Boolean = that != null && that.isInstanceOf[MynodetypeDb]

  override def property(key: String): Any = {
    key match {
      case "MYPROPERTY" => this._myproperty

      case _ => null
    }
  }

  override protected def updateSpecificProperty(key: String, value: Object): Unit = {
    key match {
      case "MYPROPERTY" => this._myproperty = value.asInstanceOf[String]

      case _ => PropertyErrorRegister.logPropertyErrorIfFirst(getClass, key)
    }
  }

  override def removeSpecificProperty(key: String): Unit =
    this.updateSpecificProperty(key, null)

  override def _initializeFromDetached(
    data: overflowdb.DetachedNodeData,
    mapper: java.util.function.Function[overflowdb.DetachedNodeData, Node]
  ) =
    fromNewNode(data.asInstanceOf[NewNode], nn => mapper.apply(nn).asInstanceOf[StoredNode])

  override def fromNewNode(newNode: NewNode, mapping: NewNode => StoredNode): Unit = {
    this._myproperty = newNode.asInstanceOf[NewMynodetype].myproperty

  }

}
