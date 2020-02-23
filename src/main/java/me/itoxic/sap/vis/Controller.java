package me.itoxic.sap.vis;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.stage.DirectoryChooser;
import me.itoxic.sap.Main;
import me.itoxic.sap.Static;
import me.itoxic.sap.base.Instruction;
import me.itoxic.sap.base.MemoryValue;
import me.itoxic.sap.base.Operator;
import me.itoxic.sap.base.Output;

import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Iterator;
import java.util.LinkedList;

public class Controller {

    @FXML
    private CheckBox done, asm, result, custom;

    @FXML
    private Label resolvetitle;

    @FXML
    private Slider switchmode;

    @FXML
    private TextField functionfield, filename;

    @FXML
    private TextArea asmarea;

    @FXML
    private Button folder, generate;

    @FXML
    private TextField lda, add, sub, mul, mod, call, got, ret, out, hlt;

    @FXML
    private Label github;

    private LinkedList<TextField> codeFields = new LinkedList<>();
    private String path;

    @FXML
    void initialize() {

        editCodes();
        changeSwitch();

        codeEditListener(lda, add, sub, mul, mod, call, got, ret, out, hlt);

        switchmode.valueProperty().addListener((observable, oldValue, newValue) -> {
            changeSwitch();
        });

        custom.selectedProperty().addListener((observable, oldValue, newValue) -> {
            Static.turnCustom(newValue);
        });

        github.onMouseEnteredProperty().setValue(event -> {
            github.setTextFill(Color.RED);
        });

        github.onMouseExitedProperty().setValue(event -> {
            github.setTextFill(Color.BLACK);
        });

        github.setOnMouseClicked(event -> {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                try {
                    Desktop.getDesktop().browse(new URI("https://github.com/zlToxicNetherlz"));
                } catch (IOException | URISyntaxException e) {
                    e.printStackTrace();
                }
            }
        });

        done.selectedProperty().addListener((observable, oldValue, newValue) -> {

            editCodes();

            if(newValue.booleanValue() == true)
                for(TextField field : codeFields)
                    if(field.getText() == null || field.getText().isEmpty())
                        field.setPromptText("0");

        });

        functionfield.setOnKeyPressed(ke -> {
            if (ke.getCode().equals(KeyCode.ENTER)) {
                generate();
            }
        });

        folder.onMouseClickedProperty().setValue(event -> {

            DirectoryChooser directoryChooser = new DirectoryChooser();
            File selectedDirectory = directoryChooser.showDialog(Main.getStage());

            if(selectedDirectory != null)
                path = selectedDirectory.getAbsolutePath();

        });

