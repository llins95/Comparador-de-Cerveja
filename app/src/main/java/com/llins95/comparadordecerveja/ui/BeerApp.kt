package com.llins95.comparadordecerveja.ui

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
import androidx.compose.material.icons.rounded.Calculate
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.llins95.comparadordecerveja.data.BeerOfferEntity
import com.llins95.comparadordecerveja.domain.BeerPriceCalculator
import java.text.DateFormat
import java.text.NumberFormat
import java.util.Date
import java.util.Locale

private val ptBr = Locale("pt", "BR")
private val currencyFormatter: NumberFormat = NumberFormat.getCurrencyInstance(ptBr)
private val dateFormatter: DateFormat = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, ptBr)

private data class AppTab(
    val title: String,
    val icon: @Composable () -> Unit
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BeerApp(viewModel: BeerViewModel) {
    val offers by viewModel.offers.collectAsStateWithLifecycle()
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }

    val tabs = listOf(
        AppTab("Início") { Icon(Icons.Rounded.Home, contentDescription = null) },
        AppTab("Adicionar") { Icon(Icons.Rounded.AddCircle, contentDescription = null) },
        AppTab("Ranking") { Icon(Icons.Rounded.EmojiEvents, contentDescription = null) },
        AppTab("Simular") { Icon(Icons.Rounded.Calculate, contentDescription = null) },
        AppTab("Histórico") { Icon(Icons.Rounded.History, contentDescription = null) }
    )

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            TopAppBar(title = { Text(tabs[selectedTab].title) })
        },
        bottomBar = {
            NavigationBar {
                tabs.forEachIndexed { index, tab ->
                    NavigationBarItem(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        icon = tab.icon,
                        label = { Text(tab.title) }
                    )
                }
            }
        }
    ) { padding ->
        when (selectedTab) {
            0 -> HomeScreen(offers, padding, onAdd = { selectedTab = 1 })
            1 -> AddOfferScreen(
                padding = padding,
                onSave = { brand, packageType, volume, quantity, price, store, returnable ->
                    viewModel.addOffer(brand, packageType, volume, quantity, price, store, returnable)
                    selectedTab = 0
                }
            )
            2 -> RankingScreen(offers, padding)
            3 -> SimulatorScreen(offers, padding)
            else -> HistoryScreen(offers, padding, onDelete = viewModel::deleteOffer)
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
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (offers.isEmpty()) {
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Nenhum preço cadastrado", style = MaterialTheme.typography.titleLarge)
                        Text("Adicione uma oferta para o app calcular automaticamente o preço por litro e montar o ranking.")
                        Button(onClick = onAdd) { Text("Adicionar primeiro preço") }
                    }
                }
            }
        } else {
            val cheapest = offers.first()
            item {
                Text("Mais barata agora", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(20.dp)) {
                        Text("🥇 ${cheapest.brand}", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                        Text(offerDescription(cheapest))
                        Spacer(Modifier.height(8.dp))
                        Text(
                            formatPricePerLiter(cheapest.pricePerLiter),
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                        Text("Oferta: ${currencyFormatter.format(cheapest.totalPrice)}${storeSuffix(cheapest)}")
                    }
                }
            }
            item { Text("Top 5", style = MaterialTheme.typography.titleMedium) }
            items(offers.take(5)) { offer -> OfferRow(offer) }
        }
    }
}

