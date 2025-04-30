package br.ufsc.feesc.pdf;

import br.ufsc.feesc.services.FeriadoService;
import br.ufsc.feesc.utils.HorarioGenerator;
import java.util.Calendar;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

import java.io.File;
import java.io.IOException;

public class PdfHandler {

    //private static final String RUBRICA_PATH = "src/main/resources/rubrica/michel.png";

    private final FeriadoService feriadoService;
    private final HorarioGenerator horarioGenerator;

    public PdfHandler() {
        this.feriadoService = new FeriadoService();
        this.horarioGenerator = new HorarioGenerator();
    }

    public void preencherPonto(File pdfFile, int ano, int mes, int offset, String rubricaPath) throws IOException {
        // Carregar o documento PDF usando o arquivo selecionado
        PDDocument document = Loader.loadPDF(pdfFile);
        PDPage page = document.getPage(0);

        PDPageContentStream contentStream = new PDPageContentStream(document, page, PDPageContentStream.AppendMode.APPEND, true, true);
        contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 7);

        Calendar calendario = Calendar.getInstance();
        calendario.set(ano, mes - 2, 25);  // Iniciar em 25 do mês anterior

        int yEntradaManha  = 590 + offset;
        int horasTotaisMensais = 0;
        int horasEsperadasMensais = 0;
        int horasSemanais = 0;
        int semanaAtual = calendario.get(Calendar.WEEK_OF_YEAR);

        while (!(calendario.get(Calendar.MONTH) == mes - 1 && calendario.get(Calendar.DAY_OF_MONTH) == 25)) {
            String data = String.format("%04d-%02d-%02d",
                    calendario.get(Calendar.YEAR),
                    calendario.get(Calendar.MONTH) + 1,
                    calendario.get(Calendar.DAY_OF_MONTH));

            if (isDiaValido(calendario) && !feriadoService.isFeriado(data)) {
                String[] horarios = horarioGenerator.gerarHorarios();

                contentStream.beginText();
                contentStream.newLineAtOffset(165, yEntradaManha);

                contentStream.showText(horarios[0]);  // Entrada Manhã
                contentStream.newLineAtOffset(60, 0);
                contentStream.showText(horarios[1]);  // Saída Manhã
                contentStream.newLineAtOffset(125, 0);
                contentStream.showText(horarios[2]);  // Entrada Tarde
                contentStream.newLineAtOffset(60, 0);
                contentStream.showText(horarios[3]);  // Saída Tarde

                contentStream.endText();

                for (int i = 0; i < 2; i++) {
                    contentStream.drawImage(
                            PDImageXObject.createFromFile(rubricaPath, document),
                            165 + 110 + 190 * i,
                            yEntradaManha - 4,
                            40,
                            12
                    );
                }

                int horasDia = 8;
                horasTotaisMensais += horasDia;
                horasSemanais += horasDia;
            }

            if (calendario.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
                System.out.println("Semana " + semanaAtual + ": " + horasSemanais + " horas contabilizadas.");
                horasSemanais = 0;
                semanaAtual = calendario.get(Calendar.WEEK_OF_YEAR);
            }

            calendario.add(Calendar.DAY_OF_MONTH, 1);

            if (calendario.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY ||
                    calendario.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY ||
                    calendario.get(Calendar.DAY_OF_WEEK) == Calendar.FRIDAY ||
                    calendario.get(Calendar.DAY_OF_MONTH) == 10) {
                yEntradaManha -= 10;
            } else {
                yEntradaManha -= 11;
            }
        }

        if (horasSemanais > 0) {
            System.out.println("Semana " + semanaAtual + ": " + horasSemanais + " horas contabilizadas.");
        }

        System.out.println("Horas Esperadas: " + horasEsperadasMensais);
        System.out.println("Horas Contabilizadas: " + horasTotaisMensais);

        contentStream.close();

        // Define o nome do novo arquivo com sufixo '_preenchido'
        String originalPath = pdfFile.getAbsolutePath();
        String newFileName = originalPath.substring(0, originalPath.lastIndexOf('.')) + "_preenchido.pdf";

        // Salvar o documento com o novo nome
        document.save(new File(newFileName));
        document.close();

        System.out.println("PDF preenchido e salvo em: " + newFileName);
    }

    private boolean isDiaValido(Calendar calendario) {
        int diaSemana = calendario.get(Calendar.DAY_OF_WEEK);
        return diaSemana >= Calendar.MONDAY && diaSemana <= Calendar.FRIDAY;
    }

}