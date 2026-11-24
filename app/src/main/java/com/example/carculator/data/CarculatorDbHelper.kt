package com.example.carculator.data

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.carculator.model.Manutencao
import com.example.carculator.model.MeuCarro
import com.example.carculator.model.TipoServico
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class CarculatorDbHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        const val DATABASE_NAME = "carculator.db"
        const val DATABASE_VERSION = 2

        // Tabela meus_carros
        const val TABLE_CARROS = "meus_carros"
        const val COL_CARRO_ID = "id"
        const val COL_CARRO_NOME = "nome_carro"
        const val COL_CARRO_CONSUMO_CIDADE = "consumo_cidade"
        const val COL_CARRO_CONSUMO_ESTRADA = "consumo_estrada"
        const val COL_CARRO_KM_ATUAL = "km_atual"
        const val COL_CARRO_PRINCIPAL = "principal"

        // Tabela manutencoes
        const val TABLE_MANUTENCOES = "manutencoes"
        const val COL_MANUT_ID = "id"
        const val COL_MANUT_ID_CARRO = "id_carro"
        const val COL_MANUT_TIPO_SERVICO = "tipo_servico"
        const val COL_MANUT_DATA_ULTIMA = "data_ultima"
        const val COL_MANUT_DATA_PROXIMA = "data_proxima"
        const val COL_MANUT_KM_ULTIMA = "km_ultima"
        const val COL_MANUT_KM_PROXIMO = "km_proximo"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTableCarros = """
            CREATE TABLE $TABLE_CARROS (
                $COL_CARRO_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_CARRO_NOME TEXT NOT NULL,
                $COL_CARRO_CONSUMO_CIDADE REAL NOT NULL,
                $COL_CARRO_CONSUMO_ESTRADA REAL NOT NULL,
                $COL_CARRO_KM_ATUAL INTEGER NOT NULL DEFAULT 0,
                $COL_CARRO_PRINCIPAL INTEGER NOT NULL DEFAULT 0
            );
        """.trimIndent()

        val createTableManutencoes = """
            CREATE TABLE $TABLE_MANUTENCOES (
                $COL_MANUT_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_MANUT_ID_CARRO INTEGER NOT NULL,
                $COL_MANUT_TIPO_SERVICO TEXT NOT NULL,
                $COL_MANUT_DATA_ULTIMA TEXT NOT NULL,
                $COL_MANUT_DATA_PROXIMA TEXT NOT NULL,
                $COL_MANUT_KM_ULTIMA INTEGER NOT NULL DEFAULT 0,
                $COL_MANUT_KM_PROXIMO INTEGER NOT NULL DEFAULT 0,
                FOREIGN KEY ($COL_MANUT_ID_CARRO) REFERENCES $TABLE_CARROS($COL_CARRO_ID) ON DELETE CASCADE
            );
        """.trimIndent()

        db.execSQL(createTableCarros)
        db.execSQL(createTableManutencoes)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 2) {
            // Migração da v1 para v2
            try {
                db.execSQL("ALTER TABLE $TABLE_CARROS ADD COLUMN $COL_CARRO_KM_ATUAL INTEGER NOT NULL DEFAULT 0")
            } catch (_: Exception) {}

            try {
                db.execSQL("ALTER TABLE $TABLE_MANUTENCOES ADD COLUMN $COL_MANUT_KM_ULTIMA INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE $TABLE_MANUTENCOES ADD COLUMN $COL_MANUT_KM_PROXIMO INTEGER NOT NULL DEFAULT 0")
            } catch (_: Exception) {}
        }
    }

    override fun onConfigure(db: SQLiteDatabase) {
        super.onConfigure(db)
        db.setForeignKeyConstraintsEnabled(true)
    }

    // ==========================================
    // OPERAÇÕES: MEUS CARROS
    // ==========================================

    /**
     * Salva um novo carro com sua quilometragem inicial. Se for o primeiro carro cadastrado ou se [definirComoPrincipal] for true,
     * define-o como carro principal da garagem.
     * Além disso, inicializa os 4 serviços padrão na tabela de manutenções considerando o kmAtual informado.
     */
    fun salvarCarro(
        nomeCarro: String,
        consumoCidade: Double,
        consumoEstrada: Double,
        kmAtual: Int = 0,
        definirComoPrincipal: Boolean = false
    ): Long {
        val db = writableDatabase
        val totalCarros = contarCarros()
        val ehPrincipal = definirComoPrincipal || totalCarros == 0

        if (ehPrincipal) {
            desmarcarTodosPrincipais(db)
        }

        val values = ContentValues().apply {
            put(COL_CARRO_NOME, nomeCarro)
            put(COL_CARRO_CONSUMO_CIDADE, consumoCidade)
            put(COL_CARRO_CONSUMO_ESTRADA, consumoEstrada)
            put(COL_CARRO_KM_ATUAL, kmAtual)
            put(COL_CARRO_PRINCIPAL, if (ehPrincipal) 1 else 0)
        }

        val idInserido = db.insert(TABLE_CARROS, null, values)

        if (idInserido > 0) {
            inicializarManutencoesPadrao(db, idInserido, kmAtual)
        }

        return idInserido
    }

    /**
     * Atualiza a quilometragem atual do carro no hodômetro.
     */
    fun atualizarKmAtual(idCarro: Long, novoKm: Int): Int {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COL_CARRO_KM_ATUAL, novoKm)
        }
        return db.update(TABLE_CARROS, values, "$COL_CARRO_ID = ?", arrayOf(idCarro.toString()))
    }

    /**
     * Atualiza os dados de um carro existente.
     */
    fun atualizarCarro(
        id: Long,
        nomeCarro: String,
        consumoCidade: Double,
        consumoEstrada: Double,
        kmAtual: Int
    ): Int {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COL_CARRO_NOME, nomeCarro)
            put(COL_CARRO_CONSUMO_CIDADE, consumoCidade)
            put(COL_CARRO_CONSUMO_ESTRADA, consumoEstrada)
            put(COL_CARRO_KM_ATUAL, kmAtual)
        }
        return db.update(TABLE_CARROS, values, "$COL_CARRO_ID = ?", arrayOf(id.toString()))
    }

    /**
     * Exclui um carro e, por cascata, suas manutenções.
     */
    fun excluirCarro(id: Long): Int {
        val db = writableDatabase
        val resultado = db.delete(TABLE_CARROS, "$COL_CARRO_ID = ?", arrayOf(id.toString()))

        // Se o carro excluído era o principal, seleciona o primeiro disponível como principal
        val carroPrincipal = obterCarroPrincipal()
        if (carroPrincipal == null) {
            val primeiro = listarCarros().firstOrNull()
            if (primeiro != null) {
                definirComoPrincipal(primeiro.id)
            }
        }

        return resultado
    }

    /**
     * Lista todos os carros cadastrados pelo usuário.
     */
    fun listarCarros(): List<MeuCarro> {
        val lista = mutableListOf<MeuCarro>()
        val db = readableDatabase
        val cursor: Cursor = db.query(
            TABLE_CARROS,
            null,
            null,
            null,
            null,
            null,
            "$COL_CARRO_PRINCIPAL DESC, $COL_CARRO_ID ASC"
        )

        cursor.use {
            val idIndex = it.getColumnIndexOrThrow(COL_CARRO_ID)
            val nomeIndex = it.getColumnIndexOrThrow(COL_CARRO_NOME)
            val cidadeIndex = it.getColumnIndexOrThrow(COL_CARRO_CONSUMO_CIDADE)
            val estradaIndex = it.getColumnIndexOrThrow(COL_CARRO_CONSUMO_ESTRADA)
            val kmIndex = it.getColumnIndexOrThrow(COL_CARRO_KM_ATUAL)
            val principalIndex = it.getColumnIndexOrThrow(COL_CARRO_PRINCIPAL)

            while (it.moveToNext()) {
                lista.add(
                    MeuCarro(
                        id = it.getLong(idIndex),
                        nomeCarro = it.getString(nomeIndex),
                        consumoCidade = it.getDouble(cidadeIndex),
                        consumoEstrada = it.getDouble(estradaIndex),
                        kmAtual = it.getInt(kmIndex),
                        principal = it.getInt(principalIndex) == 1
                    )
                )
            }
        }
        return lista
    }

    /**
     * Obtém o carro atualmente marcado como principal.
     */
    fun obterCarroPrincipal(): MeuCarro? {
        val db = readableDatabase
        val cursor: Cursor = db.query(
            TABLE_CARROS,
            null,
            "$COL_CARRO_PRINCIPAL = 1",
            null,
            null,
            null,
            null,
            "1"
        )

        cursor.use {
            if (it.moveToFirst()) {
                return MeuCarro(
                    id = it.getLong(it.getColumnIndexOrThrow(COL_CARRO_ID)),
                    nomeCarro = it.getString(it.getColumnIndexOrThrow(COL_CARRO_NOME)),
                    consumoCidade = it.getDouble(it.getColumnIndexOrThrow(COL_CARRO_CONSUMO_CIDADE)),
                    consumoEstrada = it.getDouble(it.getColumnIndexOrThrow(COL_CARRO_CONSUMO_ESTRADA)),
                    kmAtual = it.getInt(it.getColumnIndexOrThrow(COL_CARRO_KM_ATUAL)),
                    principal = true
                )
            }
        }

        // Se nenhum estiver marcado explicitamente como principal, pega o primeiro da lista
        return listarCarros().firstOrNull()
    }

    /**
     * Define um carro específico como o carro principal.
     */
    fun definirComoPrincipal(idCarro: Long) {
        val db = writableDatabase
        db.beginTransaction()
        try {
            desmarcarTodosPrincipais(db)
            val values = ContentValues().apply {
                put(COL_CARRO_PRINCIPAL, 1)
            }
            db.update(TABLE_CARROS, values, "$COL_CARRO_ID = ?", arrayOf(idCarro.toString()))
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    private fun desmarcarTodosPrincipais(db: SQLiteDatabase) {
        val values = ContentValues().apply {
            put(COL_CARRO_PRINCIPAL, 0)
        }
        db.update(TABLE_CARROS, values, null, null)
    }

    private fun contarCarros(): Int {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT COUNT(*) FROM $TABLE_CARROS", null)
        cursor.use {
            return if (it.moveToFirst()) it.getInt(0) else 0
        }
    }

    // ==========================================
    // OPERAÇÕES: AGENDA DE MANUTENÇÃO
    // ==========================================

    /**
     * Inicializa os 4 serviços obrigatórios para um carro recém-cadastrado,
     * calculando a data de hoje como última e a data futura de acordo com a periodicidade,
     * bem como kmUltima = kmAtual e kmProximo = kmAtual + intervalo.
     */
    private fun inicializarManutencoesPadrao(db: SQLiteDatabase, idCarro: Long, kmAtual: Int) {
        val dataHoje = formatarDataIso(Date())

        for (servico in TipoServico.entries) {
            val dataProxima = calcularProximaDataIso(Date(), servico.mesesProximaValidade)
            val values = ContentValues().apply {
                put(COL_MANUT_ID_CARRO, idCarro)
                put(COL_MANUT_TIPO_SERVICO, servico.name)
                put(COL_MANUT_DATA_ULTIMA, dataHoje)
                put(COL_MANUT_DATA_PROXIMA, dataProxima)
                put(COL_MANUT_KM_ULTIMA, kmAtual)
                put(COL_MANUT_KM_PROXIMO, kmAtual + servico.kmIntervalo)
            }
            db.insert(TABLE_MANUTENCOES, null, values)
        }
    }

    /**
     * Lista todas as manutenções cadastradas para o carro indicado.
     */
    fun listarManutencoesPorCarro(idCarro: Long): List<Manutencao> {
        val lista = mutableListOf<Manutencao>()
        val db = readableDatabase
        val cursor: Cursor = db.query(
            TABLE_MANUTENCOES,
            null,
            "$COL_MANUT_ID_CARRO = ?",
            arrayOf(idCarro.toString()),
            null,
            null,
            "$COL_MANUT_KM_PROXIMO ASC, $COL_MANUT_DATA_PROXIMA ASC"
        )

        cursor.use {
            val idIndex = it.getColumnIndexOrThrow(COL_MANUT_ID)
            val carroIndex = it.getColumnIndexOrThrow(COL_MANUT_ID_CARRO)
            val servicoIndex = it.getColumnIndexOrThrow(COL_MANUT_TIPO_SERVICO)
            val ultimaIndex = it.getColumnIndexOrThrow(COL_MANUT_DATA_ULTIMA)
            val proximaIndex = it.getColumnIndexOrThrow(COL_MANUT_DATA_PROXIMA)
            val kmUltimaIndex = it.getColumnIndexOrThrow(COL_MANUT_KM_ULTIMA)
            val kmProximoIndex = it.getColumnIndexOrThrow(COL_MANUT_KM_PROXIMO)

            while (it.moveToNext()) {
                val servicoStr = it.getString(servicoIndex)
                lista.add(
                    Manutencao(
                        id = it.getLong(idIndex),
                        idCarro = it.getLong(carroIndex),
                        tipoServico = TipoServico.fromString(servicoStr),
                        dataUltima = it.getString(ultimaIndex),
                        dataProxima = it.getString(proximaIndex),
                        kmUltima = it.getInt(kmUltimaIndex),
                        kmProximo = it.getInt(kmProximoIndex)
                    )
                )
            }
        }
        return lista
    }

    /**
     * Marca a manutenção como "Já fiz":
     * - Atualiza a data_ultima para a data de hoje.
     * - Calcula a nova data_proxima de acordo com mesesProximaValidade.
     * - Atualiza km_ultima = kmAtual do carro.
     * - Calcula novo km_proximo = kmAtual + kmIntervalo do serviço.
     */
    fun marcarComoFeita(idManutencao: Long, tipoServico: TipoServico, kmAtualCarro: Int): Int {
        val db = writableDatabase
        val hoje = Date()
        val dataHojeIso = formatarDataIso(hoje)
        val novaDataProximaIso = calcularProximaDataIso(hoje, tipoServico.mesesProximaValidade)
        val novoKmProximo = kmAtualCarro + tipoServico.kmIntervalo

        val values = ContentValues().apply {
            put(COL_MANUT_DATA_ULTIMA, dataHojeIso)
            put(COL_MANUT_DATA_PROXIMA, novaDataProximaIso)
            put(COL_MANUT_KM_ULTIMA, kmAtualCarro)
            put(COL_MANUT_KM_PROXIMO, novoKmProximo)
        }

        return db.update(
            TABLE_MANUTENCOES,
            values,
            "$COL_MANUT_ID = ?",
            arrayOf(idManutencao.toString())
        )
    }

    // ==========================================
    // MÉTODOS AUXILIARES DE DATA
    // ==========================================

    private fun formatarDataIso(data: Date): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(data)
    }

    private fun calcularProximaDataIso(dataBase: Date, mesesAdicionais: Int): String {
        val calendar = Calendar.getInstance()
        calendar.time = dataBase
        calendar.add(Calendar.MONTH, mesesAdicionais)
        return formatarDataIso(calendar.time)
    }
}
