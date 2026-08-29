from pathlib import Path

path = Path('app/src/main/java/com/llins95/comparadordecerveja/ui/BeerApp.kt')
text = path.read_text()


def replace_once(old: str, new: str, label: str):
    global text
    count = text.count(old)
    if count != 1:
        raise SystemExit(f'{label}: esperado 1 trecho, encontrado {count}')
    text = text.replace(old, new, 1)


replace_once(
    'import androidx.compose.material.icons.rounded.Inventory2\nimport androidx.compose.material.icons.rounded.Settings',
    'import androidx.compose.material.icons.rounded.Inventory2\nimport androidx.compose.material.icons.rounded.Search\nimport androidx.compose.material.icons.rounded.Settings',
    'import do ícone de busca',
)

replace_once(
    '''    var editingReturnTab by rememberSaveable { mutableIntStateOf(4) }
    var showSettings by rememberSaveable { mutableStateOf(false) }
    var settingsSection by rememberSaveable { mutableIntStateOf(0) }''',
    '''    var editingReturnTab by rememberSaveable { mutableIntStateOf(4) }
    var editingReturnToSearch by rememberSaveable { mutableStateOf(false) }
    var showSettings by rememberSaveable { mutableStateOf(false) }
    var showSearch by rememberSaveable { mutableStateOf(false) }
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var settingsSection by rememberSaveable { mutableIntStateOf(0) }''',
    'estados da busca',
)

replace_once(
    '''    BackHandler(enabled = showSettings) {
        if (settingsSection == 0) {
            showSettings = false
        } else {
            settingsSection = 0
        }
    }''',
    '''    BackHandler(enabled = showSettings || showSearch) {
        when {
            showSearch -> showSearch = false
            settingsSection == 0 -> showSettings = false
            else -> settingsSection = 0
        }
    }''',
    'voltar da busca',
)

replace_once(
    '''                            showSettings && settingsSection == 1 -> "Cadastro de embalagens"
                            showSettings && settingsSection == 2 -> "Cadastro de lojas"
                            showSettings -> "Configurações"
                            selectedTab == 1 && editingOfferId != null -> "Editar oferta"''',
    '''                            showSearch -> "Procurar promoção"
                            showSettings && settingsSection == 1 -> "Cadastro de embalagens"
                            showSettings && settingsSection == 2 -> "Cadastro de lojas"
                            showSettings -> "Configurações"
                            selectedTab == 1 && editingOfferId != null -> "Editar oferta"''',
    'título da busca',
)

replace_once(
    '''                navigationIcon = {
                    if (showSettings) {
                        IconButton(
                            onClick = {
                                if (settingsSection == 0) {
                                    showSettings = false
                                } else {
                                    settingsSection = 0
                                }
                            }
                        ) {
                            Icon(Icons.Rounded.ArrowBack, contentDescription = "Voltar")
                        }
                    }
                },''',
    '''                navigationIcon = {
                    if (showSettings || showSearch) {
                        IconButton(
                            onClick = {
                                when {
                                    showSearch -> showSearch = false
                                    settingsSection == 0 -> showSettings = false
                                    else -> settingsSection = 0
                                }
                            }
                        ) {
                            Icon(Icons.Rounded.ArrowBack, contentDescription = "Voltar")
                        }
                    }
                },''',
    'navegação da busca',
)

replace_once(
    '''                actions = {
                    if (!showSettings) {
                        IconButton(
                            onClick = {
                                settingsSection = 0
                                showSettings = true
                            }
                        ) {
                            Icon(Icons.Rounded.Settings, contentDescription = "Configurações")
                        }
                    }
                },''',
    '''                actions = {
                    if (!showSettings && !showSearch) {
                        IconButton(
                            onClick = {
                                searchQuery = ""
                                showSearch = true
                            }
                        ) {
                            Icon(Icons.Rounded.Search, contentDescription = "Procurar promoção")
                        }
                        IconButton(
                            onClick = {
                                settingsSection = 0
                                showSettings = true
                            }
                        ) {
                            Icon(Icons.Rounded.Settings, contentDescription = "Configurações")
                        }
                    }
                },''',
    'lupa ao lado de configurações',
)

replace_once(
    '''        bottomBar = {
            if (!showSettings) {''',
    '''        bottomBar = {
            if (!showSettings && !showSearch) {''',
    'ocultar navegação inferior na busca',
)

replace_once(
    '''            }
        } else when (selectedTab) {
            0 -> HomeScreen(''',
    '''            }
        } else if (showSearch) {
            SearchOffersScreen(
                offers = offers,
                padding = padding,
                query = searchQuery,
                onQueryChange = { searchQuery = it },
                onEdit = { offer ->
                    editingReturnTab = selectedTab
                    editingReturnToSearch = true
                    showSearch = false
                    editingOfferId = offer.id
                    selectedTab = 1
                },
            )
        } else when (selectedTab) {
            0 -> HomeScreen(''',
    'roteamento da tela de busca',
)

