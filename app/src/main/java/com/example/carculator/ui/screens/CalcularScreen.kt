package com.example.carculator.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
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
import com.example.carculator.model.MeuCarro
import com.example.carculator.model.TipoPercurso
import java.util.Locale

@Composable
fun CalcularScreen(
    carroPrincipal: MeuCarro?,
    onNavegarParaCarros: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    if (carroPrincipal == null) {
        SemCarroCadastrado(onNavegarParaCarros, modifier)
        return
    }

    var tipoPercursoSelecionado by remember { mutableStateOf(TipoPercurso.CIDADE) }
    var distanciaTexto by remember { mutableStateOf("") }
    var precoLitroTexto by remember { mutableStateOf("") }

    val consumoKmL = carroPrincipal.obterConsumoPorPercurso(tipoPercursoSelecionado)

    // Cálculos em tempo real
    val distanciaKm = distanciaTexto.replace(',', '.').toDoubleOrNull()
    val precoLitro = precoLitroTexto.replace(',', '.').toDoubleOrNull()

    val litrosNecessarios = if (distanciaKm != null && distanciaKm > 0 && consumoKmL > 0) {
        distanciaKm / consumoKmL
    } else null

    val custoEstimado = if (litrosNecessarios != null && precoLitro != null && precoLitro > 0) {
        litrosNecessarios * precoLitro
    } else null

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Cartão do Carro Selecionado
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Carro selecionado:",
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = carroPrincipal.nomeCarro,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Cidade: ${String.format(Locale.getDefault(), "%.1f", carroPrincipal.consumoCidade)} km/L",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = "Estrada: ${String.format(Locale.getDefault(), "%.1f", carroPrincipal.consumoEstrada)} km/L",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }

        // 2. Seleção de Tipo de Trajeto (Botões Grandes: mínimo 60dp)
        Text(
            text = "Tipo de Trajeto:",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            BotaoOpcaoTrajeto(
                rotulo = "Cidade",
                selecionado = tipoPercursoSelecionado == TipoPercurso.CIDADE,
                onClick = { tipoPercursoSelecionado = TipoPercurso.CIDADE },
                modifier = Modifier.weight(1f)
            )
            BotaoOpcaoTrajeto(
                rotulo = "Estrada",
                selecionado = tipoPercursoSelecionado == TipoPercurso.ESTRADA,
                onClick = { tipoPercursoSelecionado = TipoPercurso.ESTRADA },
                modifier = Modifier.weight(1f)
            )
            BotaoOpcaoTrajeto(
                rotulo = "Misto",
                selecionado = tipoPercursoSelecionado == TipoPercurso.MISTO,
                onClick = { tipoPercursoSelecionado = TipoPercurso.MISTO },
                modifier = Modifier.weight(1f)
            )
        }

        Text(
            text = "Consumo considerado: ${String.format(Locale.getDefault(), "%.1f", consumoKmL)} km/L",
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary
        )

        // 3. Campo de Distância + Botão Google Maps
        Text(
            text = "Distância da viagem:",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )

        OutlinedTextField(
            value = distanciaTexto,
            onValueChange = { distanciaTexto = it },
            label = { Text("Distância em km (ex: 150)", fontSize = 18.sp) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            textStyle = TextStyle(fontSize = 22.sp, fontWeight = FontWeight.Bold),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(68.dp)
        )

        // Botão Grande para abrir o Google Maps
        Button(
            onClick = { abrirGoogleMaps(context) },
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.secondary
            ),
            shape = RoundedCornerShape(10.dp)
        ) {
            Text(
                text = "📍 Abrir Google Maps",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // 4. Campo Opcional: Preço do Litro
        Text(
            text = "Preço do Combustível (Opcional):",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )

        OutlinedTextField(
            value = precoLitroTexto,
            onValueChange = { precoLitroTexto = it },
            label = { Text("Preço por litro em R$ (ex: 5.89)", fontSize = 18.sp) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            textStyle = TextStyle(fontSize = 22.sp, fontWeight = FontWeight.Bold),
            modifier = Modifier
                .fillMaxWidth()
                .height(68.dp)
        )

        // 5. Cartão de Resultado em Destaque (Alto Contraste)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primary
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "LITROS NECESSÁRIOS",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f)
                )

                Text(
                    text = if (litrosNecessarios != null) {
                        "${String.format(Locale.getDefault(), "%.1f", litrosNecessarios)} L"
                    } else {
                        "-- L"
                    },
                    fontSize = 34.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onPrimary
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "CUSTO ESTIMADO",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f)
                )

                Text(
                    text = if (custoEstimado != null) {
                        "R$ ${String.format(Locale.getDefault(), "%.2f", custoEstimado)}"
                    } else {
                        "R$ --"
                    },
                    fontSize = 34.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun BotaoOpcaoTrajeto(
    rotulo: String,
    selecionado: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (selecionado) {
        Button(
            onClick = onClick,
            modifier = modifier.height(60.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(text = rotulo, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
    } else {
        OutlinedButton(
            onClick = onClick,
            modifier = modifier.height(60.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.onSurface
            ),
            border = BorderStroke(2.dp, MaterialTheme.colorScheme.outline),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(text = rotulo, fontSize = 18.sp, fontWeight = FontWeight.Normal)
        }
    }
}

@Composable
fun SemCarroCadastrado(
    onCadastrarCarro: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "🚗",
            fontSize = 64.sp
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Nenhum carro cadastrado!",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Cadastre seu veículo com o consumo de combustível para começar a calcular.",
            fontSize = 18.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onCadastrarCarro,
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp),
            shape = RoundedCornerShape(10.dp)
        ) {
            Text(
                text = "➕ Cadastrar meu carro",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

private fun abrirGoogleMaps(context: Context) {
    try {
        val uri = Uri.parse("https://www.google.com/maps")
        val intent = Intent(Intent.ACTION_VIEW, uri)
        context.startActivity(intent)
    } catch (_: Exception) {
        Toast.makeText(context, "Não foi possível abrir o navegador ou Google Maps.", Toast.LENGTH_LONG).show()
    }
}
