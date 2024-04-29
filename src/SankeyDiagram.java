import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.CubicCurve;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.scene.transform.Scale;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class SankeyDiagram extends Application {
    private double length = 0;
    private final Map<String, Double> sources = new HashMap<>();
    private String title;
    private String source;
    private double total;
    private int categoryCount;
    private final List<String> categories = new ArrayList<>();
    private final File file = new File("data/");
    private final ComboBox<String> comboBox = new ComboBox<>();
    private final Pane diagramPane = new Pane();
    private Stage primaryStage;

    // Read data from file
    private void readData(String selectedFile) {
        // Clear data
        sources.clear();
        categories.clear();

        // Reset data
        total = 0;
        categoryCount = 0;

        // Read data from file
        File file = new File("data/" + selectedFile);
        try {
            Scanner input = new Scanner(file);
            this.title = input.nextLine();
            source = input.nextLine();
            // Read data line by line
            while (input.hasNextLine()) {
                String line = input.nextLine();
                int lastSpaceIndex = line.lastIndexOf(' ');
                String category = line.substring(0, lastSpaceIndex);
                double value = Double.parseDouble(line.substring(lastSpaceIndex + 1));
                categories.add(category);
                sources.put(category, value);
                this.total += value;
                categoryCount++;
            }
            sources.put(source, total);
            input.close();
        } catch (RuntimeException e) {
            showErrorDialog("Error Reading File",
                    "An error occurred while reading the file:\n" + e.getMessage());
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }
    }

    private void showErrorDialog(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    // Show Sankey diagram
    private void showSankeyDiagram() {
        // Reset data
        length = 0;
        diagramPane.getChildren().clear();
        // Create diagram
        int gap = 50;
        int index = 0;
        Rectangle node0 = create(250, 150, 350, Color.LIGHTBLUE);
        Text text0 = new Text(source + ": " + sources.get(source));
        text0.setFont(Font.font("Thoma", 20));
        text0.setX(node0.getX() - source.length() * 10 - 90);
        text0.setY(node0.getY() + node0.getHeight() / 2);

        // Creating a connection
        CubicCurve connection;
        Rectangle[] nodes = new Rectangle[categoryCount];
        for (Rectangle node : nodes) {
            Color color = Color.hsb(Math.random() * 360, 0.45, 0.8);
            Text text = new Text();
            double value = sources.get(categories.get(index)) / total;
            node = create(700, gap, value * node0.getHeight(), color);
            connection = create(node0, node, value,
                    Color.color(color.getRed(), color.getGreen(), color.getBlue(), 0.5));
            text.setText(categories.get(index) + ": " + sources.get(categories.get(index)));
            text.setFont(Font.font("Thoma", 20));
            text.setX(node.getX() + node.getWidth() + 5);
            text.setY(node.getY() + node.getHeight());
            diagramPane.getChildren().addAll(node, connection, text);
            gap += (int) (600 * value);
            index++;
        }
        diagramPane.getChildren().addAll(node0, text0);
    }

    // Create main UI
    private AnchorPane mainUI() {
        // Sets the pane to generate the Sankey diagram
        AnchorPane pane = new AnchorPane();
        AnchorPane.setTopAnchor(diagramPane, 50D);

        // Create a combo box
        File[] files = file.listFiles();
        if (files != null) {
            for (File value : files) {
                this.comboBox.getItems().add(value.getName());
            }
        }
        comboBox.setPrefWidth(150D);
        comboBox.setPrefHeight(23D);
        AnchorPane.setLeftAnchor(comboBox, 30D);
        AnchorPane.setTopAnchor(comboBox, 30D);
        pane.getChildren().add(comboBox);

        // Create a button
        Button button = new Button("View");
        button.setFont(new Font("Arial", 14));
        button.setPrefWidth(100D);
        button.setPrefHeight(23D);
        AnchorPane.setLeftAnchor(button, 200D);
        AnchorPane.setTopAnchor(button, 30D);
        pane.getChildren().add(button);

        // Set the action of the button
        button.setOnAction(event -> {
            String selectedFile = comboBox.getSelectionModel().getSelectedItem();
            if (selectedFile != null) {
                readData(selectedFile);
                showSankeyDiagram();
                primaryStage.setTitle(title);
            } else {
                showErrorDialog("Error", "Please select a file");
            }
        });

        // Add diagram pane to main pane
        pane.getChildren().add(diagramPane);
        return pane;
    }

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        Scene scene = new Scene(mainUI(), 1000, 700);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Select a file and click View");

        // Add scaling to the diagram pane
        Scale scale = new Scale();
        diagramPane.getTransforms().add(scale);

        // Bind the scaling to the scene width and height
        scene.widthProperty().addListener((obs, oldVal, newVal) -> scale.setX(newVal.doubleValue() / 1000));
        scene.heightProperty().addListener((obs, oldVal, newVal) -> scale.setY(newVal.doubleValue() / 700));

        primaryStage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }

    // Create a rectangle
    private Rectangle create(double x, double y, double height, Color color) {
        Rectangle rectangle = new Rectangle(x, y, 10, height);
        rectangle.setFill(color);
        return rectangle;
    }

    // Create a connection
    private CubicCurve create(Rectangle source, Rectangle target, double value, Color color) {
        // Creating a cubic curve
        double startX = source.getX() + source.getWidth() + value * 175;
        double startY = source.getY() + value * source.getHeight() / 2 + length;
        double endX = target.getX() - value * 175;
        double endY = target.getY() + target.getHeight() / 2;

        // Set the properties of the connection
        CubicCurve connection = new CubicCurve();
        connection.setStartX(startX);
        connection.setStartY(startY);
        connection.setControlX1((startX + endX) / 2);
        connection.setControlY1(startY);
        connection.setControlX2((startX + endX) / 2);
        connection.setControlY2(endY);
        connection.setEndX(endX);
        connection.setEndY(endY);

        // Set the color and width of the connection
        connection.setStroke(color);
        connection.setStrokeWidth(value * source.getHeight());
        connection.setFill(null);

        length += value * source.getHeight();
        return connection;
    }
}
