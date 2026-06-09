# Gold Miners Expandido em JaCaMo/Jason

Esta pasta contem uma implementacao nova e isolada do Gold Miners. Nenhum arquivo das pastas `001`, `002` ou `003` foi alterado.

## Execucao

```bash
cd "Projeto Final/004 - Implementação Gold Miners - Adições, Expansões e Melhorias"
gradle run
```

O projeto usa JaCaMo `1.3.0` via Gradle. Em ambientes sem display grafico, a simulacao continua rodando em modo headless e registra a mensagem `GUI disabled: headless Java environment`. Em ambiente com X11/display, a janela Swing `Expanded Gold Miners` e aberta automaticamente.

## Arquitetura

- `gold_miners_expanded.jcm`: define um leader e quatro miners, cada miner focando seu proprio artefato CArtAgO.
- `src/agt/miner_ext.asl`: comportamento comum dos miners.
- `src/agt/leader_ext.asl`: placar, vencedor e logs de coordenacao.
- `src/agt/jia_ext/*.java`: acoes internas Jason para distancia, direcao, identificacao do miner, sorteio, vizinhanca e desempate.
- `src/env/mining/expanded/ExpandedMiningPlanet.java`: artefato CArtAgO exposto aos agentes.
- `src/env/mining/expanded/ExpandedWorldModel.java`: estado global da simulacao.
- `src/env/mining/expanded/ExpandedWorldView.java`: visualizacao Swing.

## Tempo

O ambiente mantem um relogio global em ticks. As acoes externas consomem ticks no `ExpandedWorldModel`.

| Acao | Custo |
| --- | ---: |
| mover sem carrinho | 1 tick |
| mover com carrinho | 2 ticks |
| enviar broadcast | 1 tick |
| extrair ouro | 2 ticks |
| depositar ouro | 1 tick |
| equipar/desequipar na base | 1 tick |
| skip | 1 tick |

## Inventario e equipamentos

Cada miner possui 2 espacos de equipamento. GPS e comunicador sao permanentes e nao ocupam inventario.

O ouro e tratado como carga separada dos equipamentos para permitir que um miner especializado com lanterna/carrinho ainda consiga transportar ouro.

- Capacidade base de ouro: 1.
- Lanterna: ocupa 1 espaco; aumenta raio de visao de 1 para 3; nao altera velocidade.
- Carrinho: ocupa 1 espaco; adiciona 4 de capacidade de ouro; movimento custa 2 ticks.
- Mochila: ocupa 1 espaco; adiciona 2 de capacidade de ouro; nao altera velocidade.

Equipamentos nunca ficam abandonados no mapa. Eles so podem estar equipados por um miner ou armazenados na base. Trocas sao aceitas apenas na celula do depot.

Distribuicao inicial:

- `miner1`: lanterna.
- `miner2`: carrinho.
- `miner3`: mochila.
- `miner4`: lanterna e mochila.
- Base: 1 lanterna, 1 carrinho, 1 mochila.

## Comunicacao e coordenacao

Toda comunicacao entre miners usa broadcast. Antes de cada `.broadcast`, o miner executa `comm_tick(...)`, consumindo 1 tick.

Quando um miner percebe ouro:

1. Anuncia `gold_found(X,Y,Finder)` por broadcast.
2. Todos calculam distancia ate a coordenada usando GPS.
3. Cada miner publica `gold_bid(X,Y,Miner,Distance)`.
4. Quando os quatro bids estao conhecidos, todos calculam o mesmo vencedor por `jia_ext.best_bid`.
5. O menor valor de distancia vence; empate e resolvido pelo menor identificador numerico.
6. Apenas o vencedor anuncia `mission_claimed(X,Y,Miner)` e executa `!handle(gold(X,Y))`.
7. Os demais removem/desistem daquela jazida.

Se a coleta falhar, o miner anuncia `mission_cancelled(X,Y,Reason)` para liberar a jazida.

## Visualizacao

A GUI foi implementada em Swing para manter compatibilidade com o projeto original, que ja usa `GridWorldView`. A janela mostra:

- mapa, base, obstaculos, ouro e agentes;
- equipamento visual por cor;
- tick global;
- ouro depositado;
- estoque de equipamentos na base;
- carga, score e equipamentos de cada miner;
- ultimo evento relevante.

## Observacoes

Os custos e capacidades ficam centralizados em `src/env/mining/expanded/Config.java`.

O projeto atual prioriza clareza pedagogica de coordenacao distribuida. O leader nao escolhe qual miner vai ao ouro; ele observa claims, cancelamentos e placar.