        asm.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if(newValue == true) {
                result.setDisable(false);
            } else {
                result.selectedProperty().setValue(false);
                result.setDisable(true);
            }
        });

        filename.textProperty().addListener((observable, oldValue, newValue) -> {
            if(newValue.length() == 0)
                filename.setText(oldValue);
        });

        generate.onMouseClickedProperty().setValue(event -> {
            generate();
        });

        String css = Main.getResource("pgen.css").toExternalForm();
        asmarea.getStylesheets().add(css);
        asmarea.setWrapText(true);

        asmarea.scrollTopProperty().addListener((observable, oldValue, newValue) -> {

            asmarea.setScrollTop(0);
            ScrollBar scrollBarv = (ScrollBar) asmarea.lookup(".scroll-bar:vertical");
            scrollBarv.setDisable(true);

        });

    }

    void codeEditListener(TextField...fields) {
        for(TextField field : fields) {
            codeFields.add(field);
            field.setPromptText("0");
            field.textProperty().addListener((observable, oldValue, newValue) -> {

                String value;

                if(newValue.length() >= 2)
                    value = oldValue;
                else
                    value = newValue;

                Static.setCustomLiteral(Static.getByUI(field.getId().toUpperCase()), value);
                System.out.println(Static.getByUI(field.getId().toUpperCase()));

            });
        }
    }

    void generate() {

        if(!done.isSelected()) {

            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Modo Edición");
            alert.setContentText("Debe desactivar el modo edición de códigos.");

            alert.showAndWait();
            return;

        }

        if(path == null)
            path = System.getProperty("user.home") + "/Desktop";

        File output = null;
        File output_asm = null;

        if(folder != null) {

            if(!new File(path).exists()) {
                new File(path).mkdir();
            }

            output = new File(path + "\\" + filename.getText() + ".hex");
            output_asm = new File(path + "\\" + filename.getText() + "_asm.txt");

            if(!output.exists()) {
                try {
                    output.createNewFile();
                } catch (IOException e) {
                    System.exit(-1);
                }
            }

            if(!output_asm.exists() && asm.isSelected()) {
                try {
                    output_asm.createNewFile();
                } catch (IOException e) {
                    System.exit(-1);
                }
            }

        }

        String formula = "";
        Output resultOutput;

        try {

            if(switchmode.getValue() == 2) {
                formula = asmFromArea();
            } else {
                formula = functionfield.getText();
            }

            resultOutput = Main.process(formula);

        } catch(Exception e) {

            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Formula Invalida");
            alert.setContentText("La formula introducida no es válida.");

            alert.showAndWait();
            return;

        }

        try {

            FileWriter writer = new FileWriter(output.getAbsolutePath());
            writer.append(resultOutput.getFile());
            writer.flush();
            writer.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

        if(asm.isSelected()) {

            StringBuffer asm = new StringBuffer();
            String prediction = getASMFormula(resultOutput.getInstructions(), resultOutput.getMemoryValues());

            asm.append(prediction + System.lineSeparator());
            asm.append(System.lineSeparator());
            asm.append("[Programa]" + System.lineSeparator() + System.lineSeparator());

            for(Instruction instruction : resultOutput.getInstructions()) {

                asm.append(instruction.getLiteralInstruction().substring(0, 3));
                asm.append(" ");
                asm.append(instruction.getMemoryValue() == "0" ? "00" : "R" + instruction.getMemoryValue());
                asm.append(" ");
                asm.append("⟺");
                asm.append(" ");
                asm.append(instruction.getOutput());
                asm.append(System.lineSeparator());

            }

            asm.append(System.lineSeparator() + "[Memoria]" + System.lineSeparator() + System.lineSeparator());

            for(MemoryValue memoryValue : resultOutput.getMemoryValues()) {

                asm.append("R" + memoryValue.getPosition().getLiteral() + " = " + memoryValue.getOutput());
                asm.append(System.lineSeparator());

            }

            try {

                FileWriter writer = new FileWriter(output_asm.getAbsolutePath());
                writer.append(asm.toString());
                writer.flush();
                writer.close();

            } catch (IOException e) {
                e.printStackTrace();
            }

            if(result.selectedProperty().get()) {

                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Completado");
                alert.setHeaderText("Predicción");
                alert.setContentText(prediction);

                alert.showAndWait();
                return;

            }

        }

    }

    String asmFromArea() {

        ObservableList<CharSequence> charSequences = asmarea.getParagraphs();
        String formula = "";

        for(int i = 0; i < 16; i++) {

            if(charSequences.size() < i + 1)
                return formula;

            String ins = String.valueOf(charSequences.get(i));
            ins = ins.replaceAll(" ", "");

            if(i == 0) {
                formula += ins;
                continue;
            }

            z: for(Static s : Static.values()) {
                if (ins.startsWith(s.getAsm())) {
                    for(Operator operator : Operator.values())
                        if (operator.getInstruction() == s) {
                            formula += operator.getCharacter() + ins.substring(s.getAsm().length());
                            continue z;
                        }
                }
            }

        }

        return formula;

    }

    void editCodes() {

        boolean dis = done.isSelected();

        lda.setDisable(dis);
        add.setDisable(dis);
        sub.setDisable(dis);
        mul.setDisable(dis);
        mod.setDisable(dis);
        call.setDisable(dis);
        got.setDisable(dis);
        ret.setDisable(dis);
        out.setDisable(dis);
        hlt.setDisable(dis);
        return;

    }

    void changeSwitch() {

        if(switchmode.getValue() == 1) {

            functionfield.setDisable(false);
            asmarea.setDisable(true);
            resolvetitle.setText("Resolver (Modo Ecuación)");
            return;

        }

        functionfield.setDisable(true);
        asmarea.setDisable(false);
        resolvetitle.setText("Resolver (Modo Avanzado)");
        return;

    }

    String getASMFormula(LinkedList<Instruction> ins, LinkedList<MemoryValue> mem) {

        String formula = "";
        Integer result = 0;

        boolean failed = false;

        Iterator<Instruction> insIterator = ins.iterator();

        z: for(int i = 1; i <= 16; i++) {

            if(insIterator.hasNext()) {

                Instruction last = insIterator.next();
                Static instruction = Static.getByLiteral(last.getInstructionValue());

                if(last.getMemoryValue() == "0")
                    continue;

                MemoryValue.MemoryPosition position = MemoryValue.MemoryPosition.getByPosition(MemoryValue.MemoryPosition.valueOf("_" + last.getMemoryValue()).getPos());

                int base = position.getPos() - ins.size();
                MemoryValue reference = mem.get(base);

                switch(instruction) {

                    case LDA:

                        formula = reference.getValue();
                        result = Integer.parseInt(formula, 16);
                        break;
                    case ADD:
                        formula = "(" + formula + "+" + reference.getValue() + ")";
                        result += Integer.parseInt(reference.getValue(), 8);
                        continue;
                    case SUB:
                        formula = "(" + formula + "-" + reference.getValue() + ")";
                        result -= Integer.parseInt(reference.getValue(), 8);
                        continue;
                    case MUL:
                        formula = "(" + formula + "*" + reference.getValue() + ")";
                        result *= Integer.parseInt(reference.getValue(), 8);
                        continue;
                    case MOD:
                        formula = "(" + formula + "%" + reference.getValue() + ")";
                        result %= Integer.parseInt(reference.getValue(), 8);
                        continue;
                    case CALL:
                        formula = formula + "CALL(" + reference.getValue() + ")";
                        failed = true;
                        continue;
                    case GOTO:
                        formula = formula + "GOTO(" + reference.getValue() + ")";
                        failed = true;
                        continue;
                    case RETUR:
                        break;
                    case OUT:
                        break;
                    case HLT:
                        break;
                }

                continue;

            }

        }

        if(failed == true)
            formula = formula + " = " + " ?";
        else
            formula = formula + " = " + Integer.toHexString(result);

        return formula;

    }

}
