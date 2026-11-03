import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Scanner;

/** Classe principal do domínio: mantém os cadastros, executa operações e persiste os dados. */
public class Pousada {
    private String nome;
    private String contato;
    private final ArrayList<Quarto> quartos;
    private final ArrayList<Reserva> reservas;
    private final ArrayList<Produto> produtos;
    private final Scanner entrada;
    private boolean alteracoesPendentes;

    public Pousada() {
        this("Pousada", "");
        carregaDados();
    }

    public Pousada(String nome, String contato) {
        this.nome = nome == null || nome.trim().isEmpty() ? "Pousada" : limparCampo(nome);
        this.contato = contato == null ? "" : limparCampo(contato);
        this.quartos = new ArrayList<>();
        this.reservas = new ArrayList<>();
        this.produtos = new ArrayList<>();
        this.entrada = new Scanner(System.in);
        this.alteracoesPendentes = false;
    }

    public String getNome() { return nome; }
    public String getContato() { return contato; }
    public ArrayList<Quarto> getQuartos() { return new ArrayList<>(quartos); }
    public ArrayList<Reserva> getReservas() { return new ArrayList<>(reservas); }
    public ArrayList<Produto> getProdutos() { return new ArrayList<>(produtos); }

    private static String limparCampo(String valor) {
        return valor.trim().replace('\t', ' ').replace('\n', ' ').replace('\r', ' ');
    }

    public String serializar() {
        return limparCampo(nome) + "\t" + limparCampo(contato);
    }

    public static Pousada deserializar(String linha) {
        String[] campos = linha.split("\\t", -1);
        String nomeLido = campos.length > 0 ? campos[0].trim() : "Pousada";
        String contatoLido = campos.length > 1 ? campos[1].trim() : "";
        return new Pousada(nomeLido, contatoLido);
    }

    /** Carrega os arquivos tabulados. Produtos são lidos antes dos quartos para reconstruir consumos. */
    public final void carregaDados() {
        quartos.clear();
        reservas.clear();
        produtos.clear();

        Path arquivoPousada = Paths.get("pousada.txt");
        try {
            List<String> linhas = lerLinhas(arquivoPousada);
            if (!linhas.isEmpty()) {
                Pousada dadosPousada = Pousada.deserializar(linhas.get(0));
                nome = dadosPousada.nome;
                contato = dadosPousada.contato;
            }
        } catch (IOException erro) {
            System.out.println("Aviso: não foi possível ler pousada.txt: " + erro.getMessage());
        }

        Map<Integer, Produto> produtosPorCodigo = new HashMap<>();
        try {
            for (String linha : lerLinhas(Paths.get("produto.txt"))) {
                try {
                    Produto produto = Produto.deserializar(linha);
                    if (produtosPorCodigo.containsKey(produto.getCodigo())) {
                        System.out.println("Aviso: código de produto duplicado ignorado: " + produto.getCodigo());
                        continue;
                    }
                    produtos.add(produto);
                    produtosPorCodigo.put(produto.getCodigo(), produto);
                } catch (RuntimeException erro) {
                    System.out.println("Aviso: produto ignorado (" + erro.getMessage() + ").");
                }
            }
        } catch (IOException erro) {
            System.out.println("Aviso: não foi possível ler produto.txt: " + erro.getMessage());
        }

        Map<Integer, Quarto> quartosPorNumero = new HashMap<>();
        try {
            for (String linha : lerLinhas(Paths.get("quarto.txt"))) {
                try {
                    Quarto quarto = Quarto.deserializar(linha, produtosPorCodigo);
                    if (quartosPorNumero.containsKey(quarto.getNumero())) {
                        System.out.println("Aviso: número de quarto duplicado ignorado: " + quarto.getNumero());
                        continue;
                    }
                    quartos.add(quarto);
                    quartosPorNumero.put(quarto.getNumero(), quarto);
                } catch (RuntimeException erro) {
                    System.out.println("Aviso: quarto ignorado (" + erro.getMessage() + ").");
                }
            }
        } catch (IOException erro) {
            System.out.println("Aviso: não foi possível ler quarto.txt: " + erro.getMessage());
        }

        try {
            for (String linha : lerLinhas(Paths.get("reserva.txt"))) {
                try {
                    Reserva reserva = Reserva.deserializar(linha, quartosPorNumero);
                    reservas.add(reserva);
                } catch (RuntimeException erro) {
                    System.out.println("Aviso: reserva ignorada (" + erro.getMessage() + ").");
                }
            }
        } catch (IOException erro) {
            System.out.println("Aviso: não foi possível ler reserva.txt: " + erro.getMessage());
        }

        alteracoesPendentes = false;
        System.out.println("Dados carregados: " + quartos.size() + " quarto(s), " + produtos.size()
                + " produto(s) e " + reservas.size() + " reserva(s).\n");
    }

