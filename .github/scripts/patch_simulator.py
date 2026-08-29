from pathlib import Path

path = Path("app/src/main/java/com/llins95/comparadordecerveja/ui/BeerApp.kt")
text = path.read_text()


def replace_once(old: str, new: str, label: str) -> None:
    global text
    count = text.count(old)
    if count != 1:
        raise SystemExit(f"{label}: esperado 1 trecho, encontrado {count}")
    text = text.replace(old, new, 1)


replace_once(
    "    var editingOfferId by rememberSaveable { mutableStateOf<Long?>(null) }\n    var showSettings by rememberSaveable { mutableStateOf(false) }",
    "    var editingOfferId by rememberSaveable { mutableStateOf<Long?>(null) }\n    var editingReturnTab by rememberSaveable { mutableIntStateOf(4) }\n    var showSettings by rememberSaveable { mutableStateOf(false) }",
    "estado de retorno da edição",
)

if text.count("                        selectedTab = 4") != 2:
    raise SystemExit("retorno atual da edição não corresponde ao esperado")
text = text.replace("                        selectedTab = 4", "                        selectedTab = editingReturnTab")

replace_once(
    "            3 -> SimulatorScreen(offers, padding)",
    """            3 -> SimulatorScreen(
                offers = offers,
                padding = padding,
                onEdit = { offer ->
                    editingReturnTab = 3
                    editingOfferId = offer.id
                    selectedTab = 1
                },
            )""",
    "chamada do simulador",
)

replace_once(
    """                onEdit = { offer ->
                    editingOfferId = offer.id
                    selectedTab = 1
                },
                onDelete = { offer ->""",
    """                onEdit = { offer ->
                    editingReturnTab = 4
                    editingOfferId = offer.id
                    selectedTab = 1
                },
                onDelete = { offer ->""",
    "retorno da edição pelo histórico",
)

replace_once(
    "private fun SimulatorScreen(offers: List<BeerOfferEntity>, padding: PaddingValues) {",
    """private fun SimulatorScreen(
    offers: List<BeerOfferEntity>,
    padding: PaddingValues,
    onEdit: (BeerOfferEntity) -> Unit,
) {""",
    "assinatura do simulador",
)

replace_once(
    """                        Text(
                            \"Você consegue comprar ${formatUnits(units)} (${formatVolume(volume)})\",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )""",
    """                        Text(
                            \"Você consegue comprar ${formatUnits(units)} (${formatVolume(volume)})\",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                        OutlinedButton(
                            onClick = { onEdit(offer) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = MaterialTheme.shapes.medium,
                        ) {
                            Icon(Icons.Rounded.Edit, contentDescription = null)
                            Spacer(Modifier.padding(horizontal = 4.dp))
                            Text(\"Editar promoção\")
                        }""",
    "botão editar promoção",
)

path.write_text(text)
