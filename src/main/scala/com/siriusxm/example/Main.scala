package com.siriusxm.example

import cats.effect.{IO, IOApp}
import com.siriusxm.example.client.ProductsClient
import com.siriusxm.example.shoppingCart.ShoppingCart
import com.siriusxm.example.model.Item
import org.slf4j.{Logger, LoggerFactory}

object ShoppingCartApp extends IOApp.Simple {
  private implicit val log: Logger = LoggerFactory.getLogger(getClass)
  def initializeShoppingCartAndClient: IO[(ShoppingCart, ProductsClient)] = IO{
      val productsClient = new ProductsClient
      val shoppingCart = new ShoppingCart(productsClient)
      (shoppingCart, productsClient)
  }

  override def run: IO[Unit] = {
    initializeShoppingCartAndClient.flatMap { case (shoppingCart, _) =>
      // Example usage:
      val itemsToAdd = List(
        Item("Cheerios", 3.99),
        Item("Cornflakes", 2.99),
        Item("Cornflakes", 2.99),
        Item("Frosties", 4.49),
        Item("Shreddies", 3.79)
      )
      shoppingCart.addItems(itemsToAdd).flatMap { _ =>
        shoppingCart.listItems.flatMap { items =>
          shoppingCart.calculateTotalCost.flatMap { totalCost =>
            IO {
              println(s"Items in the cart: $items")
              println(s"Total cost with 12.5% surcharge: ${"%.2f".format(totalCost)}")
            }
          }
        }
      }
    }
  }
}
