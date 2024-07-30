package com.siriusxm.example

import cats.effect.{IO, IOApp}
import com.siriusxm.example.client.ProductsClient
import com.siriusxm.example.service.ShoppingCartService
import org.slf4j.{Logger, LoggerFactory}

object Main extends IOApp.Simple {
  private implicit val log: Logger                                               = LoggerFactory.getLogger(getClass)
  def initializeShoppingCartAndClient: IO[(ShoppingCartService, ProductsClient)] = IO {
    val productsClient = new ProductsClient
    val shoppingCart   = ShoppingCartService(productsClient)
    (shoppingCart, productsClient)
  }

  override def run: IO[Unit] = {
    initializeShoppingCartAndClient.flatMap { case (shoppingCart, _) =>
      val itemsToAdd = List("Cheerios", "Cornflakes", "BANANA", "Frosties", "Shreddies")

      for {
        modifiedCart <- shoppingCart.addItems(itemsToAdd)
        items        <- modifiedCart.listItems
        totalCost    <- modifiedCart.calculateTotalCost
      } yield {
        println(s"Items in the cart: $items")
        println(s"Total cost with 12.5% surcharge: ${"%.2f".format(totalCost)}")
      }
    }
  }
}
