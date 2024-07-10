package com.siriusxm.example.model

case class Item(title: String, price: Double) {
  override def toString: String = s"(title: $title, price: $$$price)"
}
