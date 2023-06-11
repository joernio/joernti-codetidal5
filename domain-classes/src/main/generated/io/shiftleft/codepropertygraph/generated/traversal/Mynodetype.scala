package io.shiftleft.codepropertygraph.generated.traversal

import overflowdb.traversal._
import io.shiftleft.codepropertygraph.generated.nodes._

/** Traversal steps for Mynodetype */
class MynodetypeTraversalExtGen[NodeType <: Mynodetype](val traversal: Iterator[NodeType]) extends AnyVal {

  /** Traverse to myproperty property */
  def myproperty: Iterator[String] =
    traversal.map(_.myproperty)

  /** Traverse to nodes where the myproperty matches the regular expression `value`
    */
  def myproperty(pattern: String): Iterator[NodeType] = {
    if (!Misc.isRegex(pattern)) {
      mypropertyExact(pattern)
    } else {
      overflowdb.traversal.filter.StringPropertyFilter.regexp(traversal)(_.myproperty, pattern)
    }
  }

  /** Traverse to nodes where the myproperty matches at least one of the regular expressions in `values`
    */
  def myproperty(patterns: String*): Iterator[NodeType] =
    overflowdb.traversal.filter.StringPropertyFilter.regexpMultiple(traversal)(_.myproperty, patterns)

  /** Traverse to nodes where myproperty matches `value` exactly.
    */
  def mypropertyExact(value: String): Iterator[NodeType] = {
    val fastResult = traversal match {
      case init: overflowdb.traversal.InitialTraversal[NodeType] => init.getByIndex("MYPROPERTY", value).getOrElse(null)
      case _                                                     => null
    }
    if (fastResult != null) fastResult
    else traversal.filter { node => node.myproperty == value }
  }

  /** Traverse to nodes where myproperty matches one of the elements in `values` exactly.
    */
  def mypropertyExact(values: String*): Iterator[NodeType] = {
    if (values.size == 1)
      mypropertyExact(values.head)
    else
      overflowdb.traversal.filter.StringPropertyFilter
        .exactMultiple[NodeType, String](traversal, node => Some(node.myproperty), values, "MYPROPERTY")
  }

  /** Traverse to nodes where myproperty does not match the regular expression `value`.
    */
  def mypropertyNot(pattern: String): Iterator[NodeType] = {
    if (!Misc.isRegex(pattern)) {
      traversal.filter { node => node.myproperty != pattern }
    } else {
      overflowdb.traversal.filter.StringPropertyFilter.regexpNot(traversal)(_.myproperty, pattern)
    }
  }

  /** Traverse to nodes where myproperty does not match any of the regular expressions in `values`.
    */
  def mypropertyNot(patterns: String*): Iterator[NodeType] = {
    overflowdb.traversal.filter.StringPropertyFilter.regexpNotMultiple(traversal)(_.myproperty, patterns)
  }

}
