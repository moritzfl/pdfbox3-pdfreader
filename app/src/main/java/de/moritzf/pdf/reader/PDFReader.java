package de.moritzf.pdf.reader;

import java.awt.image.BufferedImage;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.printing.PDFPageable;
import org.apache.pdfbox.rendering.PDFRenderer;

/**
 * An application to read PDF documents. This will provide Acrobat Reader like funtionality.
 */
public final class PDFReader extends javax.swing.JFrame {
    private File currentDir = new File(".");
    private final JPanel documentPanel = new JPanel();
    private final ReaderBottomPanel bottomStatusPanel = new ReaderBottomPanel();
    private PDDocument document = null;
    private int currentPage = 0;
    private int numberOfPages = 0;
    private String currentFilename = null;
    private static final String PASSWORD = "-password";

    /**
     * Constructor.
     */
    public PDFReader() {
        initComponents();
    }

    /**
     * This method is called from within the consructor to initialize the form.
     */
    private void initComponents() {
        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu();
        JMenuItem openMenuItem = new JMenuItem();
        JMenuItem saveAsImageMenuItem = new JMenuItem();
        JMenuItem exitMenuItem = new JMenuItem();
        JMenuItem printMenuItem = new JMenuItem();
        JMenu viewMenu = new JMenu();
        JMenuItem nextPageItem = new JMenuItem();
        JMenuItem previousPageItem = new JMenuItem();
        setTitle("PDFBox - PDF Reader");
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                exitApplication();
            }
        });
        JScrollPane documentScroller = new JScrollPane();
        documentScroller.setViewportView(documentPanel);
        getContentPane().add(documentScroller, java.awt.BorderLayout.CENTER);
        getContentPane().add(bottomStatusPanel, java.awt.BorderLayout.SOUTH);
        fileMenu.setText("File");
        openMenuItem.setText("Open");
        openMenuItem.setToolTipText("Open PDF file");
        openMenuItem.addActionListener(this::openMenuItemActionPerformed);
        fileMenu.add(openMenuItem);
        printMenuItem.setText("Print");
        printMenuItem.addActionListener(evt -> {
            try {
                if (document != null) {
                    PrinterJob job = PrinterJob.getPrinterJob();
                    if (document.getDocumentInformation().getTitle() != null) {
                        job.setJobName(document.getDocumentInformation().getTitle());
                    }
                    job.setPageable(new PDFPageable(document));
                    if (job.printDialog()) {
                        job.print();
                    }
                }
            } catch (PrinterException e) {
                e.printStackTrace();
            }
        });
        fileMenu.add(printMenuItem);
        saveAsImageMenuItem.setText("Save as image");
        saveAsImageMenuItem.addActionListener(evt -> {
            if (document != null) {
                saveImage();
            }
        });
        fileMenu.add(saveAsImageMenuItem);
        exitMenuItem.setText("Exit");
        exitMenuItem.addActionListener(evt -> exitApplication());
        fileMenu.add(exitMenuItem);
        menuBar.add(fileMenu);
        viewMenu.setText("View");
        nextPageItem.setText("Next page");
        nextPageItem.setAccelerator(KeyStroke.getKeyStroke('+'));
        nextPageItem.addActionListener(evt -> nextPage());
        viewMenu.add(nextPageItem);
        previousPageItem.setText("Previous page");
        previousPageItem.setAccelerator(KeyStroke.getKeyStroke('-'));
        previousPageItem.addActionListener(evt -> previousPage());
        viewMenu.add(previousPageItem);
        menuBar.add(viewMenu);
        setJMenuBar(menuBar);
        java.awt.Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
        setBounds((screenSize.width - 700) / 2, (screenSize.height - 600) / 2, 700, 600);
    }

    private void updateTitle() {
        setTitle("PDFBox - " + currentFilename + " (" + (currentPage + 1) + "/" + numberOfPages + ")");
    }

    private void nextPage() {
        if (currentPage < numberOfPages - 1) {
            currentPage++;
            updateTitle();
            showPage(currentPage);
        }
    }

    private void previousPage() {
        if (currentPage > 0) {
            currentPage--;
            updateTitle();
            showPage(currentPage);
        }
    }

    private void openMenuItemActionPerformed(java.awt.event.ActionEvent evt) {
        JFileChooser chooser = new JFileChooser();
        chooser.setCurrentDirectory(currentDir);
        chooser.setFileFilter(new FileNameExtensionFilter("PDF Files", "PDF"));
        int result = chooser.showOpenDialog(PDFReader.this);
        if (result == JFileChooser.APPROVE_OPTION) {
            String name = chooser.getSelectedFile().getPath();
            currentDir = new File(name).getParentFile();
            try {
                openPDFFile(name, "");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void exitApplication() {
        try {
            if (document != null) {
                document.close();
            }
        } catch (IOException io) {
            // do nothing because we are closing the application
        }
        this.setVisible(false);
        this.dispose();
    }

    /**
     * @param args the command line arguments
     * @throws Exception If anything goes wrong.
     */
    public static void main(String[] args) throws Exception {
        PDFReader viewer = new PDFReader();
        String password = "";
        String filename = null;
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals(PASSWORD)) {
                i++;
                if (i >= args.length) {
                    usage();
                }
                password = args[i];
            }
            filename = args[i];
        }
        // open the pdf if present
        if (filename != null) {
            viewer.openPDFFile(filename, password);
        }
        viewer.setVisible(true);
    }

    private void openPDFFile(String filename, String password) throws Exception {
        if (document != null) {
            document.close();
            documentPanel.removeAll();
        }
        File file = new File(filename);
        parseDocument(file, password);

        List<PDPage> pages = StreamSupport.stream(document.getPages().spliterator(), false)
                .collect(Collectors.toList());
        numberOfPages = pages.size();
        currentFilename = file.getAbsolutePath();
        currentPage = 0;
        updateTitle();
        showPage(0);
    }

    private void showPage(int pageNumber) {
        try {
            PageWrapper wrapper = new PageWrapper(this, document);
            wrapper.displayPage(pageNumber);
            if (documentPanel.getComponentCount() > 0) {
                documentPanel.remove(0);
            }
            documentPanel.add(wrapper.getPanel());
            pack();
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    private void saveImage() {
        try {
            PDFRenderer renderer = new PDFRenderer(document);
            BufferedImage pageAsImage = renderer.renderImageWithDPI(currentPage, 300);
            String imageFilename = currentFilename;
            if (imageFilename.toLowerCase().endsWith(".pdf")) {
                imageFilename = imageFilename.substring(0, imageFilename.length() - 4);
            }
            imageFilename += "_" + (currentPage + 1);
            FileOutputStream out = new FileOutputStream(imageFilename);
            ImageIO.write(pageAsImage, "png", out);
            out.close();
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    /**
     * This will parse a document.
     *
     * @throws IOException If there is an error parsing the document.
     */
    private void parseDocument(File file, String password) throws IOException {
        document = Loader.loadPDF(file, password);
    }

    /**
     * Get the bottom status panel.
     *
     * @return The bottom status panel.
     */
    public ReaderBottomPanel getBottomStatusPanel() {
        return bottomStatusPanel;
    }

    /**
     * This will print out a message telling how to use this utility.
     */
    private static void usage() {
        System.err.println("""
                usage: java -jar pdfbox-app-x.y.z.jar PDFReader [OPTIONS] <input-file>
                  -password <password>      Password to decrypt the document
                  <input-file>              The PDF document to be loaded
                """);
    }
}