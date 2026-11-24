package com.example.carculator.model

data class MeuCarro(
    val id: Long = 0,
    val nomeCarro: String,
    val consumoCidade: Double,
    val consumoEstrada: Double,
    val kmAtual: Int = 0,
    val principal: Boolean = false
) {
    /**
     * Retorna o consumo em km/L de acordo com o percurso:
     * - CIDADE: consumoCidade
     * - ESTRADA: consumoEstrada
     * - MISTO: média aritmética entre cidade e estrada
     */
    fun obterConsumoPorPercurso(percurso: TipoPercurso): Double {
        return when (percurso) {
            TipoPercurso.CIDADE -> consumoCidade
            TipoPercurso.ESTRADA -> consumoEstrada
            TipoPercurso.MISTO -> (consumoCidade + consumoEstrada) / 2.0
        }
    }
}
