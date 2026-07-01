# Evolução do Projeto Gold Miners

O desenvolvimento do projeto foi realizado de forma incremental, com cada versão introduzindo novas funcionalidades aos agentes, ao ambiente e à organização do sistema. A seguir é apresentada uma visão geral da evolução dos componentes implementados.

## Evolução dos Miners

### Miner I

Versão inicial baseada apenas na percepção de ouro (`gold(X,Y)`).

Principais características:

* Percepção de ouro no ambiente;
* Navegação até a posição do ouro;
* Coleta de uma única unidade por vez;
* Entrega do ouro no depósito;
* Notificação simples ao líder através da mensagem `dropped`.

---

### Miner K

Introdução do suporte a múltiplos tipos de minério.

Alterações realizadas:

* Substituição da percepção `gold(X,Y)` por:

  * `cell(X,Y,Type,Value)`
  * `ore(Type,X,Y,Value)`
* Suporte a diferentes recursos, como:

  * Carvão (Coal)
  * Ferro (Iron)
  * Ouro (Gold)
  * Diamante (Diamond)
* Envio do valor coletado ao líder:

```asl
.send(leader,tell,dropped(Value))
```

---

### Miner L

Adição do sistema de controle temporal.

Novas funcionalidades:

* Leitura da percepção `time_left(T)`;
* Tratamento da percepção `time_over`;
* Interrupção das atividades de exploração e coleta quando o tempo se esgota.

---

### Miner M

Implementação do sistema de comunicação por rádio.

Funcionalidades adicionadas:

* Compartilhamento de informações sobre minérios detectados;
* Operação `broadcastOre(...)`;
* Recepção de mensagens:

  * `radio_ore_0`
  * `radio_ore_1`
  * entre outras.

---

### Miner N

Integração do sistema de lanterna.

Características:

* Uso do `LanternArtifact`;
* Ampliação do raio de percepção do agente;
* Lógica de visão ampliada implementada no ambiente.

---

### Miner O

Implementação da mochila (backpack).

Funcionalidades:

* Transporte de múltiplos minérios simultaneamente;
* Respeito à capacidade máxima configurada;
* Descarga completa no depósito;
* Cálculo do valor total transportado.

---

### Miner P

Integração inicial com a organização Moise.

Comportamentos por papel:

#### Explorer

* Explora o ambiente;
* Detecta minérios;
* Compartilha informações.

#### Retriever

* Recebe informações de localização;
* Prioriza a coleta dos minérios.

Além disso, o agente passa a identificar:

* Equipe (`blue`, `red`, etc.);
* Papel organizacional (`explorer`, `retriever`).

---

## Evolução dos Leaders

### Leader K

Atualização da pontuação baseada no valor do minério.

```asl
dropped(Value)
```

Permite que diferentes recursos contribuam com pontuações distintas.

---

### Leader L

Integração com o sistema de tempo.

Responsabilidades:

* Monitorar o `ClockArtifact`;
* Informar os agentes sobre:

  * Tempo restante (`time_left(T)`);
  * Encerramento da partida (`time_over`).

---

### Leader M

Versão mais completa do líder.

Melhorias:

* Mantém pontuação baseada em valor;
* Utiliza broadcast para disseminação de informações;
* Reduz a necessidade de envio individual de mensagens.

---

## Artifacts Desenvolvidos

### ClockArtifact

Responsável pelo cronômetro da simulação.

Funções:

* Criação da propriedade observável `time_left`;
* Atualização automática do tempo restante.

---

### RadioArtifact

Sistema de comunicação por proximidade.

Permite:

* Compartilhamento de informações entre miners;
* Comunicação baseada apenas na distância entre agentes.

---

### RadioArtifactA

Extensão do sistema de rádio com suporte a equipes.

Características:

* Registro de equipes via `registerTeam(...)`;
* Comunicação restrita a membros da mesma equipe.

---

### LanternArtifact

Gerencia os agentes que possuem lanterna.

Utilizado para:

* Aumentar o raio de percepção dos agentes;
* Integrar a visão ampliada ao ambiente.

---

### BackpackArtifact

Responsável pelo gerenciamento das mochilas.

Controla:

* Capacidade máxima;
* Quantidade transportada;
* Integração com os modelos de ambiente da versão B.

---

## Evolução do Ambiente

### WorldModelA

Primeira versão com múltiplos tipos de minério.

Recursos adicionados:

* Coal;
* Iron;
* Gold;
* Diamond;

Cada recurso possui um valor específico associado.

---

### MiningPlanetA

Atualização da geração de percepções.

Formato adotado:

```asl
cell(X,Y,Type,Value)
```

Além disso:

* Integração com o sistema de lanterna;
* Suporte à visão ampliada.

---

### WorldViewA

Melhorias visuais do ambiente.

Inclui:

* Cores distintas para cada minério;
* Indicação visual do minério carregado pelo agente.

---

### WorldModelB

Implementação do sistema de mochila.

Novos recursos:

* Transporte simultâneo de múltiplos minérios;
* Controle de capacidade;
* Cálculo do valor acumulado.

---

### MiningPlanetB

Exposição das informações da mochila aos agentes.

Percepções disponibilizadas:

* Capacidade total;
* Carga atual;
* Valor transportado;
* Último minério coletado.

---

### WorldViewB

Extensão da visualização do ambiente.

Adiciona:

* Exibição da carga atual do agente;
* Indicador visual no formato `carga/capacidade` (ex.: `2/3`).

---

## Organização (Moise)

Foi criada uma estrutura organizacional baseada em equipes e papéis.

### Equipes

* Blue Team
* Red Team

### Papéis

* Explorer
* Retriever

A organização define a estrutura geral de cooperação, enquanto os comportamentos específicos permanecem implementados nos agentes, principalmente em `minerP.asl`.

---

## Estado Atual do Projeto

A configuração mais completa atualmente é composta por:

```text
minerP + leaderM + MiningPlanetB + WorldModelB + WorldViewB
```

### Funcionalidades Disponíveis

* Múltiplos tipos de minério;
* Valores distintos por recurso;
* Sistema de pontuação baseado em valor;
* Cronômetro de partida;
* Comunicação por rádio;
* Lanterna com visão ampliada;
* Mochila com capacidade limitada;
* Organização baseada em equipes e papéis.
