package io.joern

import io.circe.syntax.EncoderOps
import io.circe.{Decoder, Encoder, HCursor, Json}

package object codetidal5 {

  /** The inference response from the server.
    */
  case class InferenceResult(
    targetIdentifier: String,
    typ: String,
    confidence: Float,
    scope: String,
    alternatives: List[AlternativeResult]
  )

  implicit val decodeInferenceResult: Decoder[InferenceResult] = (c: HCursor) =>
    for {
      targetIdentifier <- c.downField("target_identifier").as[String]
      typ              <- c.downField("type").as[String]
      confidence       <- c.downField("confidence").as[Float]
      scope            <- c.downField("scope").as[String]
      alternatives     <- c.downField("alternatives").as[List[AlternativeResult]]
    } yield {
      InferenceResult(targetIdentifier, typ, confidence, scope, alternatives)
    }

  implicit val encodeInferenceResult: Encoder[InferenceResult] = Encoder.instance {
    case InferenceResult(targetIdentifier, typ, confidence, scope, alternatives) =>
      Json.obj(
        "target_identifier" -> targetIdentifier.asJson,
        "type"              -> typ.asJson,
        "confidence"        -> confidence.asJson,
        "scope"             -> scope.asJson,
        "alternatives"      -> alternatives.asJson
      )
  }

  /** Alternative type inference results.
    */
  case class AlternativeResult(typ: String, confidence: Float)

  implicit val decodeAlternativeResult: Decoder[AlternativeResult] = (c: HCursor) =>
    for {
      typ        <- c.downField("type").as[String]
      confidence <- c.downField("confidence").as[Float]
    } yield {
      AlternativeResult(typ, confidence)
    }

  implicit val encodeAlternativeResult: Encoder[AlternativeResult] = Encoder.instance {
    case AlternativeResult(typ, confidence) =>
      Json.obj("type" -> typ.asJson, "confidence" -> confidence.asJson)
  }

}
