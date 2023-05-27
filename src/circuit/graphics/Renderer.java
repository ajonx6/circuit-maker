package circuit.graphics;

import circuit.Game;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.Line2D;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class Renderer {
    public static final Font COMPONENT_FONT = new Font("Arial", Font.BOLD, 16);
    public static final int WIRE_WIDTH = 3;

    public Graphics g;
    private Graphics2D g2d;
    private BufferStrategy bs;
    private static Renderer instance;

    public Renderer() {
    }

    public static Renderer getInstance() {
        if (instance == null) instance = new Renderer();
        return instance;
    }

    public void setGraphics(BufferStrategy bufferStrategy) {
        instance = new Renderer();
        bs = bufferStrategy;
        g = bs.getDrawGraphics();
        g2d = (Graphics2D) g.create();
    }

    public void init(int colour) {
        fillRect(0, 0, Game.WIDTH, Game.HEIGHT, colour);
    }

    public void drawCircuit(String name, int x, int y, int width, int height, int compColour) {
        drawCircuit(name, x, y, width, height, compColour, 0xffffff);
    }

    public void drawCircuit(String name, int x, int y, int width, int height, int compColour, int textColour) {
        g.setColor(new Color(compColour));
        g.fillRect(x, y, width, height);

        g2d.setFont(COMPONENT_FONT);
        FontMetrics fm = g2d.getFontMetrics();
        int tx =((width - fm.stringWidth(name)) / 2);
        int ty = ((height - fm.getHeight()) / 2) + fm.getAscent();
        g2d.setColor(new Color(textColour));
        g2d.drawString(name, tx + x, ty + y);
    }

    public void drawSSD(int x, int y, BufferedImage img) {
        g.drawImage(img, x, y, null);
    }

    public void drawPin(int x, int y, int radius, int colour) {
        g.setColor(new Color(colour));
        g.fillOval(x - radius, y - radius, 2 * radius, 2 * radius);
    }

    public void drawWire(int x1, int y1, int x2, int y2, int colour) {
        g2d.setColor(new Color(colour));
        g2d.setStroke(new BasicStroke(WIRE_WIDTH));
        g2d.draw(new Line2D.Double(x1, y1, x2, y2));
    }

    public void fillRect(int x, int y, int w, int h, int colour) {
        g.setColor(new Color(colour));
        g.fillRect(x, y, w, h);
    }

    public void finish() {
        g.dispose();
        g2d.dispose();
        bs.show();
    }
}