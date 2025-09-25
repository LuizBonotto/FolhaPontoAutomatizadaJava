package br.ufsc.feesc.ui;

import br.ufsc.feesc.pdf.DateInfo;
import br.ufsc.feesc.pdf.PdfExtractor;
import br.ufsc.feesc.pdf.PdfHandler;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
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
import java.util.Optional; // <<< MUDANÇA: Import necessário
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.layout.VBox;

import javafx.scene.control.Alert.AlertType;

import javafx.scene.text.Text;
import javafx.util.Pair; // <<< MUDANÇA: Import necessário

public class PdfApp extends Application {

    private File selectedFile;
    private int offset = 0;
    private Image pdfImage;
    private ImageView pdfImageView;
    private ComboBox<String> rubricaComboBox;
    private Text tutorialText;
    private DateInfo currentDateInfo; // <<< MUDANÇA: Para guardar a data globalmente

    @Override
    public void start(Stage primaryStage) {
        System.out.println("Aplicação iniciada");

        primaryStage.setTitle("Folha de Ponto Editor");

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

        rubricaComboBox = new ComboBox<>();
        rubricaComboBox.setPromptText("Selecione a rubrica");

        File rubricaDir = new File("src/main/resources/rubrica/");
        if (rubricaDir.exists() && rubricaDir.isDirectory()) {
            File[] rubricaFiles = rubricaDir.listFiles((dir, name) -> name.toLowerCase().endsWith(".png"));
            if (rubricaFiles != null) {
                for (File file : rubricaFiles) {
                    rubricaComboBox.getItems().add(file.getName());
                }
            }
        }
        if (!rubricaComboBox.getItems().isEmpty()) {
            rubricaComboBox.setValue(rubricaComboBox.getItems().get(0));
        }

        Rectangle overlayRect = new Rectangle(170, 55, 120, 20);
        overlayRect.setFill(Color.TRANSPARENT);
        overlayRect.setStroke(Color.RED);

        pdfImageView = new ImageView();

        tutorialText = new Text( "<-- 1 clique aqui para carregar a folha ponto\n\n\n" +
                "<-- 2 ajuste o quadrado em cima da cédula\n\n\n\n" +
                "<-- 3 Selecione a rubrica\n\n" +
                "<-- 4 aperte OK para Editar");
        tutorialText.setLayoutX(150);
        tutorialText.setLayoutY(17);
        tutorialText.setVisible(true);

        btnSelectPdf.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
            selectedFile = fileChooser.showOpenDialog(primaryStage);

            if (selectedFile != null) {
                try {
                    PdfExtractor extractor = new PdfExtractor();
                    currentDateInfo = extractor.extractMonthAndYear(selectedFile); // Extrai mês e ano

                    // <<< MUDANÇA: Lógica para pedir dados ao usuário
                    if (currentDateInfo.getMonth() == -1 || currentDateInfo.getYear() == -1) {
                        System.out.println("Não foi possível extrair mês e ano. Pedindo ao usuário...");

                        // Chama o dialog para pegar o mês e ano manualmente
                        Optional<DateInfo> manualDateInfo = askForMonthAndYear();

                        if (manualDateInfo.isPresent()) {
                            currentDateInfo = manualDateInfo.get(); // Atualiza com os dados do usuário
                        } else {
                            // Se o usuário cancelar, a operação é abortada
                            Alert alert = new Alert(AlertType.WARNING, "Operação cancelada. Mês e ano são necessários para continuar.");
                            alert.showAndWait();
                            selectedFile = null;
                            return; // Sai da ação do botão
                        }
                    }

                    System.out.println("Usando - Mes: " + currentDateInfo.getMonth() + " Ano: " + currentDateInfo.getYear());

                    BufferedImage bufferedImage = renderPdfToImage(selectedFile);
                    pdfImage = SwingFXUtils.toFXImage(bufferedImage, null);
                    pdfImageView.setImage(pdfImage);

                    Rectangle2D viewport = new Rectangle2D(0,180,350,150);
                    pdfImageView.setViewport(viewport);
                    pdfImageView.setScaleX(2);
                    pdfImageView.setScaleY(2);
                    pdfImageView.setLayoutX(50);

                    tutorialText.setVisible(false);

                } catch (Exception ex) {
                    ex.printStackTrace();
                    selectedFile = null;
                    tutorialText.setVisible(true);
                }
            } else {
                System.out.println("Por favor, selecione um arquivo PDF.");
                tutorialText.setVisible(true);
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
            if (selectedFile != null && currentDateInfo != null) { // <<< MUDANÇA: Verifica se currentDateInfo não é nulo

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

                try {
                    PdfHandler pdfHandler = new PdfHandler();
                    // <<< MUDANÇA: Usa a variável currentDateInfo que já temos
                    pdfHandler.preencherPonto(selectedFile, currentDateInfo.getYear(), currentDateInfo.getMonth(), offset/2, rubricaPath);
                    System.out.println("Arquivo processado com sucesso!");

                    Alert sucesso = new Alert(Alert.AlertType.INFORMATION);
                    sucesso.setTitle("Sucesso");
                    sucesso.setHeaderText(null);
                    sucesso.setContentText("Folha de ponto preenchida com sucesso!");
                    sucesso.showAndWait();

                    selectedFile = null;
                    pdfImageView.setImage(null);
                    tutorialText.setVisible(true);
                    currentDateInfo = null; // <<< MUDANÇA: Reseta a data

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
        pane.getChildren().add(tutorialText);
        Scene scene = new Scene(pane, 400, 200);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    // <<< MUDANÇA: NOVO MÉTODO PARA CRIAR A CAIXA DE DIÁLOGO
    private Optional<DateInfo> askForMonthAndYear() {
        // Cria uma nova caixa de diálogo
        Dialog<DateInfo> dialog = new Dialog<>();
        dialog.setTitle("Entrada Manual");
        dialog.setHeaderText("Não foi possível detectar o mês e o ano.\nPor favor, insira manualmente.");

        // Adiciona os botões de OK e Cancelar
        ButtonType okButtonType = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(okButtonType, ButtonType.CANCEL);

        // Cria o layout para os campos de texto
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField monthField = new TextField();
        monthField.setPromptText("Mês (1-12)");
        TextField yearField = new TextField();
        yearField.setPromptText("Ano (ex: 2024)");

        grid.add(new Label("Mês:"), 0, 0);
        grid.add(monthField, 1, 0);
        grid.add(new Label("Ano:"), 0, 1);
        grid.add(yearField, 1, 1);

        dialog.getDialogPane().setContent(grid);

        // Converte o resultado do diálogo para um objeto DateInfo quando o botão OK for clicado
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == okButtonType) {
                try {
                    int month = Integer.parseInt(monthField.getText());
                    int year = Integer.parseInt(yearField.getText());
                    if (month >= 1 && month <= 12 && year > 1900) {
                        return new DateInfo(month, year);
                    }
                } catch (NumberFormatException e) {
                    // Ignora se não for número, vai retornar null e o alerta será exibido
                }
            }
            return null; // Retorna nulo se o usuário cancelar ou inserir dados inválidos
        });

        return dialog.showAndWait();
    }

    private BufferedImage renderPdfToImage(File pdfFile) throws Exception {
        try (PDDocument document = Loader.loadPDF(pdfFile)) {
            PDFRenderer pdfRenderer = new PDFRenderer(document);
            return pdfRenderer.renderImage(0);
        }
    }

    public static void main(String[] args) {
        System.setOut(System.out);
        System.out.println("Iniciando aplicação...");
        launch(args);
    }
}