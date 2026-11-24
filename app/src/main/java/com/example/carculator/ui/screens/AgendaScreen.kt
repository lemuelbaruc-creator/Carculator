package com.example.carculator.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.carculator.data.CarculatorDbHelper
import com.example.carculator.model.Manutencao
import com.example.carculator.model.MeuCarro
import com.example.carculator.model.StatusManutencao
import com.example.carculator.ui.theme.StatusAmarelo
import com.example.carculator.ui.theme.StatusVerde
import com.example.carculator.ui.theme.StatusVermelho
import java.text.NumberFormat
import java.util.Locale

@Composable
fun AgendaScreen(
    carroPrincipal: MeuCarro?,
    dbHelper: CarculatorDbHelper,
    onNavegarParaCarros: () -> Unit,
    onCarroAtualizado: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    if (carroPrincipal == null) {
        SemCarroCadastrado(onNavegarParaCarros, modifier)
        return
    }

    var manutencoes by remember(carroPrincipal.id) {
        mutableStateOf(dbHelper.listarManutencoesPorCarro(carroPrincipal.id))
    }
    var mostrarDialogAtualizarKm by remember { mutableStateOf(false) }

    fun recarregar() {
        manutencoes = dbHelper.listarManutencoesPorCarro(carroPrincipal.id)
    }

    val formatadorKm = NumberFormat.getIntegerInstance(Locale("pt", "BR"))

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Cabeçalho com o Carro Atual e Quilometragem em destaque
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Agenda de Manutenção de:",
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = carroPrincipal.nomeCarro,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "KM ATUAL DO PAINEL",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                        Text(
                            text = "${formatadorKm.format(carroPrincipal.kmAtual)} km",
                            fontSize = 26.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    // Botão grande para atualizar KM
                    Button(
                        onClick = { mostrarDialogAtualizarKm = true },
                        modifier = Modifier
                            .height(60.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondary
                        )
                    ) {
                        Text(
                            text = "🔄 Atualizar KM",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        Text(
            text = "Serviços Preventivos:",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            items(manutencoes, key = { it.id }) { item ->
                CartaoManutencao(
                    manutencao = item,
                    kmAtualCarro = carroPrincipal.kmAtual,
                    onMarcarComoFeita = {
                        dbHelper.marcarComoFeita(item.id, item.tipoServico, carroPrincipal.kmAtual)
                        recarregar()
                        Toast.makeText(
                            context,
                            "${item.tipoServico.descricao} atualizada com sucesso!",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                )
            }
        }
    }

    // Dialog para atualizar o hodômetro / KM Atual
    if (mostrarDialogAtualizarKm) {
        DialogAtualizarKm(
            kmAtual = carroPrincipal.kmAtual,
            onDismiss = { mostrarDialogAtualizarKm = false },
            onConfirmar = { novoKm ->
                dbHelper.atualizarKmAtual(carroPrincipal.id, novoKm)
                mostrarDialogAtualizarKm = false
                onCarroAtualizado()
                recarregar()
                Toast.makeText(context, "Quilometragem atualizada para ${formatadorKm.format(novoKm)} km!", Toast.LENGTH_SHORT).show()
            }
        )
    }
}

@Composable
fun CartaoManutencao(
    manutencao: Manutencao,
    kmAtualCarro: Int,
    onMarcarComoFeita: () -> Unit
) {
    val status = manutencao.obterStatus(kmAtualCarro)
    val diasRestantes = manutencao.calcularDiasRestantes()
    val kmRestantes = manutencao.calcularKmRestantes(kmAtualCarro)
    val formatadorKm = NumberFormat.getIntegerInstance(Locale("pt", "BR"))

    val (corStatus, textoStatus) = when (status) {
        StatusManutencao.EM_DIA -> Pair(
            StatusVerde,
            "EM DIA"
        )
        StatusManutencao.ATENCAO -> Pair(
            StatusAmarelo,
            "ATENÇÃO"
        )
        StatusManutencao.VENCIDO -> Pair(
            StatusVermelho,
            "VENCIDO"
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(2.dp, corStatus),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Título do Serviço + Badge de Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = manutencao.tipoServico.descricao,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )

                // Selo / Badge de Status com alto contraste
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(corStatus)
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = textoStatus,
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Meta clara: Próximo em X km (faltam Y km) ou até DD/MM/YYYY
            val textoKmRestante = if (kmRestantes <= 0) {
                "Ultrapassou em ${formatadorKm.format(-kmRestantes)} km!"
            } else {
                "faltam ${formatadorKm.format(kmRestantes)} km"
            }

            val textoDiasRestante = when {
                diasRestantes == null -> ""
                diasRestantes < 0 -> "venceu há ${-diasRestantes} dia(s)"
                diasRestantes == 0L -> "vence hoje"
                else -> "faltam $diasRestantes dia(s)"
            }

            Text(
                text = "🎯 Próximo em: ${formatadorKm.format(manutencao.kmProximo)} km ($textoKmRestante) ou até ${manutencao.dataProximaFormatada()} ($textoDiasRestante)",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = corStatus,
                lineHeight = 24.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Linha com informações da última realização
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Última: ${manutencao.dataUltimaFormatada()}",
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Feita aos ${formatadorKm.format(manutencao.kmUltima)} km",
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Botão Grande "Já fiz" (altura mínima 60dp)
            Button(
                onClick = onMarcarComoFeita,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text(
                    text = "✅ Já fiz (${manutencao.tipoServico.descricao})",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun DialogAtualizarKm(
    kmAtual: Int,
    onDismiss: () -> Unit,
    onConfirmar: (novoKm: Int) -> Unit
) {
    var kmTexto by remember { mutableStateOf(kmAtual.toString()) }
    var erroValidacao by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Atualizar Hodômetro",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Digite a quilometragem atual marcada no painel do veículo:",
                    fontSize = 18.sp
                )

                OutlinedTextField(
                    value = kmTexto,
                    onValueChange = { kmTexto = it; erroValidacao = null },
                    label = { Text("KM Atual", fontSize = 18.sp) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    textStyle = TextStyle(fontSize = 22.sp, fontWeight = FontWeight.Bold),
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
                    val km = kmTexto.trim().toIntOrNull()
                    if (km == null || km < 0) {
                        erroValidacao = "Digite uma quilometragem válida (0 ou maior)."
                    } else {
                        onConfirmar(km)
                    }
                },
                modifier = Modifier.height(56.dp)
            ) {
                Text(text = "Salvar KM", fontSize = 18.sp, fontWeight = FontWeight.Bold)
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
