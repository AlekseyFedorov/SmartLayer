package ru.ixsys;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;

class ControlPanel extends JFrame {
    private final DrawingFrame drawingFrame;
    private final JButton toggleButton;

    public ControlPanel(DrawingFrame frame) {
        super("Панель инструментов");
        this.drawingFrame = frame;

        setAlwaysOnTop(true);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new FlowLayout());
        setSize(520, 90);
        setLocation(100, 100);

        // 🎨 Цвет кисти
        JButton colorButton = new JButton("Цвет");
        colorButton.addActionListener(e -> {
            Color c = JColorChooser.showDialog(this, "Выберите цвет кисти", Color.RED);
            if (c != null) {
                drawingFrame.setBrushColor(c);
                colorButton.setBackground(c);
            }
        });

        // 📏 Толщина кисти
        JSpinner sizeSpinner = new JSpinner(new SpinnerNumberModel(3.0, 1.0, 30.0, 1.0));
        sizeSpinner.addChangeListener(e ->
                drawingFrame.setBrushSize(((Double) sizeSpinner.getValue()).floatValue()));

        // 🧹 Очистка
        JButton clearButton = new JButton("Очистить");
        clearButton.addActionListener(e -> drawingFrame.clear());

        // 👁 Click-through
        toggleButton = new JButton("Прозрачный: ВЫКЛ");
        toggleButton.addActionListener(e -> {
            drawingFrame.toggleClickThrough();
            toggleButton.setText("Прозрачный: " +
                    (drawingFrame.isClickThrough() ? "ВКЛ" : "ВЫКЛ"));
        });

        // 💾 Сохранить
        JButton saveButton = new JButton("Сохранить");
        saveButton.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setSelectedFile(new File("drawing.png"));
            if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                try {
                    drawingFrame.saveToPNG(chooser.getSelectedFile());
                    JOptionPane.showMessageDialog(this,
                            "Изображение сохранено:\n" + chooser.getSelectedFile().getAbsolutePath(),
                            "Сохранено", JOptionPane.INFORMATION_MESSAGE);
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(this,
                            "Ошибка сохранения: " + ex.getMessage(),
                            "Ошибка", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        // ❌ Выход
        JButton exitButton = new JButton("Выход");
        exitButton.addActionListener(e -> System.exit(0));

        add(colorButton);
        add(new JLabel("Толщина:"));
        add(sizeSpinner);
        add(clearButton);
        add(toggleButton);
        add(saveButton);
        add(exitButton);

        setVisible(true);
    }
}