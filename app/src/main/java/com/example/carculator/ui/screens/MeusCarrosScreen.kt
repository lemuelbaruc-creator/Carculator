package com.example.carculator.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.carculator.data.CarculatorDbHelper
import com.example.carculator.model.MeuCarro
import java.text.NumberFormat
import java.util.Locale

@Composable
fun MeusCarrosScreen(
    dbHelper: CarculatorDbHelper,
    onCarroAlterado: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var listaCarros by remember { mutableStateOf(dbHelper.listarCarros()) }
    var mostrarDialogCadastro by remember { mutableStateOf(false) }
    var carroParaExcluir by remember { mutableStateOf<MeuCarro?>(null) }

    fun recarregar() {
        listaCarros = dbHelper.listarCarros()
        onCarroAlterado()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Cabeçalho e Botão Adicionar Carro
        Text(
            text = "Minha Garagem",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold
        )

        Button(
            onClick = { mostrarDialogCadastro = true },
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp),
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Text(
                text = "➕ Adicionar Novo Carro",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }

        if (listaCarros.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(text = "🚗", fontSize = 64.sp)
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Nenhum carro na garagem",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Toque no botão acima para cadastrar seu primeiro veículo.",
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(listaCarros, key = { it.id }) { carro ->
                    CartaoCarro(
                        carro = carro,
                        onSelecionarPrincipal = {
                            dbHelper.definirComoPrincipal(carro.id)
                            recarregar()
                            Toast.makeText(context, "${carro.nomeCarro} selecionado como principal!", Toast.LENGTH_SHORT).show()
                        },
                        onSolicitarExclusao = {
                            carroParaExcluir = carro
                        }
                    )
                }
            }
        }
    }

    // Dialog de Cadastro de Carro
    if (mostrarDialogCadastro) {
        DialogCadastrarCarro(
            onDismiss = { mostrarDialogCadastro = false },
            onSalvar = { nome, cidade, estrada, kmAtual ->
                dbHelper.salvarCarro(
                    nomeCarro = nome,
                    consumoCidade = cidade,
                    consumoEstrada = estrada,
                    kmAtual = kmAtual,
                    definirComoPrincipal = listaCarros.isEmpty()
                )
                mostrarDialogCadastro = false
                recarregar()
                Toast.makeText(context, "Carro cadastrado com sucesso!", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // Dialog de Confirmação de Exclusão
    carroParaExcluir?.let { carro ->
        AlertDialog(
            onDismissRequest = { carroParaExcluir = null },
            title = {
                Text(
                    text = "Excluir Carro?",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "Deseja realmente remover '${carro.nomeCarro}' da sua garagem? Todo o histórico de manutenção dele também será excluído.",
                    fontSize = 18.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        dbHelper.excluirCarro(carro.id)
                        carroParaExcluir = null
                        recarregar()
                        Toast.makeText(context, "Carro excluído.", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.height(54.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text(text = "Excluir", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { carroParaExcluir = null },
                    modifier = Modifier.height(54.dp)
                ) {
                    Text(text = "Cancelar", fontSize = 18.sp)
                }
            }
        )
    }
}

@Composable
fun CartaoCarro(
    carro: MeuCarro,
    onSelecionarPrincipal: () -> Unit,
    onSolicitarExclusao: () -> Unit
) {
    val ehPrincipal = carro.principal
    val borderStroke = if (ehPrincipal) {
        BorderStroke(3.dp, MaterialTheme.colorScheme.primary)
    } else {
        BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    }
    val formatadorKm = NumberFormat.getIntegerInstance(Locale("pt", "BR"))

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        border = borderStroke,
        colors = CardDefaults.cardColors(
            containerColor = if (ehPrincipal) {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
            } else {
                MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = carro.nomeCarro,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )

                if (ehPrincipal) {
                    Text(
                        text = "⭐ PRINCIPAL",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // KM Atual do carro
            Text(
                text = "Quilometragem: ${formatadorKm.format(carro.kmAtual)} km",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Cidade: ${String.format(Locale.getDefault(), "%.1f", carro.consumoCidade)} km/L",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "Estrada: ${String.format(Locale.getDefault(), "%.1f", carro.consumoEstrada)} km/L",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (!ehPrincipal) {
                    Button(
                        onClick = onSelecionarPrincipal,
                        modifier = Modifier
                            .weight(1f)
                            .height(60.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "Usar este carro",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                OutlinedButton(
                    onClick = onSolicitarExclusao,
                    modifier = Modifier
                        .then(if (ehPrincipal) Modifier.fillMaxWidth() else Modifier)
                        .height(60.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    ),
                    border = BorderStroke(2.dp, MaterialTheme.colorScheme.error),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "🗑️ Excluir",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun DialogCadastrarCarro(
    onDismiss: () -> Unit,
    onSalvar: (nome: String, cidade: Double, estrada: Double, kmAtual: Int) -> Unit
) {
    var nome by remember { mutableStateOf("") }
    var cidadeTexto by remember { mutableStateOf("") }
    var estradaTexto by remember { mutableStateOf("") }
    var kmAtualTexto by remember { mutableStateOf("") }
    var erroValidacao by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Novo Carro",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = nome,
                    onValueChange = { nome = it; erroValidacao = null },
                    label = { Text("Nome / Modelo (ex: Meu Gol)", fontSize = 18.sp) },
                    singleLine = true,
                    textStyle = TextStyle(fontSize = 18.sp),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = kmAtualTexto,
                    onValueChange = { kmAtualTexto = it; erroValidacao = null },
                    label = { Text("Quilometragem atual (km)", fontSize = 18.sp) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    textStyle = TextStyle(fontSize = 18.sp),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = cidadeTexto,
                    onValueChange = { cidadeTexto = it; erroValidacao = null },
                    label = { Text("Consumo Cidade em km/L", fontSize = 18.sp) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    textStyle = TextStyle(fontSize = 18.sp),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = estradaTexto,
                    onValueChange = { estradaTexto = it; erroValidacao = null },
                    label = { Text("Consumo Estrada em km/L", fontSize = 18.sp) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    textStyle = TextStyle(fontSize = 18.sp),
                    modifier = Modifier.fillMaxWidth()
                )

                erroValidacao?.let { erro ->
                    Text(
                        text = erro,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val nomeLimpo = nome.trim()
                    val cidade = cidadeTexto.replace(',', '.').toDoubleOrNull()
                    val estrada = estradaTexto.replace(',', '.').toDoubleOrNull()
                    val km = kmAtualTexto.trim().toIntOrNull()

                    if (nomeLimpo.isBlank()) {
                        erroValidacao = "Digite o nome ou modelo do carro."
                    } else if (km == null || km < 0) {
                        erroValidacao = "Informe a quilometragem atual válida (0 ou maior)."
                    } else if (cidade == null || cidade <= 0) {
                        erroValidacao = "Informe um consumo na cidade válido."
                    } else if (estrada == null || estrada <= 0) {
                        erroValidacao = "Informe um consumo na estrada válido."
                    } else {
                        onSalvar(nomeLimpo, cidade, estrada, km)
                    }
                },
                modifier = Modifier.height(56.dp)
            ) {
                Text(text = "Salvar", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.height(56.dp)
            ) {
                Text(text = "Cancelar", fontSize = 18.sp)
            }
        }
    )
}
