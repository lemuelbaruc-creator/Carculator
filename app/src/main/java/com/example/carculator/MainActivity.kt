package com.example.carculator

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.carculator.data.CarculatorDbHelper
import com.example.carculator.model.MeuCarro
import com.example.carculator.ui.screens.AgendaScreen
import com.example.carculator.ui.screens.CalcularScreen
import com.example.carculator.ui.screens.MeusCarrosScreen
import com.example.carculator.ui.theme.CarculatorTheme

enum class AbaNavegacao(val rotulo: String, val icone: String) {
    CALCULAR("Calcular", "⛽"),
    CARROS("Meus Carros", "🚗"),
    AGENDA("Agenda", "🔧")
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        esconderBarraNavegacao()
        setContent {
            CarculatorTheme {
                CarculatorApp()
            }
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            esconderBarraNavegacao()
        }
    }

    private fun esconderBarraNavegacao() {
        val controller = WindowCompat.getInsetsController(window, window.decorView)
        controller.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        controller.hide(WindowInsetsCompat.Type.navigationBars())
    }
}

@Composable
fun CarculatorApp() {
    val context = LocalContext.current
    val dbHelper = remember { CarculatorDbHelper(context) }

    var abaAtual by remember { mutableStateOf(AbaNavegacao.CALCULAR) }
    var carroPrincipal by remember { mutableStateOf<MeuCarro?>(null) }
    var mostrarAvisoTransito by rememberSaveable { mutableStateOf(true) }

    // Recarrega o carro principal sempre que necessário
    fun atualizarCarroPrincipal() {
        carroPrincipal = dbHelper.obterCarroPrincipal()
    }

    LaunchedEffect(abaAtual) {
        atualizarCarroPrincipal()
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            BarraNavegacaoGrande(
                abaAtual = abaAtual,
                onSelecionarAba = { novaAba ->
                    abaAtual = novaAba
                }
            )
        }
    ) { innerPadding ->
        when (abaAtual) {
            AbaNavegacao.CALCULAR -> {
                CalcularScreen(
                    carroPrincipal = carroPrincipal,
                    onNavegarParaCarros = { abaAtual = AbaNavegacao.CARROS },
                    modifier = Modifier.padding(innerPadding)
                )
            }
            AbaNavegacao.CARROS -> {
                MeusCarrosScreen(
                    dbHelper = dbHelper,
                    onCarroAlterado = {
                        atualizarCarroPrincipal()
                    },
                    modifier = Modifier.padding(innerPadding)
                )
            }
            AbaNavegacao.AGENDA -> {
                AgendaScreen(
                    carroPrincipal = carroPrincipal,
                    dbHelper = dbHelper,
                    onNavegarParaCarros = { abaAtual = AbaNavegacao.CARROS },
                    onCarroAtualizado = {
                        atualizarCarroPrincipal()
                    },
                    modifier = Modifier.padding(innerPadding)
                )
            }
        }
    }

    if (mostrarAvisoTransito) {
        DialogAvisoTransito(
            onEntendi = { mostrarAvisoTransito = false }
        )
    }
}

@Composable
fun DialogAvisoTransito(
    onEntendi: () -> Unit
) {
    AlertDialog(
        onDismissRequest = {},
        containerColor = MaterialTheme.colorScheme.surface,
        titleContentColor = MaterialTheme.colorScheme.onSurface,
        textContentColor = MaterialTheme.colorScheme.onSurface,
        title = {
            Text(
                text = "⚠️ Atenção no Trânsito",
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold
            )
        },
        text = {
            Text(
                text = "Não utilize este aplicativo enquanto dirige. Usar o celular ao volante é infração de trânsito e coloca vidas em risco. Faça seus cálculos com o veículo parado.",
                fontSize = 18.sp,
                lineHeight = 26.sp,
                fontWeight = FontWeight.Medium
            )
        },
        confirmButton = {
            Button(
                onClick = onEntendi,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text(
                    text = "Entendi",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    )
}

@Composable
fun BarraNavegacaoGrande(
    abaAtual: AbaNavegacao,
    onSelecionarAba: (AbaNavegacao) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        tonalElevation = 8.dp,
        shadowElevation = 8.dp,
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            AbaNavegacao.entries.forEach { aba ->
                val selecionada = aba == abaAtual
                val fundoItem = if (selecionada) {
                    MaterialTheme.colorScheme.primaryContainer
                } else {
                    Color.Transparent
                }
                val corTexto = if (selecionada) {
                    MaterialTheme.colorScheme.onPrimaryContainer
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(68.dp) // Altura grande (mínimo 60dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(fundoItem)
                        .clickable { onSelecionarAba(aba) }
                        .padding(horizontal = 4.dp, vertical = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = aba.icone,
                            fontSize = 24.sp
                        )
                        Text(
                            text = aba.rotulo,
                            fontSize = 18.sp,
                            fontWeight = if (selecionada) FontWeight.Bold else FontWeight.Medium,
                            color = corTexto,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}
