# Trabalho Prático Final — Gold Miners

Projeto desenvolvido para a disciplina de **Sistemas Multiagentes**, utilizando o cenário **Gold Miners** e as tecnologias **Jason**, **JaCaMo**, **CArtAgO** e **Moise**.

O trabalho parte do cenário original de mineração e adiciona novas funcionalidades aos agentes e ao ambiente, incluindo:

* múltiplos tipos de minério com valores diferentes;
* cronômetro de partida;
* comunicação por rádio com alcance limitado;
* lanterna para ampliação do campo de visão;
* mochila para transporte de múltiplos minérios;
* organização dos agentes em equipes e papéis;
* competição entre equipes com diferentes estratégias.

## Estrutura do repositório

| Diretório            | Conteúdo                                                      |
| -------------------- | ------------------------------------------------------------- |
| `001 - Documentação` | Relatórios, apresentação e demais documentos do projeto.      |
| `002 - Soluções`     | Soluções dos exercícios e atividades iniciais do Gold Miners. |
| `003 - IGM`          | Implementações incrementais das novas funcionalidades.        |
| `004 - GMExpanded`   | Versão expandida do cenário Gold Miners.                      |
| `Projeto Final`      | Arquivos relacionados à integração e aos testes finais.       |
| `Versão Final`       | Versão completa utilizada na competição entre as equipes.     |

## Estratégias das equipes

A versão final possui três equipes, cada uma formada por um `explorer` e dois `retrievers`:

* **Equipe Vermelha:** exploração aleatória e cooperação por proximidade;
* **Equipe Azul:** exploração sistemática e diferentes políticas de uso da mochila;
* **Equipe Verde:** priorização de ouro e diamante.

As equipes utilizam a mesma infraestrutura de agentes e ambiente, diferenciando-se principalmente pelos planos AgentSpeak associados aos papéis organizacionais.

## Execução

Para executar o projeto, acesse a pasta da versão desejada e utilize:

```bash
./gradlew run
```

É necessário possuir uma instalação compatível do **Java** e do **JaCaMo**.

## Autores

* Daniel Victor Krepsky
* João Pedro da Silva Cardoso
* Rafael Bertolini Pereira Silva

Referências

BOISSIER, Olivier; BORDINI, Rafael H.; HÜBNER, Jomi F.; RICCI, Alessandro. Multi-agent oriented programming: programming multi-agent systems using JaCaMo. Cambridge: MIT Press, 2020.

BORDINI, Rafael H.; HÜBNER, Jomi F.; WOOLDRIDGE, Michael. Programming multi-agent systems in AgentSpeak using Jason. Chichester: John Wiley & Sons, 2007.

HÜBNER, Jomi F.; SICHMAN, Jaime S.; BOISSIER, Olivier. A model for the structural, functional, and deontic specification of organizations in multiagent systems. In: SBIA 2002. Berlin: Springer, 2002. p. 118–128.

JACAMO TEAM. Gold Miners Tutorial. Disponível em: https://jacamo-lang.github.io/jacamo/tutorials/gold-miners/readme.html. Acesso em: 14 jul. 2026.

LEITE, João. Gold Miners: tutorial JaCaMo. WESAAC 2014. Material didático fornecido na disciplina.

WOOLDRIDGE, Michael. An introduction to multiagent systems. 2. ed. Chichester: John Wiley & Sons, 2009.
