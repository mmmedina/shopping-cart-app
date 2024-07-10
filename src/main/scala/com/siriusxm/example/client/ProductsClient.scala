package com.siriusxm.example.client

import cats.effect.IO
import com.siriusxm.example.model.Item
import io.circe.generic.auto._
import io.circe.parser._
import org.http4s.blaze.client.BlazeClientBuilder
import org.http4s.client.Client
import org.http4s.implicits.http4sLiteralsSyntax
import org.slf4j.Logger

class ProductsClient(implicit log: Logger) {

  private def getProductInfo(client: Client[IO], str: String): IO[Item] =
    client
      .expect[String](uri"https://raw.githubusercontent.com/mattjanks16/shopping-cart-test-data/main" / str)
      .flatMap { responseBody =>
        decode[Item](responseBody) match {
          case Right(item) =>
            log.info(s"Received the product ${item.title} successfully")
            IO.pure(item)
          case Left(error) =>
            log.warn(s"Failed to parse Json. Error: ${error.getCause}")
            IO.raiseError(new RuntimeException(s"Failed to parse JSON: $error"))
        }
      }

  def getByName(productName: String): IO[Item] =
    BlazeClientBuilder[IO].resource
      .use(client => getProductInfo(client, productName))
}
