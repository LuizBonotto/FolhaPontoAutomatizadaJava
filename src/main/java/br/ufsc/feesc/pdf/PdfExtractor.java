package br.ufsc.feesc.pdf;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.File;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PdfExtractor {

    public DateInfo extractMonthAndYear(File pdfFile) throws IOException {
        String text = extractText(pdfFile);
        //System.out.println("Texto extraído do PDF:\n" + text);

        // Inicialização padrão para -1 (indicando não encontrado/erro)
        int month = -1;
        int year = -1;

        // Expressão regular para encontrar "MÊS/ANO: <Mês>/<Ano>"
        //Pattern pattern = Pattern.compile("MÊS/ANO:\\s+(\\w+)/(\\d{4})");
        Pattern pattern = Pattern.compile("M[ÊE]S/ANO:\\s*([^/]+)/(\\d{4})");
        Matcher matcher = pattern.matcher(text);

        if (matcher.find()) {
            String monthStr = matcher.group(1);
            year = Integer.parseInt(matcher.group(2));

            // Converter o nome do mês para um número
            switch (monthStr.toLowerCase()) {
                case "janeiro": month = 1; break;
                case "fevereiro": month = 2; break;
                case "março": month = 3; break;
                case "abril": month = 4; break;
                case "maio": month = 5; break;
                case "junho": month = 6; break;
                case "julho": month = 7; break;
                case "agosto": month = 8; break;
                case "setembro": month = 9; break;
                case "outubro": month = 10; break;
                case "novembro": month = 11; break;
                case "dezembro": month = 12; break;
            }
        }

        return new DateInfo(month, year);
    }

    private String extractText(File pdfFile) throws IOException {
        try (PDDocument document = Loader.loadPDF(pdfFile)) {
            PDFTextStripper pdfStripper = new PDFTextStripper();
            return pdfStripper.getText(document);
        }
    }
}