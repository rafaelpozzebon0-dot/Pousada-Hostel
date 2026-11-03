import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.Locale;

/** Representa uma data do calendário. */
public class Data implements Comparable<Data> {
    private int dia;
    private int mes;
    private int ano;

    public Data(int dia, int mes, int ano) {
        validar(dia, mes, ano);
        this.dia = dia;
        this.mes = mes;
        this.ano = ano;
    }

    private static void validar(int dia, int mes, int ano) {
        LocalDate.of(ano, mes, dia); // Lança exceção se a data não existir.
    }

    public int getDia() { return dia; }
    public int getMes() { return mes; }
    public int getAno() { return ano; }

    public void setDia(int dia) {
        validar(dia, mes, ano);
        this.dia = dia;
    }

    public void setMes(int mes) {
        validar(dia, mes, ano);
        this.mes = mes;
    }

    public void setAno(int ano) {
        validar(dia, mes, ano);
        this.ano = ano;
    }

    public LocalDate paraLocalDate() {
        return LocalDate.of(ano, mes, dia);
    }

    /** Retorna a quantidade de dias entre esta data e outra data posterior. */
    public long diasAte(Data outraData) {
        return ChronoUnit.DAYS.between(paraLocalDate(), outraData.paraLocalDate());
    }

    /** Formato usado nos arquivos de texto: d/M/y. */
    public String serializar() {
        return String.format(Locale.ROOT, "%02d/%02d/%04d", dia, mes, ano);
    }

    /** Aceita d/M/y, d-M-y ou y-M-d. */
    public static Data deserializar(String texto) {
        if (texto == null || texto.trim().isEmpty()) {
            throw new IllegalArgumentException("A data não pode ficar vazia.");
        }

        String valor = texto.trim();
        DateTimeFormatter[] formatos = {
            DateTimeFormatter.ofPattern("d/M/uuuu"),
            DateTimeFormatter.ofPattern("d-M-uuuu"),
            DateTimeFormatter.ISO_LOCAL_DATE
        };

        for (DateTimeFormatter formato : formatos) {
            try {
                LocalDate dataConvertida = LocalDate.parse(valor, formato);
                return new Data(dataConvertida.getDayOfMonth(), dataConvertida.getMonthValue(), dataConvertida.getYear());
            } catch (DateTimeParseException ignored) {
                // Tenta o próximo formato aceito.
            }
        }
        throw new IllegalArgumentException("Data inválida. Use dd/MM/aaaa.");
    }

    @Override
    public int compareTo(Data outraData) {
        return paraLocalDate().compareTo(outraData.paraLocalDate());
    }

    @Override
    public boolean equals(Object objeto) {
        if (this == objeto) return true;
        if (!(objeto instanceof Data)) return false;
        Data outra = (Data) objeto;
        return dia == outra.dia && mes == outra.mes && ano == outra.ano;
    }

    @Override
    public int hashCode() {
        return paraLocalDate().hashCode();
    }

    @Override
    public String toString() {
        return serializar();
    }
}
