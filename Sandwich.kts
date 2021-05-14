
sandwich {
    with type "compiled"
    bread = "ciabatta"

    fillings {
        +"compiledFilling"
    }

    filling("cheese")
    filling("ham")
    filling("tomato")

    dressings {
        +"Basil"
        +"Pepper"
        // // 'fun sideOrders(sideOrderToAdd: SideOrders.() -> Unit): Sandwich' can't be called in this context by implicit receiver. Use the explicit one if necessary
        // sideOrders {
        //     side("Green Salad")
        // }
    }
    dressing("Extra Hot")

    sideOrders {
        side("French Fries")
        side("Coca Cola 0.5")
    }
    sideOrders("dessert") {
        side("ice cream (vanilla)")
        side("chocolate cake")
    }
}