replace_once(
    '''                        selectedTab = editingReturnTab
                    }
                    editingOfferId = null
                },
                onCancel = if (editingOfferId != null) {
                    {
                        editingOfferId = null
                        selectedTab = editingReturnTab
                    }
                } else {''',
    '''                        selectedTab = editingReturnTab
                        if (editingReturnToSearch) {
                            showSearch = true
                            editingReturnToSearch = false
                        }
                    }
                    editingOfferId = null
                },
                onCancel = if (editingOfferId != null) {
                    {
                        editingOfferId = null
                        selectedTab = editingReturnTab
                        if (editingReturnToSearch) {
                            showSearch = true
                            editingReturnToSearch = false
                        }
                    }
                } else {''',
    'retorno para a busca após edição',
)

replace_once(
    '''                onEdit = { offer ->
                    editingReturnTab = 3
                    editingOfferId = offer.id
                    selectedTab = 1
                },''',
    '''                onEdit = { offer ->
                    editingReturnTab = 3
                    editingReturnToSearch = false
                    editingOfferId = offer.id
                    selectedTab = 1
                },''',
    'edição pelo simulador',
)

replace_once(
    '''                onEdit = { offer ->
                    editingReturnTab = 4
                    editingOfferId = offer.id
                    selectedTab = 1
                },''',
    '''                onEdit = { offer ->
                    editingReturnTab = 4
                    editingReturnToSearch = false
                    editingOfferId = offer.id
                    selectedTab = 1
                },''',
    'edição pelo histórico',
)

search_screen = r'''
@Composable
private fun SearchOffersScreen(
    offers: List<BeerOfferEntity>,
    padding: PaddingValues,
    query: String,
    onQueryChange: (String) -> Unit,
    onEdit: (BeerOfferEntity) -> Unit,
) {
    val normalizedQuery = query.trim()
    val filteredOffers = remember(offers, normalizedQuery) {
        val sortedOffers = offers.sortedByDescending { it.createdAt }
        if (normalizedQuery.isBlank()) {
            sortedOffers
        } else {
            sortedOffers.filter { offer ->
                val searchableText = buildString {
                    append(offer.brand)
                    append(' ')
                    append(offer.packageType)
                    append(' ')
                    append(offer.store)
                    append(' ')
                    append(offer.volumeMl)
                    append(" ml ")
                    append(offer.quantity)
                }
                searchableText.contains(normalizedQuery, ignoreCase = true)
            }
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(padding),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = query,
                    onValueChange = onQueryChange,
                    label = { Text("Buscar promoção") },
                    placeholder = { Text("Ex.: Brahma, lata, nome da loja...") },
                    leadingIcon = {
                        Icon(Icons.Rounded.Search, contentDescription = null)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = MaterialTheme.shapes.medium,
                )
                Text(
                    "Pesquise por marca, embalagem, loja, volume ou quantidade mínima.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        when {
            offers.isEmpty() -> {
                item { EmptyMessage("Nenhuma promoção cadastrada para pesquisar.") }
            }
            filteredOffers.isEmpty() -> {
                item { EmptyMessage("Nenhuma promoção encontrada para \"$normalizedQuery\".") }
            }
            else -> {
                item {
                    Text(
                        if (filteredOffers.size == 1) "1 promoção encontrada" else "${filteredOffers.size} promoções encontradas",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
                items(filteredOffers, key = { it.id }) { offer ->
                    ElevatedCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.large,
                        colors = CardDefaults.elevatedCardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                        ),
                    ) {
                        Column(
                            modifier = Modifier.padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(5.dp),
                        ) {
                            Text(
                                offer.brand,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                            )
                            Text(
                                offerDescription(offer),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            if (offer.store.isNotBlank()) {
                                Text(
                                    "Loja: ${offer.store}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Medium,
                                )
                            }
                            Text(
                                "${minimumPurchaseDescription(offer)} • ${formatPricePerLiter(offer.pricePerLiter)}",
                                style = MaterialTheme.typography.bodyMedium,
                            )
                            OutlinedButton(
                                onClick = { onEdit(offer) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = MaterialTheme.shapes.medium,
                            ) {
                                Icon(Icons.Rounded.Edit, contentDescription = null)
                                Spacer(Modifier.padding(horizontal = 4.dp))
                                Text("Editar promoção")
                            }
                        }
                    }
                }
            }
        }
    }
}

'''
replace_once(
    '@Composable\nprivate fun SimulatorScreen(',
    search_screen + '@Composable\nprivate fun SimulatorScreen(',
    'tela de busca',
)

path.write_text(text)
