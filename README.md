# Verticalize

App Android para organizar os estudos para concursos públicos a partir do **edital verticalizado**: o conteúdo do edital quebrado em tópicos, com o progresso de cada um e o registro das horas estudadas.

## Proposta de criação do projeto

Projeto criado para estudo de implementação do zero utilizando Claude Code. Utilizado o pattern AI Assisted Development (AIAD)

## Funcionalidades

### Concursos
- Cadastro de concursos com nome, nível, data da prova (opcional), banca e valor da inscrição.
- Lista ordenada pela data da prova (sem data por último).
- Edição e exclusão com gestos de swipe (direita edita, esquerda exclui, com confirmação).

### Conteúdos do edital
- Tópicos com matéria, descrição, eixo, bloco, quantidade de aulas, tempo médio por aula, data da última revisão, questões resolvidas e prioridade (alta, média, baixa, opcional).
- Marcação de conteúdo concluído direto na lista.
- **Filtros** por eixo, bloco e prioridade; **ordenação** por prioridade, eixo, bloco, revisão ou tempo de aula.
- **Agrupamento** por eixo, bloco ou matéria, com grupos retráteis (começam retraídos).
- Cabeçalho do concurso com contagem regressiva para a prova, percentual de conclusão geral e por eixo, horas de aula totais/assistidas e horas estudadas.

### Horas de estudo
- Registro manual (matéria, data, hora de início e fim) ou por **cronômetro**.
- Visualização em lista ou em **calendário** mensal.
- Cronômetro global (um por vez) com pausar, retomar, registrar e descartar, sincronizado entre:
  - o card dentro do app;
  - uma **notificação** persistente (serviço em primeiro plano);
  - um **widget** de tela inicial (Jetpack Glance), que também permite iniciar um novo estudo.

### Seleção múltipla
- Toque longo em um conteúdo ou registro de estudo entra no modo de seleção.
- Barra contextual com "selecionar todos" e exclusão em lote, com confirmação mostrando a quantidade de itens.

### Importação e exportação
- Importa concursos, conteúdos e horários de estudo de um arquivo `.txt` (menu ⋮ da lista de concursos), com pré-visualização, detecção de duplicados e relatório de erros por linha.
- Exporta todos os concursos ou um concurso específico no mesmo formato, permitindo backup e reimportação.
- Exemplos do formato em [`docs/exemplo-importacao.txt`](docs/exemplo-importacao.txt) e [`docs/exemplo-importacao-com-erros.txt`](docs/exemplo-importacao-com-erros.txt).

### Conta e sincronização (opcional)
- O app funciona totalmente offline e sem conta.
- Login com Google (Credential Manager + Firebase Authentication) habilita o backup e a sincronização dos dados no Firebase Realtime Database.
- As alterações locais (inclusive exclusões) são registradas por triggers no banco e enviadas à nuvem; quando os dados do aparelho e da nuvem divergem no login, o usuário escolhe qual manter.

## Stack

- **Kotlin** + **Jetpack Compose** (Material 3), arquitetura **MVI** (State / Action / Event por tela)
- **Room** (banco local, com migrações versionadas em `app/schemas`)
- **Koin** (injeção de dependência)
- **Navigation 3**
- **Jetpack Glance** (widget)
- **Firebase** Authentication e Realtime Database
- Testes: **JUnit 5**, **Turbine**, **AssertK**

`minSdk 28` · `targetSdk 37`

## Estrutura

O código fica em `app/src/main/java/br/com/zamfir/verticalize`, organizado por funcionalidade, cada uma dividida em `data`, `domain` e `presentation`:

| Pacote | Conteúdo |
|---|---|
| `concurso` | Lista e detalhes do concurso |
| `conteudo` | Conteúdos do edital, filtros, ordenação e agrupamento |
| `registroestudo` | Horas de estudo, calendário, cronômetro, notificação e widget |
| `importacao` | Leitura, validação e gravação dos arquivos `.txt` |
| `auth` | Login com Google |
| `sync` | Backup e sincronização com o Firebase |
| `core` | Banco, DI, `Result`/erros e componentes visuais compartilhados |
