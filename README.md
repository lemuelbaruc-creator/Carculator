# CARculator

Aplicativo Android nativo, 100% offline e de baixo consumo de recursos, desenvolvido para o cálculo de consumo de combustível em viagens e o controle de manutenção preventiva veicular. A interface foi projetada com foco em acessibilidade e alta legibilidade, atendendo especialmente motoristas profissionais e usuários idosos.

## Funcionalidades

- **Painel Rápido (Calcular):** Calcula a estimativa de litros necessários e o custo total da viagem com base nos modos de condução Cidade, Estrada ou Misto (média aritmética automática). Inclui atalho direto para consulta de distância no Google Maps.
- **Meus Carros:** Permite o cadastro manual de veículos com definição do consumo real (cidade e estrada), quilometragem atual e seleção de veículo principal.
- **Agenda de Manutenção:** Gerencia itens de manutenção preventiva (Troca de Óleo, Troca de Pneus, Alinhamento/Balanceamento e Revisão Geral) combinando critérios de quilometragem e data (prevalecendo o que vencer primeiro). Exibe indicadores de status (Em dia, Atenção e Vencido) e permite o registro rápido de conclusão com recálculo automático do próximo ciclo.
- **Acessibilidade e Segurança:** Elementos interativos com área mínima de toque de 60dp, tipografia ampliada, alto contraste, modo imersivo para otimização de tela e alerta inicial de segurança contra o uso do dispositivo ao volante.

## Tecnologias Utilizadas

- **Linguagem:** Kotlin
- **Interface:** Jetpack Compose e Material 3
- **Persistência de Dados:** SQLite nativo (`SQLiteOpenHelper`), com armazenamento integralmente local
- **Integração de Mapas:** Intent nativa do Android para o aplicativo Google Maps
- **Ambiente de Desenvolvimento:** Android Studio

## Como Executar

1. Clone o repositório:
   ```bash
   git clone [https://github.com/lemuelbaruc-creator/Carculator.git](https://github.com/lemuelbaruc-creator/carculator.git)
