import react.*
import react.dom.*
import kotlinext.js.*
import kotlinx.html.js.*
import kotlinx.coroutines.*
import kotlinx.datetime.internal.JSJoda.DateTimeFormatter
import kotlinx.datetime.internal.JSJoda.LocalDate

private val scope = MainScope()

val App = functionalComponent<RProps> { _ ->
    val (shoppingList, setShoppingList) = useState(emptyList<ShoppingListItem>())

    useEffect(dependencies = listOf()) {
        scope.launch {
            setShoppingList(getShoppingList())
        }
    }

    h1 {
        +"Full-Stack Shopping List"
    }
    ul {
        shoppingList.sortedByDescending(ShoppingListItem::priority).forEach { item ->
            li {
                key = item.toString()
                attrs.onClickFunction = {
                    scope.launch {
                        deleteShoppingListItem(item)
                        setShoppingList(getShoppingList())
                    }
                }
                +"[${item.priority}] ${item.desc}"
                if (item.deadline != null) {
                    div {
                        item.deadline.let {
                            val date = LocalDate.parse(it, DateTimeFormatter.ofPattern("dd-MM-yyyy"))
                            when {
                                date.isBefore(LocalDate.now()) ->
                                    this.attrs["class"] = "red"
                                date.isBefore(LocalDate.now().plusDays(1)) ->
                                    this.attrs["class"] = "orange"
                                else ->
                                    this.attrs["class"] = "green"
                            }
                        }
                        +"  deadline: ${item.deadline}"
                    }
                }
            }
        }
    }
    child(
        InputComponent,
        props = jsObject {
            onSubmit = { input ->
                val regexDeadline = Regex("\\d\\d-\\d\\d-\\d\\d\\d\\d")
                val deadline = regexDeadline.find(input)
                if (deadline != null) {
                    input.replace("-", "")
                }
                val cartItem = ShoppingListItem(
                        input.replace("!", ""),
                        input.count { it == '!' },
                        deadline?.value
                )
                scope.launch {
                    addShoppingListItem(cartItem)
                    setShoppingList(getShoppingList())
                }
            }
        }
    )
    h3 {
        + "Current number of products: ${shoppingList.size}"
    }
    button {
        + "Clear"
        attrs.onClickFunction = {
            shoppingList.forEach { item ->
                scope.launch {
                    deleteShoppingListItem(item)
                    setShoppingList(getShoppingList())
                }
            }
        }
    }
}

