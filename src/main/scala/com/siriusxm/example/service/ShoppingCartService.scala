package com.siriusxm.example.service

import cats.effect.IO
import cats.implicits._
import com.siriusxm.example.client.ProductsClient
import com.siriusxm.example.model.Item
import org.slf4j.Logger

case class ShoppingCartService(productClient: ProductsClient, items: List[Item] = List.empty)(implicit
    val log: Logger
) {

  def addItems(givenItems: List[String]): IO[ShoppingCartService] = {
    log.info(s"Adding items... $givenItems")

    givenItems
      .traverse { title =>
        productClient.getByName(s"${title.toLowerCase}.json").attempt.flatMap {
          case Left(error)    =>
            IO {
              log.warn(s"Error trying to add $title to the cart. Message: ${error.getMessage}")
              None
            }
          case Right(product) =>
            IO {
              log.info(s"Adding item $product to the cart")
              Some(product)
            }
        }
      }
      .map { results => this.copy(items = items ++ results.flatten) }
  }

  def listItems: IO[Map[Item, Int]] = IO {
    log.info("Getting the current items in the shopping cart")
    items.groupBy(_.title).map { case (name, items) =>
      val totalAmount = items.size
      val totalPrice  = items.map(_.price).sum
      Item(name, totalPrice) -> totalAmount
    }
  }

  def calculateTotalCost: IO[Double] = IO {
    log.info("Calculating the total cost of the cart")
    val subtotal  = items.map(_.price).sum
    val surcharge = subtotal * 0.125 // 12.5% surcharge
    val result    = "%.2f".format(subtotal + surcharge).toDouble
    log.info(s"Total: $$$result")
    result
  }
}
