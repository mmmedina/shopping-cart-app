package com.siriusxm.example.shoppingCart

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import com.siriusxm.example.client.ProductsClient
import com.siriusxm.example.model.Item
import org.mockito.Mockito._
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.mockito.MockitoSugar
import org.slf4j.Logger

class ShoppingCartSpec extends AnyWordSpec with Matchers with MockitoSugar {
  private implicit val mockLogger: Logger = mock[Logger]

  val mockClient: ProductsClient = mock[ProductsClient]

  val cheerios: Item = Item("Cheerios", 8.43)
  val cornflakes: Item = Item("Cornflakes", 2.52)
  val frosties: Item = Item("Frosties", 4.99)
  val weetabix: Item = Item("Weetabdsdix", 4.99)

  "addItems" should {
    "add items to the shopping cart" in {
      val shoppingCart = new ShoppingCart(mockClient)
      // Given
      val givenItems = List(cheerios, cornflakes, cornflakes, frosties)

      when(mockClient.getByName("cheerios.json")).thenReturn(IO(cheerios))
      when(mockClient.getByName("cornflakes.json")).thenReturn(IO(cornflakes))
      when(mockClient.getByName("frosties.json")).thenReturn(IO(frosties))
      when(mockClient.getByName("Weetabix.json")).thenReturn(IO(weetabix))

      // When
      shoppingCart.addItems(givenItems).unsafeRunSync()

      // Then
      shoppingCart.listItems.unsafeRunSync() must contain allOf (cheerios -> 1, cornflakes.copy(price = cornflakes.price*2) -> 2, frosties -> 1)
      val totalAmount = givenItems.map(_.price).sum
      shoppingCart.calculateTotalCost.unsafeRunSync() mustBe "%.2f".format(totalAmount + totalAmount * 0.125).toDouble
    }
    "handle errors from ProductsClient" in {
      // Given
      val shoppingCart = new ShoppingCart(mockClient)
      val givenItems = List(cheerios, cornflakes, frosties)

      when(mockClient.getByName("cheerios.json")).thenReturn(IO.raiseError(new RuntimeException("Failed to fetch Cheerios")))
      when(mockClient.getByName("cornflakes.json")).thenReturn(IO(cornflakes))
      when(mockClient.getByName("frosties.json")).thenReturn(IO(frosties))

      // When
      shoppingCart.addItems(givenItems).unsafeRunSync()

      // Then
      shoppingCart.listItems.unsafeRunSync() must contain only (cornflakes -> 1, frosties -> 1)
      val totalAmount = givenItems.filterNot(_.title == cheerios.title).map(_.price).sum
      shoppingCart.calculateTotalCost.unsafeRunSync() mustBe "%.2f".format(totalAmount + totalAmount * 0.125).toDouble
    }
  }
}