    private List<String> lerLinhas(Path caminho) throws IOException {
        if (!Files.exists(caminho)) return new ArrayList<>();
        List<String> linhas = Files.readAllLines(caminho, StandardCharsets.UTF_8);
        ArrayList<String> validas = new ArrayList<>();
        for (String linha : linhas) {
            if (linha != null && !linha.trim().isEmpty() && !linha.trim().startsWith("#")) {
                validas.add(linha);
            }
        }
        return validas;
    }

    /** Salva os cadastros e elimina reservas canceladas ou com checkout concluído. */
    public void salvaDados() {
        try {
            Files.write(Paths.get("pousada.txt"), List.of(serializar()), StandardCharsets.UTF_8);

            ArrayList<String> linhasProdutos = new ArrayList<>();
            for (Produto produto : produtos) linhasProdutos.add(produto.serializar());
            Files.write(Paths.get("produto.txt"), linhasProdutos, StandardCharsets.UTF_8);

            ArrayList<String> linhasQuartos = new ArrayList<>();
            for (Quarto quarto : quartos) linhasQuartos.add(quarto.serializar());
            Files.write(Paths.get("quarto.txt"), linhasQuartos, StandardCharsets.UTF_8);

            ArrayList<String> linhasReservas = new ArrayList<>();
            for (Reserva reserva : reservas) {
                if (reserva.getStatus() != 'C' && reserva.getStatus() != 'O') {
                    linhasReservas.add(reserva.serializar());
                }
            }
            Files.write(Paths.get("reserva.txt"), linhasReservas, StandardCharsets.UTF_8);

            Iterator<Reserva> iteradorReservas = reservas.iterator();
            while (iteradorReservas.hasNext()) {
                Reserva reserva = iteradorReservas.next();
                if (reserva.getStatus() == 'C' || reserva.getStatus() == 'O') {
                    iteradorReservas.remove();
                }
            }
            alteracoesPendentes = false;
            System.out.println("Dados salvos com sucesso nos arquivos de texto.\n");
        } catch (IOException erro) {
            alteracoesPendentes = true;
            System.out.println("Erro ao salvar os dados: " + erro.getMessage());
        }
    }

    /** Verifica se um quarto está livre na data consultada e exibe seu cadastro. */
    public boolean consultaDisponibilidade(Data data, Quarto quarto) {
        if (data == null || quarto == null) {
            System.out.println("Data ou quarto inválido.");
            return false;
        }
        for (Reserva reserva : reservas) {
            if (reserva.estaAtiva() && reserva.getQuarto().getNumero() == quarto.getNumero()
                    && reserva.incluiData(data)) {
                System.out.println("Quarto indisponível em " + data + ". Reserva encontrada: " + reserva);
                return false;
            }
        }
        System.out.println("Quarto disponível em " + data + ": " + quarto);
        return true;
    }

