package com.example.carculator.model

enum class TipoCombustivel {
    GASOLINA,
    ETANOL,
    FLEX,
    DIESEL;

    companion object {
        fun fromString(value: String?): TipoCombustivel {
            if (value.isNullOrBlank()) return FLEX
            return try {
                valueOf(value.trim().uppercase())
            } catch (e: IllegalArgumentException) {
                FLEX
            }
        }
    }
}

enum class TipoPercurso(val rotulo: String) {
    CIDADE("Cidade"),
    ESTRADA("Estrada"),
    MISTO("Misto")
}

data class Carro(
    val id: String,
    val marca: String,
    val modelo: String,
    val anoInicio: Int,
    val anoFim: Int,
    val combustivel: TipoCombustivel,
    val consumoCidadeGasolina: Double? = null,
    val consumoEstradaGasolina: Double? = null,
    val consumoCidadeEtanol: Double? = null,
    val consumoEstradaEtanol: Double? = null,
    val consumoCidadeDiesel: Double? = null,
    val consumoEstradaDiesel: Double? = null,
    val isCustom: Boolean = false
) {
    val nomeExibicao: String
        get() = "$marca $modelo ($anoInicio - $anoFim)"

    /**
     * Retorna o consumo em km/l de acordo com o combustível e o tipo de percurso selecionados.
     * Para 'Misto', calcula a média aritmética entre Cidade e Estrada.
     */
    fun obterConsumoKmPorLitro(combustivelSelecionado: TipoCombustivel, percurso: TipoPercurso): Double? {
        val (cidade, estrada) = when (combustivelSelecionado) {
            TipoCombustivel.GASOLINA -> consumoCidadeGasolina to consumoEstradaGasolina
            TipoCombustivel.ETANOL -> consumoCidadeEtanol to consumoEstradaEtanol
            TipoCombustivel.DIESEL -> consumoCidadeDiesel to consumoEstradaDiesel
            TipoCombustivel.FLEX -> {
                // Se for FLEX e não foi especificado, usa Gasolina por padrão
                consumoCidadeGasolina to consumoEstradaGasolina
            }
        }

        return when (percurso) {
            TipoPercurso.CIDADE -> cidade
            TipoPercurso.ESTRADA -> estrada
            TipoPercurso.MISTO -> {
                if (cidade != null && estrada != null) {
                    (cidade + estrada) / 2.0
                } else {
                    cidade ?: estrada
                }
            }
        }
    }
}
