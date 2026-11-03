import java.util.Locale;

/** Produto que pode ser consumido durante a hospedagem. */
public class Produto {
    private int codigo;
    private String nome;
    private double preco;

    public Produto(int codigo, String nome, double preco) {
        setCodigo(codigo);
        setNome(nome);
        setPreco(preco);
    }

    public int getCodigo() { return codigo; }
    public String getNome() { return nome; }
    public double getPreco() { return preco; }

    public void setCodigo(int codigo) {
        if (codigo <= 0) throw new IllegalArgumentException("O código do produto deve ser positivo.");
        this.codigo = codigo;
    }

    public void setNome(String nome) {
        if (nome == null || nome.trim().isEmpty()) throw new IllegalArgumentException("O nome do produto é obrigatório.");
        this.nome = limparCampo(nome);
    }

    public void setPreco(double preco) {
        if (!Double.isFinite(preco) || preco < 0) throw new IllegalArgumentException("O preço não pode ser negativo.");
        this.preco = preco;
    }

    private static String limparCampo(String valor) {
        return valor.trim().replace('\t', ' ').replace('\n', ' ').replace('\r', ' ');
    }

    public String serializar() {
        return codigo + "\t" + limparCampo(nome) + "\t" + String.format(Locale.ROOT, "%.2f", preco);
    }

    public static Produto deserializar(String linha) {
        String[] campos = linha.split("\\t", -1);
        if (campos.length < 3) throw new IllegalArgumentException("Linha de produto incompleta.");
        return new Produto(Integer.parseInt(campos[0].trim()), campos[1].trim(),
                Double.parseDouble(campos[2].trim().replace(',', '.')));
    }

    @Override
    public String toString() {
        return String.format(Locale.forLanguageTag("pt-BR"), "Código: %d | %s | R$ %.2f", codigo, nome, preco);
    }

    @Override
    public boolean equals(Object objeto) {
        if (this == objeto) return true;
        if (!(objeto instanceof Produto)) return false;
        return codigo == ((Produto) objeto).codigo;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(codigo);
    }
}
