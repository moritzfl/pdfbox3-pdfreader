package de.moritzf.pdf.reader;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;

import java.io.IOException;

import javax.swing.JPanel;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;

import org.apache.pdfbox.rendering.PDFRenderer;

/**
 * This is a simple JPanel that can be used to display a PDF page.
 */
public final class PDFPagePanel extends JPanel {

    private final PDDocument document;
    private int pageNumber;
    private PDFRenderer drawer;
    private Dimension pageDimension = null;
    private Dimension drawDimension = null;

    /**
     * Constructor.
     *
     * @throws IOException If there is an error creating the Page drawing objects.
     */
    public PDFPagePanel(PDDocument document) throws IOException {
        this.document = document;
        this.pageNumber = 0;
        this.drawer = new PDFRenderer(document);
    }

    /**
     * This will set the page that should be displayed in this panel.
     */
    public void setPage(int pageNumber) {
        this.pageNumber = pageNumber;
        PDPage page = document.getPage(pageNumber);
        drawDimension = new Dimension((int) page.getMediaBox().getWidth(), (int) page.getMediaBox().getHeight());
        int rotation = page.getRotation();
        if (rotation == 90 || rotation == 270) {
            pageDimension = new Dimension(drawDimension.height, drawDimension.width);
        } else {
            pageDimension = drawDimension;
        }
        setSize(drawDimension);
        setBackground(java.awt.Color.white);
    }

    /**
     * {@inheritDoc}
     */
    public void paint(Graphics g) {
        try {
            g.setColor(getBackground());
            g.fillRect(0, 0, getWidth(), getHeight());

            int rotation = document.getPage(this.pageNumber).getRotation();
            if (rotation == 90 || rotation == 270) {
                Graphics2D g2D = (Graphics2D) g;
                g2D.translate(pageDimension.getWidth(), 0.0f);
                g2D.rotate(Math.toRadians(rotation));
            }

            drawer.renderPageToGraphics(pageNumber, (Graphics2D) g);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}