# Colecao Postman - PESCD

Arquivos:

- `PESCD.postman_collection.json`: requests separados por perfil.
- `PESCD.postman_environment.json`: variaveis locais para `http://localhost:8080`.

## Como importar

1. No Postman, va em **Import**.
2. Importe `postman/PESCD.postman_collection.json`.
3. Importe `postman/PESCD.postman_environment.json`.
4. Selecione o environment **PESCD Local**.
5. Inicie a aplicacao Spring Boot.

## Roteiro rapido para apresentacao

1. Rode `00 - Publico / Listar ofertas publicas`.
2. Rode `01 - Login / Login Administrador`.
3. Rode `02 - Administrador / Listar usuarios`.
4. Rode `02 - Administrador / Criar usuario de demonstracao`.
5. Rode `02 - Administrador / Buscar usuario criado`.
6. Rode `02 - Administrador / Atualizar usuario criado`.
7. Rode `01 - Login / Login Secretario`.
8. Rode `03 - Secretario / Criar oferta`.
9. Rode `01 - Login / Login Professor`.
10. Rode `05 - Professor Supervisor / Listar alunos supervisionados`.
11. Rode `06 - Professor Responsavel / Listar ofertas do responsavel`.
12. Rode `06 - Professor Responsavel / Buscar detalhe para encerramento`.

Para demonstrar uploads de aluno:

1. Rode `01 - Login / Login Aluno`.
2. Rode `04 - Aluno / Listar ofertas do aluno`.
3. Selecione um PDF no campo `arquivoPlano` ou `arquivo`.
4. Execute `Enviar plano de trabalho`, `Enviar documentacao de aula` ou `Enviar relatorio final`.

Observacao: a autenticacao da API usa sessao/cookie. Ao executar outro login, o Postman passa a usar a sessao do ultimo perfil autenticado.

## Credenciais seed

- Administrador: `admin` / `admin123`
- Secretario: `secretario` / `secretario123`
- Professor: `professor` / `professor123`
- Aluno: `aluno` / `aluno123`

## Variaveis importantes

- `baseUrl`: URL da aplicacao, por padrao `http://localhost:8080`.
- `professorId`: capturada pelo login de professor ou pela listagem de usuarios.
- `ofertaId`: capturada pela listagem publica ou criacao de oferta.
- `usuarioCriadoId`: capturada pela criacao de usuario.
- `inscricaoId`: usada nos uploads de aluno.
- `inscricaoPlanoAprovadoId`: usada no envio de relatorio final.

Se alguma variavel de inscricao ficar vazia, copie o UUID da inscricao correspondente a partir do banco ou de uma resposta JSON que contenha inscricoes.
