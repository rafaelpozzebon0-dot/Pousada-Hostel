# Gerenciador básico de pousada (Java)

Projeto de console em Java, organizado em classes e sem dependências externas.

## Requisitos

- JDK 11 ou superior.
- Executar a partir desta pasta, onde ficam os arquivos `.txt`.

## Compilar e executar

```bash
javac *.java
java Main
```

## Arquivos de dados

Os arquivos são texto UTF-8 com campos separados por tabulação:

- `pousada.txt`: `nome<TAB>contato`
- `quarto.txt`: `numero<TAB>categoria(S/M/P)<TAB>diaria<TAB>codigosDeConsumo`
- `produto.txt`: `codigo<TAB>nome<TAB>preco`
- `reserva.txt`: `dataInicial<TAB>dataFinal<TAB>cliente<TAB>numeroQuarto<TAB>status(A/C/I/O)`

As datas usam `dd/MM/aaaa`. A data final é o dia de checkout: não gera diária e pode ser a data de início de outra reserva. O total de diárias é a diferença em dias entre início e fim.

Ao salvar, reservas canceladas (`C`) e com checkout concluído (`O`) são removidas de `reserva.txt`. Ao sair, alterações pendentes são salvas automaticamente.

## Classes

- `Pousada`: menu, operações e persistência.
- `Reserva`: datas, cliente, quarto e status.
- `Data`: validação e operações com datas.
- `Quarto`: categoria, valor da diária e consumos.
- `Produto`: catálogo de produtos.
- `Main`: instancia a pousada e inicia o menu.
# Pousada-Hostel
