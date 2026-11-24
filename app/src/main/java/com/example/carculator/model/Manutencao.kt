package com.example.carculator.model

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

enum class TipoServico(
    val descricao: String,
    val mesesProximaValidade: Int,
    val kmIntervalo: Int
) {
    TROCA_OLEO("Troca de Óleo", 6, 10_000),
    TROCA_PNEUS("Troca de Pneus", 24, 40_000),
    ALINHAMENTO_BALANCEAMENTO("Alinhamento/Balanceamento", 12, 10_000),
    REVISAO_GERAL("Revisão Geral", 12, 10_000);

    companion object {
        fun fromString(valor: String): TipoServico {
            return entries.firstOrNull { it.name.equals(valor, ignoreCase = true) || it.descricao.equals(valor, ignoreCase = true) }
                ?: TROCA_OLEO
        }
    }
}

enum class StatusManutencao {
    EM_DIA,       // Verde: acima de 1.000 km e mais de 15 dias restantes
    ATENCAO,      // Amarelo: faltam 1.000 km ou menos OU faltam 15 dias ou menos
    VENCIDO       // Vermelho: kmAtual >= kmProximo OU data prevista já passou
}

data class Manutencao(
    val id: Long = 0,
    val idCarro: Long,
    val tipoServico: TipoServico,
    val dataUltima: String, // Formato yyyy-MM-dd
    val dataProxima: String, // Formato yyyy-MM-dd
    val kmUltima: Int = 0,
    val kmProximo: Int = 0
) {
    /**
     * Calcula o status considerando o que vencer primeiro (KM ou Data):
     * - VERMELHO (VENCIDO): kmAtual >= kmProximo OU data prevista já passou.
     * - AMARELO (ATENÇÃO): faltam 1.000 km ou menos para kmProximo OU faltam 15 dias ou menos para a data.
     * - VERDE (EM DIA): acima de 1.000 km e mais de 15 dias restantes.
     */
    fun obterStatus(kmAtualCarro: Int): StatusManutencao {
        val diasRestantes = calcularDiasRestantes()
        val kmRestantes = calcularKmRestantes(kmAtualCarro)

        // Se data já venceu ou km já atingiu/passou o limite
        val vencidoPorData = diasRestantes != null && diasRestantes < 0
        val vencidoPorKm = kmRestantes <= 0

        if (vencidoPorData || vencidoPorKm) {
            return StatusManutencao.VENCIDO
        }

        // Se faltam 15 dias ou menos OU faltam 1.000 km ou menos
        val atencaoPorData = diasRestantes != null && diasRestantes <= 15
        val atencaoPorKm = kmRestantes <= 1_000

        if (atencaoPorData || atencaoPorKm) {
            return StatusManutencao.ATENCAO
        }

        return StatusManutencao.EM_DIA
    }

    /**
     * Retorna a quantidade de km restantes até kmProximo (positivo = faltam km, <= 0 = vencido).
     */
    fun calcularKmRestantes(kmAtualCarro: Int): Int {
        return kmProximo - kmAtualCarro
    }

    /**
     * Retorna a quantidade de dias restantes até a dataProxima (positivo = faltam dias, negativo = vencido).
     */
    fun calcularDiasRestantes(): Long? {
        return try {
            val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val dataAlvo = format.parse(dataProxima) ?: return null
            val hoje = format.parse(format.format(Date())) ?: Date()
            val diffMs = dataAlvo.time - hoje.time
            TimeUnit.DAYS.convert(diffMs, TimeUnit.MILLISECONDS)
        } catch (_: Exception) {
            null
        }
    }

    /**
     * Retorna a data no formato legível brasileiro (dd/MM/yyyy).
     */
    fun dataProximaFormatada(): String {
        return formatarDataBr(dataProxima)
    }

    fun dataUltimaFormatada(): String {
        return formatarDataBr(dataUltima)
    }

    private fun formatarDataBr(dataIso: String): String {
        return try {
            val isoFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val brFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val date = isoFormat.parse(dataIso)
            if (date != null) brFormat.format(date) else dataIso
        } catch (_: Exception) {
            dataIso
        }
    }
}
