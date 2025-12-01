package br.ufsc.feesc.services;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FeriadoService {

    private static final Set<String> feriadosFixos = new HashSet<>();
    private static final List<PeriodoFerias> ferias = new ArrayList<>();

    static {
        // Feriados fixos nacionais no formato "dd-MM"
        feriadosFixos.add("01-01"); // Ano Novo
        feriadosFixos.add("07-09"); // Independência
        feriadosFixos.add("21-04"); // Tiradentes
        feriadosFixos.add("25-12"); // Natal
        feriadosFixos.add("01-05"); // Dia do Trabalho
        feriadosFixos.add("15-11"); // Proclamação da República
        feriadosFixos.add("20-11"); // Dia da Consciência Negra

        ferias.add(new PeriodoFerias(
                LocalDate.of(2025, 12, 22),
                LocalDate.of(2026, 1, 5)
        ));

        ferias.add(new PeriodoFerias(
                LocalDate.of(2026, 1, 26),
                LocalDate.of(2026, 2, 4)
        ));

        ferias.add(new PeriodoFerias(
                LocalDate.of(2026, 2, 23),
                LocalDate.of(2026, 2, 27)
        ));
    }

    // Classe interna representando cada período de férias
    private static class PeriodoFerias {
        LocalDate inicio;
        LocalDate fim;

        PeriodoFerias(LocalDate inicio, LocalDate fim) {
            this.inicio = inicio;
            this.fim = fim;
        }
    }

    public boolean isFeriado(String dataCompleta) {
        // Extrair o dia e o mês do formato "yyyy-MM-dd"
        String diaMes = dataCompleta.substring(8, 10) + "-" + dataCompleta.substring(5, 7);
        LocalDate data = LocalDate.parse(dataCompleta);
        return feriadosFixos.contains(diaMes) || isFeriadoMovel(dataCompleta) || isFerias(data);
    }

    private boolean isFerias(LocalDate data) {
        for (PeriodoFerias periodo : ferias) {
            if (!data.isBefore(periodo.inicio) && !data.isAfter(periodo.fim)) {
                return true;
            }
        }
        return false;
    }

    // Método para verificar feriados móveis
    private boolean isFeriadoMovel(String dataCompleta) {
        int ano = Integer.parseInt(dataCompleta.substring(0, 4));
        LocalDate pascoa = calcularDataPascoa(ano);

        // Calculando datas de feriados móveis
        LocalDate carnaval = pascoa.minusDays(47); // Carnaval: 47 dias antes da Páscoa
        LocalDate sextaFeiraSanta = pascoa.minusDays(2); // Sexta-feira Santa: 2 dias antes da Páscoa
        LocalDate corpusChristi = pascoa.plusDays(60); // Corpus Christ

        Set<String> feriadosMoveis = new HashSet<>();
        feriadosMoveis.add(formatarData(carnaval));
        feriadosMoveis.add(formatarData(sextaFeiraSanta));
        feriadosMoveis.add(formatarData(corpusChristi));


        // Verificar se é um feriado móvel baseado no ano e data completa
        String diaMes = dataCompleta.substring(8, 10) + "-" + dataCompleta.substring(5, 7);
        return feriadosMoveis.contains(diaMes);
    }

    // Método para calcular a data da Páscoa
    private LocalDate calcularDataPascoa(int ano) {
        int a = ano % 19;
        int b = ano / 100;
        int c = ano % 100;
        int d = b / 4;
        int e = b % 4;
        int f = (b + 8) / 25;
        int g = (b - f + 1) / 3;
        int h = (19 * a + b - d - g + 15) % 30;
        int i = c / 4;
        int k = c % 4;
        int l = (32 + 2 * e + 2 * i - h - k) % 7;
        int m = (a + 11 * h + 22 * l) / 451;
        int mes = (h + l - 7 * m + 114) / 31;
        int dia = ((h + l - 7 * m + 114) % 31) + 1;
        return LocalDate.of(ano, mes, dia);
    }

    // Método para formatar a data como "dd-MM"
    private String formatarData(LocalDate date) {
        return String.format("%02d-%02d", date.getDayOfMonth(), date.getMonthValue());
    }
}