    /** Consulta reservas ativas usando um ou mais filtros opcionais. */
    public void consultaReserva(Data data, String cliente, Quarto quarto) {
        boolean encontrouFiltro = data != null || (cliente != null && !cliente.trim().isEmpty()) || quarto != null;
        if (!encontrouFiltro) {
            System.out.println("Informe ao menos um filtro para consultar reservas.");
            return;
        }

        boolean encontrou = false;
        for (Reserva reserva : reservas) {
            if (!reserva.estaAtiva()) continue;
            if (data != null && !reserva.incluiData(data)) continue;
            if (cliente != null && !cliente.trim().isEmpty()
                    && !reserva.getCliente().equalsIgnoreCase(cliente.trim())) continue;
            if (quarto != null && reserva.getQuarto().getNumero() != quarto.getNumero()) continue;
            System.out.println(reserva);
            encontrou = true;
        }
        if (!encontrou) System.out.println("Nenhuma reserva ativa corresponde aos filtros informados.");
    }

    /** Cria reserva se o período estiver livre e o cliente não possuir reserva ativa ou check-in. */
    public boolean realizaReserva(Data dtIni, Data dtFim, String cliente, Quarto quarto) {
        if (dtIni == null || dtFim == null || dtIni.compareTo(dtFim) >= 0) {
            System.out.println("Período inválido. A data final deve ser posterior à inicial.");
            return false;
        }
        if (cliente == null || cliente.trim().isEmpty() || quarto == null) {
            System.out.println("Cliente e quarto são obrigatórios.");
            return false;
        }

        for (Reserva reserva : reservas) {
            if (reserva.estaAtiva() && reserva.getCliente().equalsIgnoreCase(cliente.trim())) {
                System.out.println("Não foi possível reservar: o cliente já possui uma reserva ativa ou está em check-in.");
                return false;
            }
            if (reserva.estaAtiva() && reserva.getQuarto().getNumero() == quarto.getNumero()
                    && reserva.sobrepoePeriodo(dtIni, dtFim)) {
                System.out.println("Não foi possível reservar: o quarto está ocupado em parte do período solicitado.");
                return false;
            }
        }

        reservas.add(new Reserva(dtIni, dtFim, cliente, quarto, 'A'));
        alteracoesPendentes = true;
        System.out.println("Reserva realizada com sucesso:\n" + reservas.get(reservas.size() - 1));
        return true;
    }

    public void cancelaReserva(String cliente) {
        if (cliente == null || cliente.trim().isEmpty()) {
            System.out.println("Informe o nome do cliente.");
            return;
        }
        for (Reserva reserva : reservas) {
            if (reserva.getStatus() == 'A' && reserva.getCliente().equalsIgnoreCase(cliente.trim())) {
                reserva.setStatus('C');
                alteracoesPendentes = true;
                System.out.println("Reserva cancelada: " + reserva);
                return;
            }
        }
        System.out.println("Não existe reserva ativa para esse cliente.");
    }

    public void realizaCheckIn(String cliente) {
        Reserva reserva = localizarReservaCliente(cliente, 'A');
        if (reserva == null) {
            System.out.println("Não existe reserva ativa para realizar o check-in desse cliente.");
            return;
        }
        reserva.setStatus('I');
        alteracoesPendentes = true;
        mostrarResumoHospedagem(reserva);
        System.out.println("Check-in realizado com sucesso.");
    }

    public void realizaCheckOut(String cliente) {
        Reserva reserva = localizarReservaCliente(cliente, 'I');
        if (reserva == null) {
            System.out.println("Não existe reserva com check-in ativo para esse cliente.");
            return;
        }

        mostrarResumoHospedagem(reserva);
        System.out.println("Consumo do quarto:");
        List<Produto> consumo = reserva.getQuarto().listaConsumo();
        if (consumo.isEmpty()) {
            System.out.println("  Nenhum consumo registrado.");
        } else {
            for (Produto produto : consumo) System.out.println("  " + produto);
        }

        double totalDiarias = reserva.valorTotalDiarias();
        double totalConsumo = reserva.getQuarto().valorTotalConsumo();
        System.out.printf(Locale.forLanguageTag("pt-BR"), "Total das diárias: R$ %.2f%n", totalDiarias);
        System.out.printf(Locale.forLanguageTag("pt-BR"), "Total do consumo: R$ %.2f%n", totalConsumo);
        System.out.printf(Locale.forLanguageTag("pt-BR"), "VALOR FINAL A PAGAR: R$ %.2f%n", totalDiarias + totalConsumo);

        reserva.setStatus('O');
        reserva.getQuarto().limpaConsumo();
        alteracoesPendentes = true;
        System.out.println("Check-out registrado. A reserva será removida do arquivo ao salvar.");
    }

