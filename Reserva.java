import java.util.Locale;
import java.util.Map;

/** Reserva de um quarto por um cliente. */
public class Reserva {
    private Data dtInicio;
    private Data dtFim;
    private String cliente;
    private Quarto quarto;
    private char status;

    public Reserva(Data dtInicio, Data dtFim, String cliente, Quarto quarto, char status) {
        setDtInicio(dtInicio);
        setDtFim(dtFim);
        setCliente(cliente);
        setQuarto(quarto);
        setStatus(status);
        if (dtInicio.compareTo(dtFim) >= 0) {
            throw new IllegalArgumentException("A data final deve ser posterior à data inicial.");
        }
    }

    public Data getDtInicio() { return dtInicio; }
    public Data getDtFim() { return dtFim; }
    public String getCliente() { return cliente; }
    public Quarto getQuarto() { return quarto; }
    public char getStatus() { return status; }

    public void setDtInicio(Data dtInicio) {
        if (dtInicio == null) throw new IllegalArgumentException("Data inicial obrigatória.");
        this.dtInicio = dtInicio;
    }

    public void setDtFim(Data dtFim) {
        if (dtFim == null) throw new IllegalArgumentException("Data final obrigatória.");
        this.dtFim = dtFim;
    }

    public void setCliente(String cliente) {
        if (cliente == null || cliente.trim().isEmpty()) throw new IllegalArgumentException("O nome do cliente é obrigatório.");
        this.cliente = cliente.trim().replace('\t', ' ').replace('\n', ' ').replace('\r', ' ');
    }

    public void setQuarto(Quarto quarto) {
        if (quarto == null) throw new IllegalArgumentException("Quarto obrigatório.");
        this.quarto = quarto;
    }

    public void setStatus(char status) {
        char statusNormalizado = Character.toUpperCase(status);
        if ("ACIO".indexOf(statusNormalizado) < 0) {
            throw new IllegalArgumentException("Status inválido. Use A, C, I ou O.");
        }
        this.status = statusNormalizado;
    }

    public long quantidadeDias() {
        return dtInicio.diasAte(dtFim);
    }

    public double valorTotalDiarias() {
        return quantidadeDias() * quarto.getDiaria();
    }

    public boolean estaAtiva() {
        return status == 'A' || status == 'I';
    }

    public boolean incluiData(Data data) {
        // A data final é o dia de checkout e não conta como uma diária ocupada.
        return dtInicio.compareTo(data) <= 0 && data.compareTo(dtFim) < 0;
    }

    public boolean sobrepoePeriodo(Data inicio, Data fim) {
        // Intervalos [início, fim): permite checkout e novo check-in no mesmo dia.
        return inicio.compareTo(dtFim) < 0 && dtInicio.compareTo(fim) < 0;
    }

    public String serializar() {
        return dtInicio.serializar() + "\t" + dtFim.serializar() + "\t" + cliente + "\t"
                + quarto.getNumero() + "\t" + status;
    }

    public static Reserva deserializar(String linha, Map<Integer, Quarto> quartosPorNumero) {
        String[] campos = linha.split("\\t", -1);
        if (campos.length < 5) throw new IllegalArgumentException("Linha de reserva incompleta.");
        Data inicio = Data.deserializar(campos[0].trim());
        Data fim = Data.deserializar(campos[1].trim());
        String cliente = campos[2].trim();
        int numeroQuarto = Integer.parseInt(campos[3].trim());
        Quarto quarto = quartosPorNumero.get(numeroQuarto);
        if (quarto == null) throw new IllegalArgumentException("Quarto " + numeroQuarto + " não existe.");
        return new Reserva(inicio, fim, cliente, quarto, campos[4].trim().charAt(0));
    }

    public static String nomeStatus(char status) {
        switch (Character.toUpperCase(status)) {
            case 'A': return "Ativa";
            case 'C': return "Cancelada";
            case 'I': return "Check-in realizado";
            case 'O': return "Check-out realizado";
            default: return "Desconhecido";
        }
    }

    @Override
    public String toString() {
        return String.format(Locale.forLanguageTag("pt-BR"),
                "Cliente: %s | Período: %s a %s | %s | %d diária(s) | Total das diárias: R$ %.2f | Status: %s",
                cliente, dtInicio, dtFim, quarto, quantidadeDias(), valorTotalDiarias(), nomeStatus(status));
    }
}
