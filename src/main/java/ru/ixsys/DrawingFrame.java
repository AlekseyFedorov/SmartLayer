package ru.ixsys;

import com.sun.jna.Native;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.win32.W32APIOptions;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Line2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

class DrawingFrame extends JWindow {
    private final List<ColoredLine> lines = new ArrayList<>();
    private Point lastPoint = null;

    private Color brushColor = Color.RED;
    private float brushSize = 3f;

    private boolean clickThrough = false; // состояние

    interface User32 extends com.sun.jna.platform.win32.User32 {
        User32 INSTANCE = Native.load("user32", User32.class, W32APIOptions.DEFAULT_OPTIONS);
        int GWL_EXSTYLE = -20;
        int WS_EX_TRANSPARENT = 0x20;
        int WS_EX_LAYERED = 0x80000;
    }

    public DrawingFrame() {
        setAlwaysOnTop(true);
        setBackground(new Color(0, 0, 0, 1)); // минимальная альфа, чтобы ловить мышь

        DrawingPanel panel = new DrawingPanel();
        setContentPane(panel);

        Rectangle screen = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
        setBounds(screen);

        panel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "exit");
        panel.getActionMap().put("exit", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });

        setVisible(true);
    }

    public void setBrushColor(Color color) { this.brushColor = color; }

    public void setBrushSize(float size) { this.brushSize = size; }

    public void clear() {
        lines.clear();
        repaint();
    }

    public boolean isClickThrough() { return clickThrough; }

    /** Включает/выключает click-through режим **/
    public void toggleClickThrough() {
        WinDef.HWND hwnd = new WinDef.HWND();
        hwnd.setPointer(Native.getComponentPointer(this));
        int exStyle = User32.INSTANCE.GetWindowLong(hwnd, User32.GWL_EXSTYLE);

        if (!clickThrough) {
            User32.INSTANCE.SetWindowLong(hwnd, User32.GWL_EXSTYLE,
                    exStyle | User32.WS_EX_TRANSPARENT | User32.WS_EX_LAYERED);
            System.out.println("✅ Click-through ВКЛЮЧЕН");
        } else {
            User32.INSTANCE.SetWindowLong(hwnd, User32.GWL_EXSTYLE,
                    exStyle & ~User32.WS_EX_TRANSPARENT);
            System.out.println("🎨 Click-through ВЫКЛЮЧЕН");
        }
        clickThrough = !clickThrough;
    }

    /** Сохраняем текущее изображение в PNG **/
    public void saveToPNG(File file) throws IOException {
        Rectangle bounds = getBounds();
        BufferedImage img = new BufferedImage(bounds.width, bounds.height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();
        paint(g2); // рисуем текущее содержимое окна
        g2.dispose();
        ImageIO.write(img, "png", file);
    }

    // --- Панель для рисования ---
    private class DrawingPanel extends JPanel {
        public DrawingPanel() {
            setOpaque(false);

            addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    if (clickThrough) return;
                    lastPoint = e.getPoint();
                }
                @Override
                public void mouseReleased(MouseEvent e) {
                    lastPoint = null;
                }
            });

            addMouseMotionListener(new MouseMotionAdapter() {
                @Override
                public void mouseDragged(MouseEvent e) {
                    if (clickThrough) return;
                    if (lastPoint != null) {
                        lines.add(new ColoredLine(lastPoint, e.getPoint(), brushColor, brushSize));
                        lastPoint = e.getPoint();
                        repaint();
                    }
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            for (ColoredLine line : lines) {
                g2.setColor(line.color);
                g2.setStroke(new BasicStroke(line.size, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.draw(line.line);
            }
            g2.dispose();
        }
    }

    private static class ColoredLine {
        final Line2D line;
        final Color color;
        final float size;
        ColoredLine(Point p1, Point p2, Color color, float size) {
            this.line = new Line2D.Float(p1, p2);
            this.color = color;
            this.size = size;
        }
    }
}