    public void registraConsumo(String cliente, int codigoProduto) {
        Reserva reserva = localizarReservaCliente(cliente, 'I');
        if (reserva == null) {
            System.out.println("Não existe reserva com check-in ativo para esse cliente.");
            return;
        }
        Produto produto = localizarProduto(codigoProduto);
        if (produto == null) {
            System.out.println("Código de produto não encontrado.");
            return;
        }
        reserva.getQuarto().adicionaConsumo(produto);
        alteracoesPendentes = true;
        System.out.println("Consumo registrado: " + produto.getNome() + " (1 unidade).");
        System.out.printf(Locale.forLanguageTag("pt-BR"), "Total atual do consumo: R$ %.2f%n",
                reserva.getQuarto().valorTotalConsumo());
    }

    private void mostrarResumoHospedagem(Reserva reserva) {
        System.out.println("Resumo da hospedagem:");
        System.out.println("Cliente: " + reserva.getCliente());
        System.out.println("Data inicial: " + reserva.getDtInicio());
        System.out.println("Data final: " + reserva.getDtFim());
        System.out.println("Quantidade de diárias: " + reserva.quantidadeDias());
        System.out.println(reserva.getQuarto());
        System.out.printf(Locale.forLanguageTag("pt-BR"), "Valor total das diárias: R$ %.2f%n",
                reserva.valorTotalDiarias());
    }

    private Reserva localizarReservaCliente(String cliente, char status) {
        if (cliente == null || cliente.trim().isEmpty()) return null;
        for (Reserva reserva : reservas) {
            if (reserva.getStatus() == status && reserva.getCliente().equalsIgnoreCase(cliente.trim())) return reserva;
        }
        return null;
    }

    private Produto localizarProduto(int codigo) {
        for (Produto produto : produtos) if (produto.getCodigo() == codigo) return produto;
        return null;
    }

    private Quarto localizarQuarto(int numero) {
        for (Quarto quarto : quartos) if (quarto.getNumero() == numero) return quarto;
        return null;
    }

    /** Exibe o menu e encaminha cada opção para seu método correspondente. */
    public void menu() {
        boolean sair = false;
        while (!sair) {
            System.out.println("========================================");
            System.out.println("          " + nome.toUpperCase(Locale.ROOT));
            System.out.println("========================================");
            System.out.println("1 - Consultar disponibilidade");
            System.out.println("2 - Consultar reserva");
            System.out.println("3 - Realizar reserva");
            System.out.println("4 - Cancelar reserva");
            System.out.println("5 - Realizar check-in");
            System.out.println("6 - Realizar check-out");
            System.out.println("7 - Registrar consumo");
            System.out.println("8 - Salvar");
            System.out.println("0 - Sair");
            int opcao = lerInteiro("Escolha uma opção: ");
            System.out.println();

            switch (opcao) {
                case 1: menuConsultarDisponibilidade(); break;
                case 2: menuConsultarReserva(); break;
                case 3: menuRealizarReserva(); break;
                case 4: cancelaReserva(lerTexto("Nome do cliente: ")); break;
                case 5: realizaCheckIn(lerTexto("Nome do cliente: ")); break;
                case 6: realizaCheckOut(lerTexto("Nome do cliente: ")); break;
                case 7: menuRegistrarConsumo(); break;
                case 8: salvaDados(); break;
                case 0:
                    if (alteracoesPendentes) {
                        System.out.println("Há alterações não salvas. Salvando automaticamente...");
                        salvaDados();
                    }
                    System.out.println("Sistema encerrado.");
                    sair = true;
                    break;
                default: System.out.println("Opção inválida. Escolha uma opção do menu.\n");
            }
        }
    }

