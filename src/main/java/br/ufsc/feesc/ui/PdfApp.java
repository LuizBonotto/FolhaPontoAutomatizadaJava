package br.ufsc.feesc.ui;

import br.ufsc.feesc.pdf.DateInfo;
import br.ufsc.feesc.pdf.PdfExtractor;
import br.ufsc.feesc.pdf.PdfHandler;

import javafx.application.Application;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.layout.VBox;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

import javafx.scene.text.Text;

public class PdfApp extends Application {

    private File selectedFile;
    private int offset = 0;
    private Image pdfImage;
    private ImageView pdfImageView;
    private ComboBox<String> rubricaComboBox; // Adicionado para selecionar rubrica
    private Text tutorialText; // Adicionado para o tutorial

    @Override
    public void start(Stage primaryStage) {
        System.out.println("Aplicação iniciada");

        primaryStage.setTitle("Folha de Ponto Editor");

        // Definir o ícone da janela
        try {
            Image icon = new Image(getClass().getResourceAsStream("/icones/icon.png"));
            primaryStage.getIcons().add(icon);
        } catch (Exception e) {
            System.out.println("Erro ao carregar o ícone: " + e.getMessage());
        }

        Button btnSelectPdf = new Button("Carregar PDF");
        Button btnMoveUp = new Button("Mover para Cima");
        Button btnMoveDown = new Button("Mover para Baixo");
        Button btnOK = new Button("OK");

        // Criar o ComboBox para selecionar a rubrica
        rubricaComboBox = new ComboBox<>();
        rubricaComboBox.setPromptText("Selecione a rubrica");

        // Listar arquivos da pasta resources/rubrica/
        File rubricaDir = new File("src/main/resources/rubrica/");
        if (rubricaDir.exists() && rubricaDir.isDirectory()) {
            File[] rubricaFiles = rubricaDir.listFiles((dir, name) -> name.toLowerCase().endsWith(".png"));
            if (rubricaFiles != null) {
                for (File file : rubricaFiles) {
                    rubricaComboBox.getItems().add(file.getName());
                }
            }
        }
        // Definir um valor padrão (opcional)
        if (!rubricaComboBox.getItems().isEmpty()) {
            rubricaComboBox.setValue(rubricaComboBox.getItems().get(0));
        }

        Rectangle overlayRect = new Rectangle(170, 55, 120, 20);
        overlayRect.setFill(Color.TRANSPARENT);
        overlayRect.setStroke(Color.RED);

        pdfImageView = new ImageView();

        // Criar o texto do tutorial
        tutorialText = new Text( "<-- 1 clique aqui para carregar a folha ponto\n\n\n" +
                                    "<-- 2 ajuste o quadrado em cima da cédula\n\n\n" +
                                    "<-- 3 Selecione a rubrica\n\n" +
                                    "<-- 4 aperte OK para Editar");
        tutorialText.setLayoutX(150); // Posição ao lado dos botões
        tutorialText.setLayoutY(50);
        tutorialText.setVisible(true); // Visível inicialmente (sem arquivo carregado)

        btnSelectPdf.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
            selectedFile = fileChooser.showOpenDialog(primaryStage);

            if (selectedFile != null) {
                try {
                    PdfExtractor extractor = new PdfExtractor();
                    DateInfo dateInfo = extractor.extractMonthAndYear(selectedFile); // Extrai mês e ano

                    if (dateInfo.getMonth() == -1 || dateInfo.getYear() == -1) {
                        System.out.println("Não foi possível extrair mês e ano.");
                        selectedFile = null; // Resetar se falhar
                        return;
                    }

                    System.out.println("Mes: " + dateInfo.getMonth() + " Ano: " + dateInfo.getYear());

                    BufferedImage bufferedImage = renderPdfToImage(selectedFile);
                    pdfImage = SwingFXUtils.toFXImage(bufferedImage, null);
                    pdfImageView.setImage(pdfImage);

                    Rectangle2D viewport = new Rectangle2D(0,180,350,150); // Ajuste conforme necessário
                    pdfImageView.setViewport(viewport);

                    // Defina a escala fixa desejada
                    pdfImageView.setScaleX(2);
                    pdfImageView.setScaleY(2);

                    // Desloca a imagem para a direita
                    pdfImageView.setLayoutX(50); //

                    tutorialText.setVisible(false);

                } catch (Exception ex) {
                    ex.printStackTrace();
                    selectedFile = null; // Resetar em caso de erro
                    tutorialText.setVisible(true); // Mostrar tutorial novamente
                }
            } else {
                System.out.println("Por favor, selecione um arquivo PDF.");
                tutorialText.setVisible(true); // Garantir que o tutorial fique visível
            }
        });

        btnMoveUp.setOnAction(e -> {
            offset++;
            overlayRect.setY(overlayRect.getY() - 1);
        });

        btnMoveDown.setOnAction(e -> {
            offset--;
            overlayRect.setY(overlayRect.getY() + 1);
        });

        btnOK.setOnAction(e -> {
            if (selectedFile != null) {

                String selectedRubrica = rubricaComboBox.getValue();
                if (selectedRubrica == null) {
                    Alert alerta = new Alert(Alert.AlertType.WARNING);
                    alerta.setTitle("Aviso");
                    alerta.setHeaderText(null);
                    alerta.setContentText("Por favor, selecione uma rubrica antes de continuar.");
                    alerta.showAndWait();
                    return;
                }

                String rubricaPath = "src/main/resources/rubrica/" + selectedRubrica;
                PdfExtractor extractor = new PdfExtractor();
                try {
                    DateInfo dateInfo = extractor.extractMonthAndYear(selectedFile); // Reusa extração

                    if (dateInfo.getMonth() == -1 || dateInfo.getYear() == -1) {
                        Alert erroExtracao = new Alert(Alert.AlertType.WARNING);
                        erroExtracao.setTitle("Erro na Extração");
                        erroExtracao.setHeaderText(null);
                        erroExtracao.setContentText("Não foi possível extrair mês e ano do PDF.");
                        erroExtracao.showAndWait();
                        return;
                    }

                    PdfHandler pdfHandler = new PdfHandler();
                    pdfHandler.preencherPonto(selectedFile, dateInfo.getYear(), dateInfo.getMonth(), offset/2, rubricaPath);
                    System.out.println("Arquivo processado com sucesso!");

                    Alert sucesso = new Alert(Alert.AlertType.INFORMATION);
                    sucesso.setTitle("Sucesso");
                    sucesso.setHeaderText(null);
                    sucesso.setContentText("Folha de ponto preenchida com sucesso!");
                    sucesso.showAndWait();

                    // Resetar o estado para a tela inicial
                    selectedFile = null;
                    pdfImageView.setImage(null); // Remover a imagem do PDF
                    tutorialText.setVisible(true); // Mostrar o tutorial novamente

                } catch (IOException ex) {
                    Alert erroIO = new Alert(Alert.AlertType.ERROR);
                    erroIO.setTitle("Erro");
                    erroIO.setHeaderText("Falha ao processar o PDF");
                    erroIO.setContentText("Erro: " + ex.getMessage());
                    erroIO.showAndWait();
                }
            } else {
                Alert alerta = new Alert(Alert.AlertType.WARNING);
                alerta.setTitle("Aviso");
                alerta.setHeaderText(null);
                alerta.setContentText("Por favor, selecione um arquivo PDF antes de continuar.");
                alerta.showAndWait();
            }
        });

        Pane pane = new Pane();
        pane.getChildren().add(pdfImageView);
        pane.getChildren().add(overlayRect);
        pane.getChildren().add(new VBox(10, btnSelectPdf, btnMoveUp, btnMoveDown, rubricaComboBox, btnOK));
        pane.getChildren().add(tutorialText); // Adicionar o tutorial ao pane
        Scene scene = new Scene(pane, 400, 200);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private BufferedImage renderPdfToImage(File pdfFile) throws Exception {
        try (PDDocument document = Loader.loadPDF(pdfFile)) {
            PDFRenderer pdfRenderer = new PDFRenderer(document);
            BufferedImage bufferedImage = pdfRenderer.renderImage(0);
            return bufferedImage;
        }
    }

    public static void main(String[] args) {
        System.setOut(System.out);
        System.out.println("Iniciando aplicação...");
        launch(args);
    }
}