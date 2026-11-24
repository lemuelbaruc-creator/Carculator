#  CARculator

Aplicativo Android nativo, **100% offline e ultra leve**, desenvolvido para ajudar motoristas (com foco em acessibilidade para caminhoneiros e idosos) a calcular gastos de viagem e controlar a manutenção preventiva do veículo sem complicação.

##  Funcionalidades

- **Painel Rápido (Calcular):** Calcula litros necessários e custo estimado da viagem nos modos **Cidade**, **Estrada** ou **Misto** (média automática), com atalho rápido para o Google Maps.
- **Meus Carros:** Cadastro manual de veículos com consumo real (cidade/estrada) e quilometragem atual.
- **Agenda de Manutenção Inteligente:** Controle de Troca de Óleo, Pneus, Alinhamento/Balanceamento e Revisão Geral baseado em **quilometragem ou data** (o que vencer primeiro), com selos visuais (**Verde**, **Amarelo**, **Vermelho**) e botão **"Já fiz"** com renovação automática.
- **Acessibilidade e Segurança:** Botões grandes (≥ 60dp), fontes legíveis, alto contraste, modo tela cheia imersiva e alerta inicial de segurança no trânsito.

## Tecnologias Utilizadas

- **Linguagem:** Kotlin
- **Interface (UI):** Jetpack Compose + Material 3
- **Banco de Dados:** SQLite nativo (`SQLiteOpenHelper`) — armazenamento 100% local
- **Integração de Mapas:** Intent nativa do Android para o Google Maps
- **IDE:** Android Studio

##  Como executar o projeto

1. Clone este repositório:
   ```bash
   git clone [https://github.com/SEU-USUARIO/CARculator.git](https://github.com/SEU-USUARIO/CARculator.git)