package com.siriusxm.example.service
import cats.effect.IO
import cats.effect.unsafe.implicits.global
import com.siriusxm.example.client.ProductsClient
import com.siriusxm.example.stubs.Stubs
import org.mockito.Mockito._
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.mockito.MockitoSugar
import org.slf4j.Logger

class ShoppingCartServiceSpec extends AnyWordSpec with Matchers with MockitoSugar with Stubs {
  private implicit val mockLogger: Logger = mock[Logger]

  val mockClient: ProductsClient = mock[ProductsClient]

  "addItems" should {
    "add items to the shopping cart" in {
      val shoppingCart = ShoppingCartService(mockClient)
      // Given
      val givenItems = List(cheerios, cornflakes, cornflakes, frosties)

      when(mockClient.getByName("cheerios.json")).thenReturn(IO(cheerios))
      when(mockClient.getByName("cornflakes.json")).thenReturn(IO(cornflakes))
      when(mockClient.getByName("frosties.json")).thenReturn(IO(frosties))
      when(mockClient.getByName("Weetabix.json")).thenReturn(IO(weetabix))

      // When
      val modifiedCart = shoppingCart.addItems(givenItems.map(_.title)).unsafeRunSync()

      // Then
      modifiedCart.listItems.unsafeRunSync() must contain allOf (cheerios -> 1, cornflakes.copy(price = cornflakes.price*2) -> 2, frosties -> 1)
      val totalAmount = givenItems.map(_.price).sum
      modifiedCart.calculateTotalCost.unsafeRunSync() mustBe "%.2f".format(totalAmount + totalAmount * 0.125).toDouble
    }
    "handle errors from ProductsClient" in {
      // Given
      val shoppingCart = ShoppingCartService(mockClient)
      val givenItems = List(cheerios, cornflakes, frosties)

      when(mockClient.getByName("cheerios.json")).thenReturn(IO.raiseError(new RuntimeException("Failed to fetch Cheerios")))
      when(mockClient.getByName("cornflakes.json")).thenReturn(IO(cornflakes))
      when(mockClient.getByName("frosties.json")).thenReturn(IO(frosties))

      // When
      val modifiedCart = shoppingCart.addItems(givenItems.map(_.title)).unsafeRunSync()

      // Then
      modifiedCart.listItems.unsafeRunSync() must contain only (cornflakes -> 1, frosties -> 1)
      val totalAmount = givenItems.filterNot(_.title == cheerios.title).map(_.price).sum
      modifiedCart.calculateTotalCost.unsafeRunSync() mustBe "%.2f".format(totalAmount + totalAmount * 0.125).toDouble
    }
  }
}
