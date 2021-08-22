package de.moritzf.pdf.reader;

import org.apache.pdfbox.pdmodel.PDDocument;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.io.IOException;

/**
  * A class to handle some prettyness around a single PDF page.
  */
public final class PageWrapper implements MouseMotionListener {
    private JPanel pageWrapper = new JPanel();
    private PDFPagePanel pagePanel;
    private PDFReader reader;

    private static final int SPACE_AROUND_DOCUMENT = 20;

    /**
     * Constructor.
     *
     * @param aReader The reader application that holds this page.
     * @throws IOException If there is an error creating the page drawing objects.
     */
    public PageWrapper(PDFReader aReader, PDDocument document) throws IOException {
        reader = aReader;
        pagePanel = new PDFPagePanel(document);
        pageWrapper.setLayout(null);
        pageWrapper.add(pagePanel);
        pagePanel.setLocation(SPACE_AROUND_DOCUMENT, SPACE_AROUND_DOCUMENT);
        pageWrapper.setBorder(javax.swing.border.LineBorder.createBlackLineBorder());
        pagePanel.addMouseMotionListener(this);
    }

    /**
     * This will display the PDF page in this component.
     */
    public void displayPage(int pageNumber) {
        pagePanel.setPage(pageNumber);
        pagePanel.setPreferredSize(pagePanel.getSize());
        Dimension d = pagePanel.getSize();
        d.width += (SPACE_AROUND_DOCUMENT * 2);
        d.height += (SPACE_AROUND_DOCUMENT * 2);

        pageWrapper.setPreferredSize(d);
        pageWrapper.validate();
    }

    /**
     * This will get the JPanel that can be displayed.
     *
     * @return The panel with the displayed PDF page.
     */
    public JPanel getPanel() {
        return pageWrapper;
    }

    /**
     * {@inheritDoc}
     */
    public void mouseDragged(MouseEvent e) {
        //do nothing when mouse moves.
    }

    /**
     * {@inheritDoc}
     */
    public void mouseMoved(MouseEvent e) {
        reader.getBottomStatusPanel().getStatusLabel().setText(e.getX() + "," + (pagePanel.getHeight() - e.getY()));
    }

}