    private void menuConsultarDisponibilidade() {
        Data data = lerData("Data para consulta (dd/MM/aaaa): ");
        int numeroQuarto = lerInteiro("Número do quarto: ");
        Quarto quarto = localizarQuarto(numeroQuarto);
        if (quarto == null) {
            System.out.println("Quarto não encontrado.\n");
            return;
        }
        consultaDisponibilidade(data, quarto);
        System.out.println();
    }

    private void menuConsultarReserva() {
        System.out.println("Deixe um campo vazio para não utilizá-lo como filtro.");
        String textoData = lerTexto("Data (dd/MM/aaaa): ");
        Data data = null;
        if (!textoData.isEmpty()) {
            try {
                data = Data.deserializar(textoData);
            } catch (RuntimeException erro) {
                System.out.println("Data inválida. Consulta cancelada.\n");
                return;
            }
        }
        String cliente = lerTexto("Nome do cliente: ");
        String textoQuarto = lerTexto("Número do quarto: ");
        Quarto quarto = null;
        if (!textoQuarto.isEmpty()) {
            try {
                quarto = localizarQuarto(Integer.parseInt(textoQuarto));
                if (quarto == null) {
                    System.out.println("Quarto não encontrado.\n");
                    return;
                }
            } catch (NumberFormatException erro) {
                System.out.println("Número do quarto inválido.\n");
                return;
            }
        }
        consultaReserva(data, cliente, quarto);
        System.out.println();
    }

    private void menuRealizarReserva() {
        Data dataInicio = lerData("Data inicial (dd/MM/aaaa): ");
        Data dataFim = lerData("Data final / checkout (dd/MM/aaaa): ");
        String cliente = lerTexto("Nome do cliente: ");
        listarQuartos();
        int numeroQuarto = lerInteiro("Número do quarto desejado: ");
        Quarto quarto = localizarQuarto(numeroQuarto);
        if (quarto == null) {
            System.out.println("Quarto não encontrado.\n");
            return;
        }
        realizaReserva(dataInicio, dataFim, cliente, quarto);
        System.out.println();
    }

    private void menuRegistrarConsumo() {
        String cliente = lerTexto("Nome do cliente: ");
        Reserva reserva = localizarReservaCliente(cliente, 'I');
        if (reserva == null) {
            System.out.println("Não existe reserva com check-in ativo para esse cliente.\n");
            return;
        }
        if (produtos.isEmpty()) {
            System.out.println("Não há produtos cadastrados.\n");
            return;
        }
        System.out.println("Produtos disponíveis (cada registro adiciona uma unidade):");
        for (Produto produto : produtos) System.out.println("  " + produto);
        int codigo = lerInteiro("Código do produto consumido: ");
        registraConsumo(cliente, codigo);
        System.out.println();
    }

    private void listarQuartos() {
        if (quartos.isEmpty()) {
            System.out.println("Nenhum quarto cadastrado.");
            return;
        }
        System.out.println("Quartos cadastrados:");
        for (Quarto quarto : quartos) System.out.println("  " + quarto);
    }

    private String lerTexto(String mensagem) {
        System.out.print(mensagem);
        return entrada.nextLine().trim();
    }

    private int lerInteiro(String mensagem) {
        while (true) {
            String texto = lerTexto(mensagem);
            try {
                return Integer.parseInt(texto);
            } catch (NumberFormatException erro) {
                System.out.println("Digite um número inteiro válido.");
            }
        }
    }

    private Data lerData(String mensagem) {
        while (true) {
            String texto = lerTexto(mensagem);
            try {
                return Data.deserializar(texto);
            } catch (RuntimeException erro) {
                System.out.println("Data inválida. Use dd/MM/aaaa e informe uma data existente.");
            }
        }
    }
}
