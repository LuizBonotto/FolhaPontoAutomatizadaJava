package br.ufsc.feesc.utils;

import java.util.Random;

public class HorarioGenerator {
    private static Random random = new Random();

    // Configurações de tempo
    private static final int ENTRADA_MANHA_MIN = 7 * 60 + 50; // 7:45 em minutos
    private static final int ENTRADA_MANHA_MAX = 8 * 60 + 5; // 8:05 em minutos
    private static final int SAIDA_MANHA_MIN = 11 * 60 + 50; // 11:50 em minutos
    private static final int SAIDA_MANHA_MAX = 12 * 60 + 10; // 12:10 em minutos
    private static final int ENTRADA_TARDE_MIN = 13 * 60 + 45; // 13:45 em minutos
    private static final int ENTRADA_TARDE_MAX = 14 * 60 + 5; // 14:05 em minutos
    private static final int SAIDA_TARDE_MIN = 17 * 60 + 40; // 17:40 em minutos
    private static final int SAIDA_TARDE_MAX = 18 * 60 + 20; // 18:20 em minutos

    public String[] gerarHorarios() {
        // Gerar entrada manhã dentro do intervalo
        int entradaManha = gerarHorarioAleatorio(ENTRADA_MANHA_MIN, ENTRADA_MANHA_MAX);

        // Gerar saída manhã dentro do intervalo
        int saidaManha = gerarHorarioAleatorio(SAIDA_MANHA_MIN, SAIDA_MANHA_MAX);

        // Calcular duração da manhã
        int duracaoManha = saidaManha - entradaManha;

        // Calcular almoço entre 110 e 120 minutos
        int intervaloAlmoco = random.nextInt(11) + 110; // entre 110 e 120 minutos

        // Gerar entrada tarde
        int entradaTarde = saidaManha + intervaloAlmoco;

        // Garantir que a entrada tarde entre no intervalo definido
        if (entradaTarde < ENTRADA_TARDE_MIN) {
            entradaTarde = ENTRADA_TARDE_MIN;
        } else if (entradaTarde > ENTRADA_TARDE_MAX) {
            entradaTarde = ENTRADA_TARDE_MAX;
        }

        // Calcular duração total do trabalho
        int duracaoTarde = (8 * 60) - duracaoManha; // Total diário menos o período da manhã

        // Calcular saída tarde
        int saidaTarde = entradaTarde + duracaoTarde;

        // Ajustar saidaTarde para o máximo permitido
        if (saidaTarde > SAIDA_TARDE_MAX) {
            saidaTarde = SAIDA_TARDE_MAX;
        }

        // Converter os horários para formato "hh:mm"
        return new String[]{
                formatarHorario(entradaManha),
                formatarHorario(saidaManha),
                formatarHorario(entradaTarde),
                formatarHorario(saidaTarde)
        };
    }

    private int gerarHorarioAleatorio(int min, int max) {
        return random.nextInt(max - min + 1) + min;
    }

    private String formatarHorario(int minutosTotais) {
        int hora = minutosTotais / 60;
        int minuto = minutosTotais % 60;
        return String.format("%02d:%02d", hora, minuto);
    }
}