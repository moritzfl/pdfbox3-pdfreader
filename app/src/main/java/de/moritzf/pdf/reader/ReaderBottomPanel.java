package de.moritzf.pdf.reader;

import javax.swing.*;
import java.awt.*;

/**
 * A panel to display at the bottom of the window for status and other stuff.
 */
public final class ReaderBottomPanel extends JPanel {

    private JLabel statusLabel = null;

    /**
     * This is the default constructor.
     */
    public ReaderBottomPanel() {
        super();
        initialize();
    }

    /**
     * This method initializes this.
     */
    private void initialize() {
        FlowLayout flowLayout1 = new FlowLayout();
        this.setLayout(flowLayout1);
        this.setComponentOrientation(java.awt.ComponentOrientation.LEFT_TO_RIGHT);
        this.setPreferredSize(new Dimension(1000, 20));
        flowLayout1.setAlignment(java.awt.FlowLayout.LEFT);
        this.add(getStatusLabel(), null);
    }

    /**
     * This method initializes status label.
     *
     * @return javax.swing.JLabel
     */
    public JLabel getStatusLabel() {
        if (statusLabel == null) {
            statusLabel = new JLabel();
            statusLabel.setText("Ready");
        }
        return statusLabel;
    }
}
