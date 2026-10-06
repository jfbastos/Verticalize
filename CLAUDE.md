# CLAUDE.md

## Fluxo de validação de novas funcionalidades

O usuário testa manualmente, ele mesmo, todo novo recurso de UI/comportamento antes de considerá-lo pronto (rodando o app, navegando pelas telas, etc.). Por isso:

- Depois de implementar uma mudança, valide apenas o que dá para verificar sem rodar o app: compilação (`./gradlew compileDebugKotlin` ou equivalente), lint/testes automatizados existentes.
- Não tente rodar o app, tirar screenshots ou simular a interação do usuário para "confirmar" que a funcionalidade funciona, a menos que ele peça explicitamente.
- Ao concluir, deixe claro no resumo o que foi validado (build) e o que fica para o teste manual dele.
