package com.siriusxm.example.shoppingCart

import cats.effect.IO
import cats.implicits._
import com.siriusxm.example.client.ProductsClient
import com.siriusxm.example.model.Item
import scala.collection.mutable.ListBuffer
import org.slf4j.Logger

class ShoppingCart(productClient: ProductsClient)(implicit val log: Logger) {
  private val items: ListBuffer[Item] = ListBuffer.empty

  def addItems(givenItems: List[Item]): IO[Unit] = {
    log.info("Adding items...")
    givenItems.traverse { item =>
      productClient
        .getByName(s"${item.title.toLowerCase}.json")
        .redeem(
          error => IO.pure(log.error(s"Error trying to add ${item.title} to the cart. Message: ${error.getMessage}")),
          item =>
            IO.pure {
              log.info(s"Adding item ${item} to the cart")
              items += item
            }
        )
    }
  }.void

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