@Composable
private fun AddOfferScreen(
    padding: PaddingValues,
    onSave: (String, String, Int, Int, Double, String, Boolean) -> Unit
) {
    var brand by rememberSaveable { mutableStateOf("") }
    var packageType by rememberSaveable { mutableStateOf("Lata") }
    var volume by rememberSaveable { mutableStateOf("") }
    var quantity by rememberSaveable { mutableStateOf("1") }
    var price by rememberSaveable { mutableStateOf("") }
    var store by rememberSaveable { mutableStateOf("") }
    var returnable by rememberSaveable { mutableStateOf(false) }

    val volumeValue = volume.toIntOrNull()
    val quantityValue = quantity.toIntOrNull()
    val priceValue = price.replace(',', '.').toDoubleOrNull()
    val isValid = brand.isNotBlank() && (volumeValue ?: 0) > 0 && (quantityValue ?: 0) > 0 && (priceValue ?: 0.0) > 0

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(padding),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { Text("Cadastre o preço encontrado. Quantidade e volume serão usados no cálculo.") }
        item { OutlinedTextField(brand, { brand = it }, label = { Text("Marca") }, modifier = Modifier.fillMaxWidth(), singleLine = true) }
        item { OutlinedTextField(packageType, { packageType = it }, label = { Text("Embalagem") }, modifier = Modifier.fillMaxWidth(), singleLine = true) }
        item {
            OutlinedTextField(
                volume, { volume = it.filter(Char::isDigit) },
                label = { Text("Volume por unidade (ml)") }, modifier = Modifier.fillMaxWidth(), singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
        }
        item {
            OutlinedTextField(
                quantity, { quantity = it.filter(Char::isDigit) },
                label = { Text("Quantidade de unidades") }, modifier = Modifier.fillMaxWidth(), singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
        }
        item {
            OutlinedTextField(
                price, { price = it.filter { ch -> ch.isDigit() || ch == ',' || ch == '.' } },
                label = { Text("Preço total (R$)") }, modifier = Modifier.fillMaxWidth(), singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
            )
        }
        item { OutlinedTextField(store, { store = it }, label = { Text("Loja (opcional)") }, modifier = Modifier.fillMaxWidth(), singleLine = true) }
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = returnable, onCheckedChange = { returnable = it })
                Text("Inclui vasilhame retornável")
            }
        }
        if (isValid) {
            item {
                val perLiter = BeerPriceCalculator.pricePerLiter(priceValue!!, volumeValue!!, quantityValue!!)
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp)) {
                        Text("Prévia", fontWeight = FontWeight.Bold)
                        Text("Volume total: ${formatVolume(BeerPriceCalculator.totalVolumeMl(volumeValue, quantityValue).toLong())}")
                        Text(formatPricePerLiter(perLiter), color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
        item {
            Button(
                enabled = isValid,
                onClick = {
                    onSave(brand, packageType, volumeValue!!, quantityValue!!, priceValue!!, store, returnable)
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Salvar oferta") }
        }
    }
}

@Composable
private fun RankingScreen(offers: List<BeerOfferEntity>, padding: PaddingValues) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(padding),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (offers.isEmpty()) {
            item { Text("Adicione ofertas para gerar o ranking.") }
        } else {
            items(offers) { offer ->
                val rank = offers.indexOfFirst { it.id == offer.id } + 1
                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("#$rank", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Column(Modifier.weight(1f)) {
                            Text(offer.brand, fontWeight = FontWeight.Bold)
                            Text(offerDescription(offer), style = MaterialTheme.typography.bodySmall)
                        }
                        Text(formatPricePerLiter(offer.pricePerLiter), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
    }
}

@Composable
private fun SimulatorScreen(offers: List<BeerOfferEntity>, padding: PaddingValues) {
    var budgetText by rememberSaveable { mutableStateOf("30") }
    val budget = budgetText.replace(',', '.').toDoubleOrNull() ?: 0.0
    val results = remember(offers, budget) {
        offers.map { offer ->
            offer to BeerPriceCalculator.purchasableVolumeMl(budget, offer.totalPrice, offer.volumeMl, offer.quantity)
        }.filter { it.second > 0 }.sortedByDescending { it.second }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(padding),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            OutlinedTextField(
                value = budgetText,
                onValueChange = { budgetText = it.filter { ch -> ch.isDigit() || ch == ',' || ch == '.' } },
                label = { Text("Quanto você quer gastar? (R$)") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true
            )
        }
        if (offers.isEmpty()) {
            item { Text("Adicione ofertas para usar o simulador.") }
        } else if (results.isEmpty()) {
            item { Text("Com esse valor ainda não é possível comprar nenhuma das ofertas cadastradas.") }
        } else {
            item { Text("Mais cerveja pelo orçamento", style = MaterialTheme.typography.titleMedium) }
            items(results) { (offer, volume) ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp)) {
                        Text(offer.brand, fontWeight = FontWeight.Bold)
                        Text(offerDescription(offer))
                        Text("Você consegue comprar ${formatVolume(volume)}", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
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
    onDelete: (BeerOfferEntity) -> Unit
) {
    val history = remember(offers) { offers.sortedByDescending { it.createdAt } }
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(padding),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (history.isEmpty()) {
            item { Text("Seu histórico de preços aparecerá aqui.") }
        }
        items(history, key = { it.id }) { offer ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(offer.brand, fontWeight = FontWeight.Bold)
                        Text(offerDescription(offer), style = MaterialTheme.typography.bodySmall)
                        Text("${currencyFormatter.format(offer.totalPrice)} • ${formatPricePerLiter(offer.pricePerLiter)}")
                        Text(dateFormatter.format(Date(offer.createdAt)), style = MaterialTheme.typography.labelSmall)
                    }
                    IconButton(onClick = { onDelete(offer) }) {
                        Icon(Icons.Rounded.Delete, contentDescription = "Excluir")
                    }
                }
            }
        }
    }
}

@Composable
private fun OfferRow(offer: BeerOfferEntity) {
    Column(Modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(offer.brand, fontWeight = FontWeight.SemiBold)
                Text(offerDescription(offer), style = MaterialTheme.typography.bodySmall)
            }
            Text(formatPricePerLiter(offer.pricePerLiter), fontWeight = FontWeight.Bold)
        }
        HorizontalDivider(Modifier.padding(top = 8.dp))
    }
}

private fun offerDescription(offer: BeerOfferEntity): String {
    val pack = if (offer.quantity > 1) "${offer.quantity} × ${offer.volumeMl} ml" else "${offer.volumeMl} ml"
    val returnable = if (offer.hasReturnableBottle) " • com vasilhame" else ""
    return "${offer.packageType} • $pack • ${formatVolume(offer.totalVolumeMl.toLong())}$returnable"
}

private fun storeSuffix(offer: BeerOfferEntity): String = if (offer.store.isBlank()) "" else " • ${offer.store}"

private fun formatPricePerLiter(value: Double): String = "${currencyFormatter.format(value)}/L"

private fun formatVolume(volumeMl: Long): String = when {
    volumeMl >= 1000 -> String.format(ptBr, "%.2f L", volumeMl / 1000.0)
    else -> "$volumeMl ml"
}
