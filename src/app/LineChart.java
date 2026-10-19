package app;

import javax.imageio.ImageIO;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.RenderingHints;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class LineChart {
    private static final Color SURFACE = new Color(0xfcfcfb);
    private static final Color TEXT_PRIMARY = new Color(0x0b0b0b);
    private static final Color TEXT_SECONDARY = new Color(0x52514e);
    private static final Color GRID = new Color(0xe6e5e1);
    private static final Color AXIS = new Color(0xb9b8b2);
    private static final Color[] SERIES = {
            new Color(0x2a78d6), new Color(0xeb6834), new Color(0x1baf7a),
            new Color(0xeda100), new Color(0xe87ba4), new Color(0x008300),
            new Color(0x6250d6), new Color(0xe34948)
    };
    private static final int WIDTH = 1000;
    private static final int HEIGHT = 620;
    private static final int LEFT = 96;
    private static final int RIGHT = 40;
    private static final int TOP = 112;
    private static final int BOTTOM = 72;

    private final String title;
    private final String subtitle;
    private final String xLabel;
    private final String yLabel;
    private final boolean logX;
    private final boolean logY;
    private final List<String> names = new ArrayList<>();
    private final List<double[]> xs = new ArrayList<>();
    private final List<double[]> ys = new ArrayList<>();

    public LineChart(String title, String subtitle, String xLabel, String yLabel, boolean logX, boolean logY) {
        this.title = title;
        this.subtitle = subtitle;
        this.xLabel = xLabel;
        this.yLabel = yLabel;
        this.logX = logX;
        this.logY = logY;
    }

    public LineChart addSeries(String name, double[] x, double[] y) {
        names.add(name);
        xs.add(x);
        ys.add(y);
        return this;
    }

    public void save(File file) throws IOException {
        BufferedImage image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        g.setColor(SURFACE);
        g.fillRect(0, 0, WIDTH, HEIGHT);

        double minX = Double.MAX_VALUE;
        double maxX = -Double.MAX_VALUE;
        double minY = Double.MAX_VALUE;
        double maxY = -Double.MAX_VALUE;
        for (int s = 0; s < names.size(); s++) {
            for (int i = 0; i < xs.get(s).length; i++) {
                minX = Math.min(minX, xs.get(s)[i]);
                maxX = Math.max(maxX, xs.get(s)[i]);
                minY = Math.min(minY, ys.get(s)[i]);
                maxY = Math.max(maxY, ys.get(s)[i]);
            }
        }
        double[] xRange = axisRange(minX, maxX, logX);
        double[] yRange = axisRange(logY ? minY : 0, maxY, logY);

        int plotW = WIDTH - LEFT - RIGHT;
        int plotH = HEIGHT - TOP - BOTTOM;

        g.setFont(new Font("SansSerif", Font.BOLD, 20));
        g.setColor(TEXT_PRIMARY);
        g.drawString(title, LEFT - 60, 34);
        g.setFont(new Font("SansSerif", Font.PLAIN, 14));
        g.setColor(TEXT_SECONDARY);
        g.drawString(subtitle, LEFT - 60, 56);
        drawLegend(g, LEFT - 60, 86);

        g.setFont(new Font("SansSerif", Font.PLAIN, 13));
        FontMetrics fm = g.getFontMetrics();
        for (double tick : ticks(yRange, logY)) {
            int py = TOP + plotH - (int) Math.round(scale(tick, yRange, logY) * plotH);
            g.setColor(GRID);
            g.setStroke(new BasicStroke(1f));
            g.drawLine(LEFT, py, LEFT + plotW, py);
            String label = format(tick);
            g.setColor(TEXT_SECONDARY);
            g.drawString(label, LEFT - 10 - fm.stringWidth(label), py + 5);
        }
        for (double tick : ticks(xRange, logX)) {
            int px = LEFT + (int) Math.round(scale(tick, xRange, logX) * plotW);
            g.setColor(GRID);
            g.drawLine(px, TOP, px, TOP + plotH);
            String label = format(tick);
            g.setColor(TEXT_SECONDARY);
            g.drawString(label, px - fm.stringWidth(label) / 2, TOP + plotH + 22);
        }
        g.setColor(AXIS);
        g.drawLine(LEFT, TOP + plotH, LEFT + plotW, TOP + plotH);

        g.setColor(TEXT_SECONDARY);
        g.setFont(new Font("SansSerif", Font.PLAIN, 14));
        fm = g.getFontMetrics();
        g.drawString(xLabel, LEFT + plotW / 2 - fm.stringWidth(xLabel) / 2, HEIGHT - 22);
        Graphics2D rotated = (Graphics2D) g.create();
        rotated.rotate(-Math.PI / 2);
        rotated.drawString(yLabel, -(TOP + plotH / 2) - fm.stringWidth(yLabel) / 2, 24);
        rotated.dispose();

        for (int s = 0; s < names.size(); s++) {
            Color color = SERIES[s % SERIES.length];
            double[] x = xs.get(s);
            double[] y = ys.get(s);
            Path2D path = new Path2D.Double();
            for (int i = 0; i < x.length; i++) {
                double px = LEFT + scale(x[i], xRange, logX) * plotW;
                double py = TOP + plotH - scale(y[i], yRange, logY) * plotH;
                if (i == 0) {
                    path.moveTo(px, py);
                } else {
                    path.lineTo(px, py);
                }
            }
            g.setColor(color);
            g.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.draw(path);
            for (int i = 0; i < x.length; i++) {
                int px = (int) Math.round(LEFT + scale(x[i], xRange, logX) * plotW);
                int py = (int) Math.round(TOP + plotH - scale(y[i], yRange, logY) * plotH);
                drawMarker(g, s, px, py, color);
            }
        }
        g.dispose();
        file.getParentFile().mkdirs();
        ImageIO.write(image, "png", file);
    }

    private void drawLegend(Graphics2D g, int x, int y) {
        g.setFont(new Font("SansSerif", Font.PLAIN, 14));
        FontMetrics fm = g.getFontMetrics();
        int cursor = x;
        int row = y;
        for (int s = 0; s < names.size(); s++) {
            int itemWidth = 44 + fm.stringWidth(names.get(s)) + 22;
            if (cursor + itemWidth > WIDTH - RIGHT) {
                cursor = x;
                row += 22;
            }
            Color color = SERIES[s % SERIES.length];
            g.setColor(color);
            g.setStroke(new BasicStroke(2f));
            g.draw(new Line2D.Double(cursor, row - 5, cursor + 30, row - 5));
            drawMarker(g, s, cursor + 15, row - 5, color);
            g.setColor(TEXT_PRIMARY);
            g.drawString(names.get(s), cursor + 38, row);
            cursor += itemWidth;
        }
    }

    private void drawMarker(Graphics2D g, int series, int x, int y, Color color) {
        int r = 5;
        g.setColor(SURFACE);
        g.fillOval(x - r - 2, y - r - 2, 2 * r + 4, 2 * r + 4);
        g.setColor(color);
        switch (series % 4) {
            case 0:
                g.fillOval(x - r, y - r, 2 * r, 2 * r);
                break;
            case 1:
                g.fillRect(x - r + 1, y - r + 1, 2 * r - 2, 2 * r - 2);
                break;
            case 2:
                g.fillPolygon(new Polygon(new int[]{x, x + r + 1, x - r - 1}, new int[]{y - r - 1, y + r, y + r}, 3));
                break;
            default:
                g.fillPolygon(new Polygon(new int[]{x, x + r + 1, x, x - r - 1}, new int[]{y - r - 1, y, y + r + 1, y}, 4));
                break;
        }
    }

    private static double[] axisRange(double min, double max, boolean log) {
        if (log) {
            double low = Math.pow(10, Math.floor(Math.log10(min)));
            double high = Math.pow(10, Math.ceil(Math.log10(max)));
            if (low == high) {
                high = low * 10;
            }
            return new double[]{low, high};
        }
        double step = niceStep(max - min);
        double low = Math.floor(min / step) * step;
        double high = Math.ceil(max / step) * step;
        if (high == low) {
            high = low + step;
        }
        return new double[]{low, high};
    }

    private static double niceStep(double span) {
        if (span <= 0) {
            return 1;
        }
        double raw = span / 5;
        double magnitude = Math.pow(10, Math.floor(Math.log10(raw)));
        double residual = raw / magnitude;
        if (residual <= 1) {
            return magnitude;
        }
        if (residual <= 2) {
            return 2 * magnitude;
        }
        if (residual <= 5) {
            return 5 * magnitude;
        }
        return 10 * magnitude;
    }

    private static List<Double> ticks(double[] range, boolean log) {
        List<Double> result = new ArrayList<>();
        if (log) {
            for (double t = range[0]; t <= range[1] * 1.0001; t *= 10) {
                result.add(t);
            }
            return result;
        }
        double step = niceStep(range[1] - range[0]);
        for (double t = range[0]; t <= range[1] + step * 1e-9; t += step) {
            result.add(t);
        }
        return result;
    }

    private static double scale(double value, double[] range, boolean log) {
        if (log) {
            return (Math.log10(value) - Math.log10(range[0])) / (Math.log10(range[1]) - Math.log10(range[0]));
        }
        return (value - range[0]) / (range[1] - range[0]);
    }

    private static String format(double value) {
        double abs = Math.abs(value);
        if (abs == 0) {
            return "0";
        }
        if (abs >= 1_000_000) {
            return trim(value / 1_000_000) + "M";
        }
        if (abs >= 1_000) {
            return trim(value / 1_000) + "k";
        }
        if (abs >= 1) {
            return trim(value);
        }
        return new java.math.BigDecimal(value).round(new java.math.MathContext(3)).stripTrailingZeros().toPlainString();
    }

    private static String trim(double value) {
        if (value == Math.rint(value)) {
            return String.valueOf((long) value);
        }
        return String.format(java.util.Locale.US, "%.1f", value);
    }
}
