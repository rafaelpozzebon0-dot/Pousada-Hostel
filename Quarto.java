import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Locale;
import java.util.StringJoiner;

/** Quarto da pousada e a lista de itens consumidos pelo hóspede atual. */
public class Quarto {
    private int numero;
    private char categoria;
    private double diaria;
    private final ArrayList<Produto> produtos;

    public Quarto(int numero, char categoria, double diaria) {
        setNumero(numero);
        setCategoria(categoria);
        setDiaria(diaria);
        this.produtos = new ArrayList<>();
    }

    public int getNumero() { return numero; }
    public char getCategoria() { return categoria; }
    public double getDiaria() { return diaria; }

    public void setNumero(int numero) {
        if (numero <= 0) throw new IllegalArgumentException("O número do quarto deve ser positivo.");
        this.numero = numero;
    }

    public void setCategoria(char categoria) {
        char categoriaNormalizada = Character.toUpperCase(categoria);
        if (categoriaNormalizada != 'S' && categoriaNormalizada != 'M' && categoriaNormalizada != 'P') {
            throw new IllegalArgumentException("Categoria inválida. Use S, M ou P.");
        }
        this.categoria = categoriaNormalizada;
    }

    public void setDiaria(double diaria) {
        if (!Double.isFinite(diaria) || diaria < 0) throw new IllegalArgumentException("A diária não pode ser negativa.");
        this.diaria = diaria;
    }

    /** Registra uma unidade do produto consumido. Repetições representam mais unidades. */
    public void adicionaConsumo(Produto produto) {
        if (produto == null) throw new IllegalArgumentException("Produto inválido.");
        produtos.add(produto);
    }

    /** Devolve uma cópia da lista para evitar alterações externas acidentais. */
    public ArrayList<Produto> listaConsumo() {
        return new ArrayList<>(produtos);
    }

    public double valorTotalConsumo() {
        double total = 0.0;
        for (Produto produto : produtos) total += produto.getPreco();
        return total;
    }

    public void limpaConsumo() {
        produtos.clear();
    }

    public String serializar() {
        StringJoiner codigos = new StringJoiner(",");
        for (Produto produto : produtos) codigos.add(Integer.toString(produto.getCodigo()));
        return numero + "\t" + categoria + "\t" + String.format(Locale.ROOT, "%.2f", diaria) + "\t" + codigos;
    }

    public static Quarto deserializar(String linha, Map<Integer, Produto> produtosPorCodigo) {
        String[] campos = linha.split("\\t", -1);
        if (campos.length < 3) throw new IllegalArgumentException("Linha de quarto incompleta.");
        Quarto quarto = new Quarto(Integer.parseInt(campos[0].trim()), campos[1].trim().charAt(0),
                Double.parseDouble(campos[2].trim().replace(',', '.')));
        if (campos.length >= 4 && !campos[3].trim().isEmpty()) {
            for (String codigoTexto : campos[3].split(",")) {
                int codigo = Integer.parseInt(codigoTexto.trim());
                Produto produto = produtosPorCodigo.get(codigo);
                if (produto != null) quarto.adicionaConsumo(produto);
            }
        }
        return quarto;
    }

    public static String nomeCategoria(char categoria) {
        switch (Character.toUpperCase(categoria)) {
            case 'S': return "Standard";
            case 'M': return "Master";
            case 'P': return "Premium";
            default: return "Desconhecida";
        }
    }

    @Override
    public String toString() {
        return String.format(Locale.forLanguageTag("pt-BR"), "Quarto %d | Categoria: %s (%c) | Diária: R$ %.2f",
                numero, nomeCategoria(categoria), categoria, diaria);
    }
}
