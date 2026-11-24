package com.example.carculator.data

import android.content.Context
import com.example.carculator.model.Carro
import com.example.carculator.model.TipoCombustivel
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.text.Normalizer
import java.util.regex.Pattern

class CarrosRepository(private val context: Context) {

    private var cacheCarros: List<Carro>? = null

    /**
     * Carrega a lista completa de carros do catálogo local (assets/carros.json).
     * O resultado fica em cache na memória para garantir respostas instantâneas.
     */
    @Synchronized
    fun listarTodos(): List<Carro> {
        cacheCarros?.let { return it }

        val carros = mutableListOf<Carro>()
        try {
            val jsonString = context.assets.open("carros.json").use { inputStream ->
                BufferedReader(InputStreamReader(inputStream, Charsets.UTF_8)).use { reader ->
                    reader.readText()
                }
            }

            val jsonArray = JSONArray(jsonString)
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                val carro = parseCarro(obj)
                carros.add(carro)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        cacheCarros = carros
        return carros
    }

    /**
     * Busca rápida por texto (marca ou modelo), ignorando maiúsculas/minúsculas e acentuação.
     */
    fun buscar(termo: String): List<Carro> {
        val todos = listarTodos()
        if (termo.isBlank()) return todos

        val termoNormalizado = normalizarTexto(termo)
        return todos.filter { carro ->
            normalizarTexto(carro.marca).contains(termoNormalizado) ||
            normalizarTexto(carro.modelo).contains(termoNormalizado) ||
            normalizarTexto(carro.nomeExibicao).contains(termoNormalizado)
        }
    }

    /**
     * Obtém um carro específico pelo seu ID único.
     */
    fun obterPorId(id: String): Carro? {
        return listarTodos().firstOrNull { it.id == id }
    }

    private fun parseCarro(obj: JSONObject): Carro {
        return Carro(
            id = obj.getString("id"),
            marca = obj.getString("marca"),
            modelo = obj.getString("modelo"),
            anoInicio = obj.getInt("anoInicio"),
            anoFim = obj.getInt("anoFim"),
            combustivel = TipoCombustivel.fromString(obj.optString("combustivel", "FLEX")),
            consumoCidadeGasolina = obj.optDoubleOrNull("consumoCidadeGasolina"),
            consumoEstradaGasolina = obj.optDoubleOrNull("consumoEstradaGasolina"),
            consumoCidadeEtanol = obj.optDoubleOrNull("consumoCidadeEtanol"),
            consumoEstradaEtanol = obj.optDoubleOrNull("consumoEstradaEtanol"),
            consumoCidadeDiesel = obj.optDoubleOrNull("consumoCidadeDiesel"),
            consumoEstradaDiesel = obj.optDoubleOrNull("consumoEstradaDiesel"),
            isCustom = false
        )
    }

    private fun JSONObject.optDoubleOrNull(name: String): Double? {
        if (!has(name) || isNull(name)) return null
        val value = optDouble(name, Double.NaN)
        return if (value.isNaN()) null else value
    }

    private fun normalizarTexto(texto: String): String {
        val normalizado = Normalizer.normalize(texto, Normalizer.Form.NFD)
        val padrao = Pattern.compile("\\p{InCombiningDiacriticalMarks}+")
        return padrao.matcher(normalizado).replaceAll("").lowercase().trim()
    }
}
