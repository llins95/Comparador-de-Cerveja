package com.llins95.comparadordecerveja.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AddCircle
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Calculate
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.SystemUpdate
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.llins95.comparadordecerveja.BuildConfig
import com.llins95.comparadordecerveja.data.BeerOfferEntity
import com.llins95.comparadordecerveja.data.PackageSizeOption
import com.llins95.comparadordecerveja.domain.BeerPriceCalculator
import java.text.DateFormat
import java.text.NumberFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.launch

private val ptBr = Locale("pt", "BR")
private val currencyFormatter: NumberFormat = NumberFormat.getCurrencyInstance(ptBr)
private val dateFormatter: DateFormat = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, ptBr)

private data class AppTab(
    val title: String,
    val icon: @Composable () -> Unit
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BeerApp(
    viewModel: BeerViewModel,
    settingsViewModel: SettingsViewModel,
    onOpenUpdate: () -> Unit,
) {
    val offers by viewModel.offers.collectAsStateWithLifecycle()
    val packageSizes by settingsViewModel.packageSizes.collectAsStateWithLifecycle()
    val stores by settingsViewModel.stores.collectAsStateWithLifecycle()
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    var editingOfferId by rememberSaveable { mutableStateOf<Long?>(null) }
    var showSettings by rememberSaveable { mutableStateOf(false) }
    val editingOffer = offers.firstOrNull { it.id == editingOfferId }
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    val tabs = listOf(
        AppTab("Início") { Icon(Icons.Rounded.Home, contentDescription = null) },
        AppTab("Adicionar") { Icon(Icons.Rounded.AddCircle, contentDescription = null) },
        AppTab("Ranking") { Icon(Icons.Rounded.EmojiEvents, contentDescription = null) },
        AppTab("Simular") { Icon(Icons.Rounded.Calculate, contentDescription = null) },
        AppTab("Histórico") { Icon(Icons.Rounded.History, contentDescription = null) }
    )

    BackHandler(enabled = showSettings) {
        showSettings = false
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.surface,
        contentWindowInsets = WindowInsets.safeDrawing,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = when {
                            showSettings -> "Configurações"
                            selectedTab == 1 && editingOfferId != null -> "Editar oferta"
                            else -> tabs[selectedTab].title
                        },
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                navigationIcon = {
                    if (showSettings) {
                        IconButton(onClick = { showSettings = false }) {
                            Icon(Icons.Rounded.ArrowBack, contentDescription = "Voltar")
                        }
                    }
                },
                actions = {
                    if (!showSettings) {
                        IconButton(onClick = { showSettings = true }) {
                            Icon(Icons.Rounded.Settings, contentDescription = "Configurações")
                        }
                    }
                },
            )
        },
        bottomBar = {
            if (!showSettings) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                    tonalElevation = 0.dp
                ) {
                    tabs.forEachIndexed { index, tab ->
                        NavigationBarItem(
                            selected = selectedTab == index,
                            onClick = {
                                editingOfferId = null
                                selectedTab = index
                            },
                            icon = tab.icon,
                            label = { Text(tab.title, style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }
            }
        }
    ) { padding ->
        if (showSettings) {
            SettingsScreen(
                padding = padding,
                packageSizes = packageSizes,
                stores = stores,
                onOpenUpdate = onOpenUpdate,
                onAddPackageSize = settingsViewModel::addPackageSize,
                onDeletePackageSize = settingsViewModel::deletePackageSize,
                onAddStore = settingsViewModel::addStore,
                onDeleteStore = settingsViewModel::deleteStore,
            )
        } else when (selectedTab) {
            0 -> HomeScreen(
                offers,
                padding,
                onAdd = {
                    editingOfferId = null
                    selectedTab = 1
                }
            )
            1 -> AddOfferScreen(
                padding = padding,
                initialOffer = editingOffer,
                packageSizes = packageSizes,
                stores = stores,
                onSave = { brand, packageType, volume, quantity, price, store, returnable ->
                    if (editingOffer == null) {
                        viewModel.addOffer(brand, packageType, volume, quantity, price, store, returnable)
                        selectedTab = 0
                    } else {
                        viewModel.updateOffer(
                            editingOffer,
                            brand,
                            packageType,
                            volume,
                            quantity,
                            price,
                            store,
                            returnable
                        )
                        selectedTab = 4
                    }
                    editingOfferId = null
                },
                onCancel = if (editingOfferId != null) {
                    {
                        editingOfferId = null
                        selectedTab = 4
                    }
                } else {
                    null
                }
            )
            2 -> RankingScreen(offers, padding)
            3 -> SimulatorScreen(offers, padding)
            else -> HistoryScreen(
                offers = offers,
                padding = padding,
                onEdit = { offer ->
                    editingOfferId = offer.id
                    selectedTab = 1
                },
                onDelete = { offer ->
                    coroutineScope.launch {
                        viewModel.deleteOffer(offer)
                        val result = snackbarHostState.showSnackbar(
                            message = "Oferta de ${offer.brand} excluída",
                            actionLabel = "Desfazer",
                            withDismissAction = true,
                            duration = SnackbarDuration.Long
                        )
                        if (result == SnackbarResult.ActionPerformed) {
                            viewModel.restoreOffer(offer)
                        }
                    }
                }
            )
        }
    }
}

@Composable
private fun HomeScreen(
    offers: List<BeerOfferEntity>,
    padding: PaddingValues,
    onAdd: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(padding),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    "Compare e economize",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "Veja rapidamente qual oferta entrega mais cerveja pelo menor preço.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        if (offers.isEmpty()) {
            item {
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.extraLarge
                ) {
                    Column(
                        Modifier.padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Text("Nenhum preço cadastrado", style = MaterialTheme.typography.titleLarge)
                        Text(
                            "Adicione uma oferta para calcular automaticamente o preço por litro e montar o ranking.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        FilledTonalButton(onClick = onAdd) {
                            Icon(Icons.Rounded.AddCircle, contentDescription = null)
                            Spacer(Modifier.padding(horizontal = 4.dp))
                            Text("Adicionar primeiro preço")
                        }
                    }
                }
            }
        } else {
            val cheapest = offers.first()
            item {
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.extraLarge,
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(
                        Modifier.padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            "MELHOR PREÇO AGORA",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            "🥇 ${cheapest.brand}",
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            offerDescription(cheapest),
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            formatPricePerLiter(cheapest.pricePerLiter),
                            style = MaterialTheme.typography.displaySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Oferta: ${currencyFormatter.format(cheapest.totalPrice)}${storeSuffix(cheapest)}",
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }

            item {
                Text(
                    "Top 5 ofertas",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold
                )
            }
            items(offers.take(5), key = { it.id }) { offer ->
                OfferRow(offer)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddOfferScreen(
    padding: PaddingValues,
    initialOffer: BeerOfferEntity? = null,
    packageSizes: List<PackageSizeOption>,
    stores: List<String>,
    onSave: (String, String, Int, Int, Double, String, Boolean) -> Unit,
    onCancel: (() -> Unit)? = null
) {
    var brand by rememberSaveable(initialOffer?.id) { mutableStateOf(initialOffer?.brand.orEmpty()) }
    val defaultPackage = packageSizes.firstOrNull()
    var packageType by rememberSaveable(initialOffer?.id) {
        mutableStateOf(initialOffer?.packageType ?: defaultPackage?.name.orEmpty())
    }
    var volume by rememberSaveable(initialOffer?.id) {
        mutableStateOf(
            initialOffer?.volumeMl?.toString()
                ?: defaultPackage?.volumeMl?.toString().orEmpty()
        )
    }
    var quantity by rememberSaveable(initialOffer?.id) {
        mutableStateOf(initialOffer?.quantity?.toString() ?: "1")
    }
    var price by rememberSaveable(initialOffer?.id) {
        mutableStateOf(initialOffer?.totalPrice?.let { String.format(ptBr, "%.2f", it) }.orEmpty())
    }
    var store by rememberSaveable(initialOffer?.id) { mutableStateOf(initialOffer?.store.orEmpty()) }
    var returnable by rememberSaveable(initialOffer?.id) {
        mutableStateOf(initialOffer?.hasReturnableBottle ?: false)
    }
    var packageMenuExpanded by remember { mutableStateOf(false) }
    var storeMenuExpanded by remember { mutableStateOf(false) }

    val volumeValue = volume.toIntOrNull()
    val quantityValue = quantity.toIntOrNull()
    val priceValue = price.replace(',', '.').toDoubleOrNull()
    val isValid = brand.isNotBlank() && (volumeValue ?: 0) > 0 && (quantityValue ?: 0) > 0 && (priceValue ?: 0.0) > 0

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(padding),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text(
                if (initialOffer == null) "Informe os dados da oferta" else "Atualize os dados da oferta",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                if (initialOffer == null) {
                    "Quantidade e volume entram automaticamente no cálculo do preço por litro."
                } else {
                    "As alterações atualizarão automaticamente o preço por litro e o ranking."
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        item { OutlinedTextField(brand, { brand = it }, label = { Text("Marca") }, modifier = Modifier.fillMaxWidth(), singleLine = true, shape = MaterialTheme.shapes.medium) }
        item {
            ExposedDropdownMenuBox(
                expanded = packageMenuExpanded,
                onExpandedChange = {
                    if (packageSizes.isNotEmpty()) packageMenuExpanded = !packageMenuExpanded
                },
            ) {
                OutlinedTextField(
                    value = if (packageType.isBlank() || volumeValue == null) {
                        "Nenhuma embalagem configurada"
                    } else {
                        packageOptionLabel(packageType, volumeValue)
                    },
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Embalagem") },
                    modifier = Modifier
                        .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                        .fillMaxWidth(),
                    singleLine = true,
                    shape = MaterialTheme.shapes.medium,
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = packageMenuExpanded)
                    },
                )
                ExposedDropdownMenu(
                    expanded = packageMenuExpanded,
                    onDismissRequest = { packageMenuExpanded = false },
                ) {
                    packageSizes.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(packageOptionLabel(option.name, option.volumeMl)) },
                            onClick = {
                                packageType = option.name
                                volume = option.volumeMl.toString()
                                packageMenuExpanded = false
                            },
                        )
                    }
                }
            }
            if (packageSizes.isEmpty()) {
                Text(
                    "Cadastre pelo menos uma embalagem em Configurações.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(start = 16.dp, top = 4.dp),
                )
            }
        }
        item {
            OutlinedTextField(
                quantity,
                { quantity = it.filter(Char::isDigit) },
                label = { Text("Quantidade de unidades") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = MaterialTheme.shapes.medium,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
        }
        item {
            OutlinedTextField(
                price,
                { price = it.filter { ch -> ch.isDigit() || ch == ',' || ch == '.' } },
                label = { Text("Preço total (R$)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = MaterialTheme.shapes.medium,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
            )
        }
        item {
            ExposedDropdownMenuBox(
                expanded = storeMenuExpanded,
                onExpandedChange = { storeMenuExpanded = !storeMenuExpanded },
            ) {
                OutlinedTextField(
                    value = store.ifBlank { "Sem loja" },
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Loja (opcional)") },
                    modifier = Modifier
                        .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                        .fillMaxWidth(),
                    singleLine = true,
                    shape = MaterialTheme.shapes.medium,
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = storeMenuExpanded)
                    },
                )
                ExposedDropdownMenu(
                    expanded = storeMenuExpanded,
                    onDismissRequest = { storeMenuExpanded = false },
                ) {
                    DropdownMenuItem(
                        text = { Text("Sem loja") },
                        onClick = {
                            store = ""
                            storeMenuExpanded = false
                        },
                    )
                    stores.forEach { configuredStore ->
                        DropdownMenuItem(
                            text = { Text(configuredStore) },
                            onClick = {
                                store = configuredStore
                                storeMenuExpanded = false
                            },
                        )
                    }
                }
            }
            if (stores.isEmpty()) {
                Text(
                    "Você pode cadastrar lojas em Configurações.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 16.dp, top = 4.dp),
                )
            }
        }
        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.large,
                color = MaterialTheme.colorScheme.surfaceContainerLow
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(checked = returnable, onCheckedChange = { returnable = it })
                    Column {
                        Text("Vasilhame retornável", fontWeight = FontWeight.Medium)
                        Text(
                            "Marque se o preço inclui o vasilhame.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
        if (isValid) {
            item {
                val perLiter = BeerPriceCalculator.pricePerLiter(priceValue!!, volumeValue!!, quantityValue!!)
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.extraLarge,
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Column(
                        Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text("Prévia da comparação", fontWeight = FontWeight.Bold)
                        Text("Volume total: ${formatVolume(BeerPriceCalculator.totalVolumeMl(volumeValue, quantityValue).toLong())}")
                        Text(
                            formatPricePerLiter(perLiter),
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    enabled = isValid,
                    onClick = {
                        onSave(brand, packageType, volumeValue!!, quantityValue!!, priceValue!!, store, returnable)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.large
                ) {
                    Text(if (initialOffer == null) "Salvar oferta" else "Salvar alterações")
                }
                if (onCancel != null) {
                    OutlinedButton(
                        onClick = onCancel,
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.large
                    ) {
                        Text("Cancelar edição")
                    }
                }
            }
        }
    }
}

@Composable
private fun RankingScreen(offers: List<BeerOfferEntity>, padding: PaddingValues) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(padding),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Text(
                "Do menor para o maior preço por litro",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        if (offers.isEmpty()) {
            item { EmptyMessage("Adicione ofertas para gerar o ranking.") }
        } else {
            items(offers, key = { it.id }) { offer ->
                val rank = offers.indexOfFirst { it.id == offer.id } + 1
                val isWinner = rank == 1
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.large,
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = if (isWinner) MaterialTheme.colorScheme.primaryContainer
                        else MaterialTheme.colorScheme.surfaceContainerLow
                    )
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(18.dp),
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = MaterialTheme.shapes.medium,
                            color = if (isWinner) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.secondaryContainer
                        ) {
                            Text(
                                if (isWinner) "🥇" else "#$rank",
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (isWinner) MaterialTheme.colorScheme.onPrimary
                                else MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                        Column(Modifier.weight(1f)) {
                            Text(offer.brand, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text(
                                offerDescription(offer),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        PricePill(offer.pricePerLiter)
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsScreen(
    padding: PaddingValues,
    packageSizes: List<PackageSizeOption>,
    stores: List<String>,
    onOpenUpdate: () -> Unit,
    onAddPackageSize: (String, Int) -> Boolean,
    onDeletePackageSize: (PackageSizeOption) -> Unit,
    onAddStore: (String) -> Boolean,
    onDeleteStore: (String) -> Unit,
) {
    var packageName by rememberSaveable { mutableStateOf("") }
    var packageVolume by rememberSaveable { mutableStateOf("") }
    var packageError by rememberSaveable { mutableStateOf<String?>(null) }
    var storeName by rememberSaveable { mutableStateOf("") }
    var storeError by rememberSaveable { mutableStateOf<String?>(null) }
    val packageVolumeValue = packageVolume.toIntOrNull()

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(padding),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.extraLarge,
                colors = CardDefaults.elevatedCardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                ),
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        "Atualização do aplicativo",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                    Text(
                        "Versão instalada: ${BuildConfig.VERSION_NAME}",
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                    FilledTonalButton(onClick = onOpenUpdate) {
                        Icon(Icons.Rounded.SystemUpdate, contentDescription = null)
                        Spacer(Modifier.padding(horizontal = 4.dp))
                        Text("Verificar atualizações")
                    }
                }
            }
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    "Tamanhos das embalagens",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    "Cadastre o tipo e o volume que aparecerão no campo Embalagem ao adicionar uma oferta.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        item {
            OutlinedTextField(
                value = packageName,
                onValueChange = {
                    packageName = it
                    packageError = null
                },
                label = { Text("Tipo da embalagem") },
                placeholder = { Text("Ex.: Lata ou Latão") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = MaterialTheme.shapes.medium,
            )
        }
        item {
            OutlinedTextField(
                value = packageVolume,
                onValueChange = {
                    packageVolume = it.filter(Char::isDigit)
                    packageError = null
                },
                label = { Text("Volume (ml)") },
                placeholder = { Text("Ex.: 350 ou 473") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = MaterialTheme.shapes.medium,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                isError = packageError != null,
                supportingText = packageError?.let { message ->
                    { Text(message) }
                },
            )
        }
        item {
            Button(
                onClick = {
                    val added = onAddPackageSize(packageName, packageVolumeValue!!)
                    if (added) {
                        packageName = ""
                        packageVolume = ""
                        packageError = null
                    } else {
                        packageError = "Essa embalagem já está cadastrada."
                    }
                },
                enabled = packageName.isNotBlank() && (packageVolumeValue ?: 0) > 0,
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.large,
            ) {
                Text("Adicionar embalagem")
            }
        }
        if (packageSizes.isEmpty()) {
            item { EmptyMessage("Nenhuma embalagem cadastrada.") }
        } else {
            items(packageSizes, key = { "${it.name.lowercase(ptBr)}-${it.volumeMl}" }) { option ->
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.large,
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                    ),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            packageOptionLabel(option.name, option.volumeMl),
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium,
                        )
                        IconButton(onClick = { onDeletePackageSize(option) }) {
                            Icon(Icons.Rounded.Delete, contentDescription = "Excluir embalagem")
                        }
                    }
                }
            }
        }

        item {
            Spacer(Modifier.height(4.dp))
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    "Lojas",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    "As lojas cadastradas aparecerão no campo Loja ao adicionar uma oferta.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        item {
            OutlinedTextField(
                value = storeName,
                onValueChange = {
                    storeName = it
                    storeError = null
                },
                label = { Text("Nome da loja") },
                placeholder = { Text("Ex.: Mercado Central") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = MaterialTheme.shapes.medium,
                isError = storeError != null,
                supportingText = storeError?.let { message ->
                    { Text(message) }
                },
            )
        }
        item {
            Button(
                onClick = {
                    val added = onAddStore(storeName)
                    if (added) {
                        storeName = ""
                        storeError = null
                    } else {
                        storeError = "Essa loja já está cadastrada."
                    }
                },
                enabled = storeName.isNotBlank(),
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.large,
            ) {
                Text("Adicionar loja")
            }
        }
        if (stores.isEmpty()) {
            item { EmptyMessage("Nenhuma loja cadastrada.") }
        } else {
            items(stores, key = { it.lowercase(ptBr) }) { store ->
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.large,
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                    ),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            store,
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium,
                        )
                        IconButton(onClick = { onDeleteStore(store) }) {
                            Icon(Icons.Rounded.Delete, contentDescription = "Excluir loja")
                        }
                    }
                }
            }
        }
        item {
            Text(
                "Excluir uma embalagem ou loja daqui não altera as ofertas antigas.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(vertical = 8.dp),
            )
        }
    }
}

@Composable
private fun SimulatorScreen(offers: List<BeerOfferEntity>, padding: PaddingValues) {
    var budgetText by rememberSaveable { mutableStateOf("30") }
    val budget = budgetText.replace(',', '.').toDoubleOrNull() ?: 0.0
    val results = remember(offers, budget) {
        offers.map { offer ->
            Triple(
                offer,
                BeerPriceCalculator.purchasableVolumeMl(
                    budget,
                    offer.totalPrice,
                    offer.volumeMl,
                    offer.quantity,
                ),
                BeerPriceCalculator.purchasableUnits(
                    budget,
                    offer.totalPrice,
                    offer.quantity,
                ),
            )
        }.filter { it.second > 0 }.sortedByDescending { it.second }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(padding),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                "Quanto rende seu dinheiro?",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                "Informe um orçamento e veja qual oferta entrega o maior volume.",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        item {
            OutlinedTextField(
                value = budgetText,
                onValueChange = { budgetText = it.filter { ch -> ch.isDigit() || ch == ',' || ch == '.' } },
                label = { Text("Orçamento (R$)") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                shape = MaterialTheme.shapes.medium
            )
        }
        if (offers.isEmpty()) {
            item { EmptyMessage("Adicione ofertas para usar o simulador.") }
        } else if (results.isEmpty()) {
            item { EmptyMessage("Com esse valor ainda não é possível comprar nenhuma oferta cadastrada.") }
        } else {
            item {
                Text(
                    "Mais cerveja pelo orçamento",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
            items(results, key = { it.first.id }) { (offer, volume, units) ->
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.large
                ) {
                    Column(
                        Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(offer.brand, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text(offerDescription(offer), color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            "Você consegue comprar ${formatUnits(units)} (${formatVolume(volume)})",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HistoryScreen(
    offers: List<BeerOfferEntity>,
    padding: PaddingValues,
    onEdit: (BeerOfferEntity) -> Unit,
    onDelete: (BeerOfferEntity) -> Unit
) {
    val history = remember(offers) { offers.sortedByDescending { it.createdAt } }
    var offerPendingDeletion by remember { mutableStateOf<BeerOfferEntity?>(null) }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(padding),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Text(
                "Preços cadastrados recentemente",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        if (history.isEmpty()) {
            item { EmptyMessage("Seu histórico de preços aparecerá aqui.") }
        }
        items(history, key = { it.id }) { offer ->
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.large
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        Text(offer.brand, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text(
                            offerDescription(offer),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text("${currencyFormatter.format(offer.totalPrice)} • ${formatPricePerLiter(offer.pricePerLiter)}")
                        Text(
                            dateFormatter.format(Date(offer.createdAt)),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Row {
                        IconButton(onClick = { onEdit(offer) }) {
                            Icon(Icons.Rounded.Edit, contentDescription = "Editar")
                        }
                        IconButton(onClick = { offerPendingDeletion = offer }) {
                            Icon(Icons.Rounded.Delete, contentDescription = "Excluir")
                        }
                    }
                }
            }
        }
    }

    offerPendingDeletion?.let { offer ->
        AlertDialog(
            onDismissRequest = { offerPendingDeletion = null },
            icon = { Icon(Icons.Rounded.Delete, contentDescription = null) },
            title = { Text("Excluir oferta?") },
            text = {
                Text(
                    "A oferta de ${offer.brand} será removida do Histórico, Início, Ranking e Simulador."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        offerPendingDeletion = null
                        onDelete(offer)
                    }
                ) {
                    Text("Excluir")
                }
            },
            dismissButton = {
                TextButton(onClick = { offerPendingDeletion = null }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
private fun OfferRow(offer: BeerOfferEntity) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Column(Modifier.weight(1f)) {
                Text(offer.brand, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text(
                    offerDescription(offer),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            PricePill(offer.pricePerLiter)
        }
    }
}

@Composable
private fun PricePill(pricePerLiter: Double) {
    Surface(
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.secondaryContainer
    ) {
        Text(
            formatPricePerLiter(pricePerLiter),
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            color = MaterialTheme.colorScheme.onSecondaryContainer,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun EmptyMessage(message: String) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge
    ) {
        Text(
            message,
            modifier = Modifier.padding(24.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private fun offerDescription(offer: BeerOfferEntity): String {
    val pack = if (offer.quantity > 1) "${offer.quantity} × ${offer.volumeMl} ml" else "${offer.volumeMl} ml"
    val returnable = if (offer.hasReturnableBottle) " • com vasilhame" else ""
    return "${offer.packageType} • $pack • ${formatVolume(offer.totalVolumeMl.toLong())}$returnable"
}

private fun storeSuffix(offer: BeerOfferEntity): String = if (offer.store.isBlank()) "" else " • ${offer.store}"

private fun formatPricePerLiter(value: Double): String = "${currencyFormatter.format(value)}/L"

private fun packageOptionLabel(name: String, volumeMl: Int): String =
    "$name • ${formatVolume(volumeMl.toLong())}"

private fun formatUnits(units: Long): String =
    if (units == 1L) "1 embalagem" else "$units embalagens"

private fun formatVolume(volumeMl: Long): String = when {
    volumeMl >= 1000 -> String.format(ptBr, "%.2f L", volumeMl / 1000.0)
    else -> "$volumeMl ml"
}
