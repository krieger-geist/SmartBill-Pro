import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.border.*;
import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import java.awt.*;
import java.awt.event.*;
import java.awt.print.*;
import java.io.*;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class BillingSystemFinal extends JFrame implements Printable {
    
    // --- UI Components ---
    private JTextField customerField, itemNameField, priceField, qtyField, discountField, taxField;
    private DefaultTableModel tableModel;
    private JLabel subtotalLabel, taxLabel, discountLabel, totalLabel, statusLabel;
    private JTable billTable;
    private JTextArea previewArea;
    
    // --- Data Formatting & State ---
    private DecimalFormat df = new DecimalFormat("#,##0.00"); // Formats numbers like 1,234.56
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private double subtotal = 0, taxRate = 0, discountRate = 0;

    /**
     * Main Constructor - Initializes the Application
     */
    public BillingSystemFinal() {
        initTheme();
        createUI();
        setupEventHandlers();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    /**
     * Sets the Look and Feel to ensure consistent UI across OS
     */
    private void initTheme() {
        try {
            // Use Nimbus for a modern look, fallback to System if unavailable
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception e) {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    /**
     * Builds the Main Window Layout
     */
    private void createUI() {
        setTitle("🏪 SmartBill Pro - Professional Invoicing");
        setSize(1100, 750);
        setMinimumSize(new Dimension(1000, 650));
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE); // We will handle close event
        setLayout(new BorderLayout(10, 10));

        // 1. Header Panel
        add(createHeaderPanel(), BorderLayout.NORTH);

        // 2. Main Content (Tabbed Pane)
        JTabbedPane mainTabs = new JTabbedPane();
        mainTabs.setFont(new Font("Segoe UI", Font.BOLD, 14));
        mainTabs.addTab("📝 Billing", createBillingPanel());
        mainTabs.addTab("📊 Report & Settings", createSettingsPanel());
        add(mainTabs, BorderLayout.CENTER);

        // 3. Footer Panel (Action Buttons)
        add(createFooterPanel(), BorderLayout.SOUTH);

        // 4. Status Bar
        add(createStatusBar(), BorderLayout.PAGE_END);
        
        // Handle window closing
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                int confirm = JOptionPane.showConfirmDialog(null, 
                    "Are you sure you want to exit SmartBill Pro?", 
                    "Exit Confirmation", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    System.exit(0);
                }
            }
        });
    }

    // --- PANEL CREATION METHODS ---

    private JPanel createHeaderPanel() {
        JPanel header = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                // Gradient background
                GradientPaint gp = new GradientPaint(0, 0, new Color(41, 128, 185), 0, getHeight(), new Color(44, 62, 80));
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, getWidth(), getHeight());
                g2d.dispose();
            }
        };
        header.setPreferredSize(new Dimension(0, 80));
        header.setBorder(new EmptyBorder(10, 20, 10, 20));

        // App Title
        JLabel title = new JLabel("🏪 SmartBill Pro v5.0");
        title.setForeground(Color.WHITE);
        title.setFont(new Font("Segoe UI", Font.BOLD, 32));
        title.setBorder(new EmptyBorder(0, 0, 0, 20));
        
        // Customer Input Area
        JPanel customerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        customerPanel.setOpaque(false);
        JLabel custLabel = new JLabel("👤 Customer Name:");
        custLabel.setForeground(Color.WHITE);
        custLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        customerField = new JTextField(25);
        customerField.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        
        customerPanel.add(custLabel);
        customerPanel.add(customerField);
        
        header.add(title, BorderLayout.WEST);
        header.add(customerPanel, BorderLayout.EAST);
        
        return header;
    }

    private JPanel createBillingPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        
        // --- Input Section ---
        JPanel inputPanel = new JPanel(new GridBagLayout());
        inputPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)), 
            "➕ New Entry"));
        inputPanel.setBackground(Color.WHITE);
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // Row 0: Item Name
        gbc.gridx = 0; gbc.gridy = 0;
        inputPanel.add(new JLabel("Item Name:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        itemNameField = new JTextField();
        inputPanel.add(itemNameField, gbc);
        
        // Row 1: Price
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0;
        inputPanel.add(new JLabel("Price (₹):"), gbc);
        gbc.gridx = 1;
        priceField = new JTextField();
        inputPanel.add(priceField, gbc);

        // Row 1: Quantity (split column)
        gbc.gridx = 2; gbc.weightx = 0;
        inputPanel.add(new JLabel("Qty:"), gbc);
        gbc.gridx = 3; gbc.weightx = 0.2;
        qtyField = new JTextField();
        inputPanel.add(qtyField, gbc);

        // Row 2: Buttons
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 4; gbc.fill = GridBagConstraints.CENTER;
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 5));
        btnRow.setOpaque(false);
        
        JButton addBtn = createStyledButton("➕ Add to Bill", new Color(39, 174, 96));
        JButton delBtn = createStyledButton("🗑️ Remove Last", new Color(192, 57, 43));
        JButton clearBtn = createStyledButton("🧹 Clear Inputs", new Color(127, 140, 141));
        
        addBtn.addActionListener(e -> addItem());
        delBtn.addActionListener(e -> deleteLastItem());
        clearBtn.addActionListener(e -> clearInputFields());
        
        btnRow.add(addBtn); btnRow.add(delBtn); btnRow.add(clearBtn);
        inputPanel.add(btnRow, gbc);
        
        panel.add(inputPanel, BorderLayout.NORTH);

        // --- Table Section ---
        tableModel = new DefaultTableModel(new String[]{"Item", "Price", "Qty", "Total"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; } // Make table read-only
        };
        billTable = new JTable(tableModel);
        billTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        billTable.setRowHeight(28);
        billTable.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        billTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        billTable.getTableHeader().setBackground(new Color(230, 230, 230));
        
        // Align numbers to the right
        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(JLabel.RIGHT);
        billTable.getColumnModel().getColumn(1).setCellRenderer(rightRenderer);
        billTable.getColumnModel().getColumn(2).setCellRenderer(rightRenderer);
        billTable.getColumnModel().getColumn(3).setCellRenderer(rightRenderer);

        JScrollPane scrollPane = new JScrollPane(billTable);
        scrollPane.setBorder(new LineBorder(new Color(200, 200, 200)));
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }

    private JPanel createSettingsPanel() {
        JPanel panel = new JPanel(new BorderLayout(20, 20));
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        // Adjustments (Tax/Discount)
        JPanel adjPanel = new JPanel(new GridLayout(2, 2, 20, 15));
        adjPanel.setBorder(BorderFactory.createTitledBorder("⚙️ Bill Adjustments"));
        
        adjPanel.add(new JLabel("Discount Percentage (%):"));
        discountField = new JTextField("0");
        adjPanel.add(discountField);
        
        adjPanel.add(new JLabel("Tax / GST Percentage (%):"));
        taxField = new JTextField("0");
        adjPanel.add(taxField);
        
        // Live Calculation Trigger
        JButton calcBtn = createStyledButton("🔄 Recalculate Totals", new Color(41, 128, 185));
        calcBtn.addActionListener(e -> updateCalculations());
        adjPanel.add(calcBtn); // Add to panel layout
        
        panel.add(adjPanel, BorderLayout.NORTH);

        // Preview Area
        JPanel previewContainer = new JPanel(new BorderLayout());
        previewContainer.setBorder(BorderFactory.createTitledBorder("👁️ Live Receipt Preview"));
        previewArea = new JTextArea();
        previewArea.setEditable(false);
        previewArea.setFont(new Font("Monospaced", Font.PLAIN, 13));
        previewArea.setBackground(new Color(250, 250, 250));
        previewContainer.add(new JScrollPane(previewArea), BorderLayout.CENTER);
        
        panel.add(previewContainer, BorderLayout.CENTER);
        
        return panel;
    }

    private JPanel createFooterPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        panel.setBorder(new MatteBorder(1, 0, 0, 0, Color.LIGHT_GRAY));
        panel.setBackground(new Color(245, 245, 245));

        // Labels for Totals
        JPanel totalsPanel = new JPanel(new GridLayout(1, 4, 20, 0));
        totalsPanel.setBackground(new Color(245, 245, 245));
        
        totalsPanel.add(createTotalLabel("Subtotal:", subtotalLabel));
        totalsPanel.add(createTotalLabel("Discount:", discountLabel));
        totalsPanel.add(createTotalLabel("Tax:", taxLabel));
        
        // Grand Total specific styling
        JPanel grandPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        grandPanel.setBackground(new Color(245, 245, 245));
        JLabel gLabel = new JLabel("GRAND TOTAL:");
        gLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        totalLabel = new JLabel("₹0.00");
        totalLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        totalLabel.setForeground(new Color(39, 174, 96));
        grandPanel.add(gLabel); grandPanel.add(totalLabel);
        
        // Buttons
        JButton pdfBtn = createStyledButton("📄 Export PDF", new Color(142, 68, 173));
        JButton printBtn = createStyledButton("🖨️ Print", new Color(52, 152, 219));
        JButton newBillBtn = createStyledButton("🆕 New Bill", new Color(230, 126, 34));
        JButton aboutBtn = createStyledButton("ℹ️ About", new Color(52, 73, 94));

        pdfBtn.addActionListener(e -> exportToPDF());
        printBtn.addActionListener(e -> printBill());
        newBillBtn.addActionListener(e -> resetSystem());
        aboutBtn.addActionListener(e -> showAboutDialog());

        panel.add(totalsPanel);
        panel.add(grandPanel);
        panel.add(new JSeparator(SwingConstants.VERTICAL));
        panel.add(pdfBtn);
        panel.add(printBtn);
        panel.add(newBillBtn);
        panel.add(aboutBtn);

        return panel;
    }
    
    private JPanel createTotalLabel(String title, JLabel valueLabel) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        p.setBackground(new Color(245, 245, 245));
        p.add(new JLabel(title));
        valueLabel = new JLabel("₹0.00");
        valueLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        p.add(valueLabel);
        if(title.equals("Subtotal:")) subtotalLabel = valueLabel;
        if(title.equals("Discount:")) discountLabel = valueLabel;
        if(title.equals("Tax:")) taxLabel = valueLabel;
        return p;
    }

    private JLabel createStatusBar() {
        statusLabel = new JLabel("✅ Ready", SwingConstants.CENTER);
        statusLabel.setBorder(new EtchedBorder(EtchedBorder.LOWERED));
        statusLabel.setPreferredSize(new Dimension(0, 25));
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        statusLabel.setBackground(Color.WHITE);
        statusLabel.setOpaque(true);
        return statusLabel;
    }

    // --- LOGIC & EVENT HANDLING ---

    private void setupEventHandlers() {
        setupKeyboardNav();
    }

    private void setupKeyboardNav() {
        itemNameField.addActionListener(e -> priceField.requestFocus());
        priceField.addActionListener(e -> qtyField.requestFocus());
        qtyField.addActionListener(e -> addItem());
    }

    private void addItem() {
        try {
            String name = itemNameField.getText().trim();
            if (name.isEmpty()) throw new Exception("Item Name cannot be empty.");
            
            double price = Double.parseDouble(priceField.getText().trim());
            int qty = Integer.parseInt(qtyField.getText().trim());
            
            if (price <= 0 || qty <= 0) throw new Exception("Price and Quantity must be positive numbers.");
            
            double total = price * qty;
            
            // Add to Table
            tableModel.addRow(new Object[]{
                name, 
                df.format(price), 
                qty, 
                df.format(total)
            });
            
            clearInputFields();
            updateCalculations();
            itemNameField.requestFocus();
            statusLabel.setText("✅ Added: " + name);
            
        } catch (NumberFormatException e) {
            showError("Please enter valid numbers for Price and Quantity.");
        } catch (Exception e) {
            showError(e.getMessage());
        }
    }

    private void deleteLastItem() {
        int rows = tableModel.getRowCount();
        if (rows > 0) {
            tableModel.removeRow(rows - 1);
            updateCalculations();
            statusLabel.setText("🗑️ Last item removed");
        } else {
            showWarning("Table is already empty.");
        }
    }
    
    private void clearInputFields() {
        itemNameField.setText("");
        priceField.setText("");
        qtyField.setText("");
    }

    private void updateCalculations() {
        // 1. Calculate Subtotal
        subtotal = 0;
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            // Remove commas from formatted string before parsing
            String val = tableModel.getValueAt(i, 3).toString().replace(",", "");
            subtotal += Double.parseDouble(val);
        }

        // 2. Parse Rates
        try {
            discountRate = Double.parseDouble(discountField.getText().trim());
        } catch (NumberFormatException e) { discountRate = 0; }

        try {
            taxRate = Double.parseDouble(taxField.getText().trim());
        } catch (NumberFormatException e) { taxRate = 0; }

        // 3. Calculate Finals
        double discountAmt = subtotal * (discountRate / 100);
        double taxAmt = subtotal * (taxRate / 100);
        double grandTotal = subtotal - discountAmt + taxAmt;

        // 4. Update UI
        subtotalLabel.setText("₹" + df.format(subtotal));
        discountLabel.setText("-₹" + df.format(discountAmt));
        taxLabel.setText("₹" + df.format(taxAmt));
        totalLabel.setText("₹" + df.format(grandTotal));

        // 5. Update Preview Text
        updateTextPreview(subtotal, discountAmt, taxAmt, grandTotal);
    }
    
    private void updateTextPreview(double sub, double disc, double tax, double grand) {
        StringBuilder sb = new StringBuilder();
        String customer = customerField.getText().trim().isEmpty() ? "Walk-in Customer" : customerField.getText();
        
        sb.append("======================================\n");
        sb.append("       SMARTBILL PRO INVOICE         \n");
        sb.append("======================================\n");
        sb.append("Date: ").append(sdf.format(new Date())).append("\n");
        sb.append("Customer: ").append(customer).append("\n");
        sb.append("--------------------------------------\n");
        
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            sb.append(String.format("%-20s %5s x%2s = %8s\n",
                tableModel.getValueAt(i, 0),
                tableModel.getValueAt(i, 1),
                tableModel.getValueAt(i, 2),
                tableModel.getValueAt(i, 3)));
        }
        
        sb.append("--------------------------------------\n");
        sb.append(String.format("Subtotal:           %12s\n", df.format(sub)));
        sb.append(String.format("Discount (%.1f%%):   %12s\n", discountRate, "-" + df.format(disc)));
        sb.append(String.format("Tax (%.1f%%):        %12s\n", taxRate, df.format(tax)));
        sb.append("======================================\n");
        sb.append(String.format("GRAND TOTAL:         %12s\n", df.format(grand)));
        sb.append("======================================\n");
        sb.append("      Thank you for your business!    \n");
        
        previewArea.setText(sb.toString());
    }

    private void resetSystem() {
        int confirm = JOptionPane.showConfirmDialog(this, 
            "Start a new customer bill? All current data will be lost.", 
            "New Bill", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            tableModel.setRowCount(0);
            clearInputFields();
            customerField.setText("");
            discountField.setText("0");
            taxField.setText("0");
            updateCalculations();
            statusLabel.setText("🆕 New Bill Started");
        }
    }

    // --- PDF EXPORT (The Professional Version) ---
    private void exportToPDF() {
        if (tableModel.getRowCount() == 0) {
            showWarning("No items to export!");
            return;
        }

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save Invoice as PDF");
        fileChooser.setSelectedFile(new File("Invoice_" + System.currentTimeMillis() + ".pdf"));
        
        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            // Ensure .pdf extension
            if (!file.getName().toLowerCase().endsWith(".pdf")) {
                file = new File(file.getParentFile(), file.getName() + ".pdf");
            }

            try {
                Document document = new Document();
                PdfWriter.getInstance(document, new FileOutputStream(file));
                document.open();

                // 1. Fonts
                Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, new Color(41, 128, 185));
                Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 12);
                Font tableHeaderFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, Color.WHITE);
                
                // 2. Title & Info
                Paragraph title = new Paragraph("SMARTBILL PRO - INVOICE", headerFont);
                title.setAlignment(Element.ALIGN_CENTER);
                title.setSpacingAfter(10f);
                document.add(title);

                String customer = customerField.getText().trim().isEmpty() ? "Walk-in Customer" : customerField.getText();
                document.add(new Paragraph("Customer: " + customer, normalFont));
                document.add(new Paragraph("Date: " + sdf.format(new Date()), normalFont));
                
                // 3. Items Table (Professional Layout)
                PdfPTable table = new PdfPTable(4);
                table.setWidthPercentage(100);
                table.setWidths(new float[]{3, 1.5f, 1, 1.5f}); // Column ratios
                table.setSpacingBefore(20f);
                table.setSpacingAfter(20f);

                // Table Header
                PdfPCell cell = new PdfPCell(new Phrase("Item Name", tableHeaderFont));
                cell.setBackgroundColor(new Color(44, 62, 80));
                table.addCell(cell);
                
                cell = new PdfPCell(new Phrase("Price", tableHeaderFont));
                cell.setBackgroundColor(new Color(44, 62, 80));
                cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                table.addCell(cell);
                
                cell = new PdfPCell(new Phrase("Qty", tableHeaderFont));
                cell.setBackgroundColor(new Color(44, 62, 80));
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(cell);
                
                cell = new PdfPCell(new Phrase("Total", tableHeaderFont));
                cell.setBackgroundColor(new Color(44, 62, 80));
                cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                table.addCell(cell);

                // Table Rows
                for (int i = 0; i < tableModel.getRowCount(); i++) {
                    table.addCell(tableModel.getValueAt(i, 0).toString()); // Name
                    
                    cell = new PdfPCell(new Phrase(tableModel.getValueAt(i, 1).toString()));
                    cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                    table.addCell(cell);
                    
                    cell = new PdfPCell(new Phrase(tableModel.getValueAt(i, 2).toString()));
                    cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                    table.addCell(cell);
                    
                    cell = new PdfPCell(new Phrase(tableModel.getValueAt(i, 3).toString()));
                    cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                    table.addCell(cell);
                }
                document.add(table);

                // 4. Summary Section
                double discAmt = subtotal * (discountRate / 100);
                double taxAmt = subtotal * (taxRate / 100);
                double grand = subtotal - discAmt + taxAmt;

                Paragraph summary = new Paragraph();
                summary.add(new Chunk("Subtotal: ", normalFont));
                summary.add(new Chunk(df.format(subtotal) + "\n", FontFactory.getFont(FontFactory.HELVETICA_BOLD)));
                summary.add(new Chunk("Discount: ", normalFont));
                summary.add(new Chunk("-" + df.format(discAmt) + "\n", FontFactory.getFont(FontFactory.HELVETICA_BOLD)));
                summary.add(new Chunk("Tax: ", normalFont));
                summary.add(new Chunk(df.format(taxAmt) + "\n", FontFactory.getFont(FontFactory.HELVETICA_BOLD)));
                summary.add(new Chunk("GRAND TOTAL: ", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14)));
                summary.add(new Chunk(df.format(grand), FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, new Color(39, 174, 96))));
                
                document.add(summary);
                
                // 5. Footer
                document.add(new Paragraph("\nThank you for your business!", normalFont));

                document.close();
                JOptionPane.showMessageDialog(this, "PDF Saved Successfully!\n" + file.getAbsolutePath(), "Success", JOptionPane.INFORMATION_MESSAGE);
                statusLabel.setText("💾 PDF Exported to: " + file.getName());

            } catch (Exception ex) {
                ex.printStackTrace();
                showError("PDF Export Failed: " + ex.getMessage());
            }
        }
    }

    // --- PRINTING ---
    @Override
    public int print(Graphics g, PageFormat pf, int pageIndex) {
        if (pageIndex > 0) return NO_SUCH_PAGE;
        
        Graphics2D g2d = (Graphics2D) g;
        g2d.translate(pf.getImageableX(), pf.getImageableY());
        
        // Simple Print Logic using the text from Preview
        String[] lines = previewArea.getText().split("\n");
        int y = 50;
        g2d.setFont(new Font("Monospaced", Font.PLAIN, 12));
        
        for (String line : lines) {
            if (y > pf.getImageableHeight()) break; 
            g2d.drawString(line, 20, y);
            y += 15;
        }
        return PAGE_EXISTS;
    }

    private void printBill() {
        if (tableModel.getRowCount() == 0) { showWarning("Nothing to print!"); return; }
        PrinterJob job = PrinterJob.getPrinterJob();
        job.setPrintable(this);
        if (job.printDialog()) {
            try { job.print(); } 
            catch (PrinterException ex) { showError("Print Error: " + ex.getMessage()); }
        }
    }

    // --- UTILS ---
    private JButton createStyledButton(String text, Color color) {
        JButton btn = new JButton(text);
        btn.setBackground(color);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setBorder(new EmptyBorder(8, 15, 8, 15));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { btn.setBackground(color.brighter()); }
            public void mouseExited(MouseEvent e) { btn.setBackground(color); }
        });
        return btn;
    }

    private void showAboutDialog() {
        JOptionPane.showMessageDialog(this, 
            "SmartBill Pro v5.0\nDeveloped by: [Your Name Here]\nA Professional Java Swing Application", 
            "About", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE);
        statusLabel.setText("❌ " + msg);
    }

    private void showWarning(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Warning", JOptionPane.WARNING_MESSAGE);
        statusLabel.setText("⚠️ " + msg);
    }

    // --- SPLASH SCREEN ---
    private static void showSplashScreen() {
        JWindow splash = new JWindow();
        splash.setBackground(new Color(41, 128, 185));
        
        JPanel content = new JPanel(new BorderLayout());
        content.setBackground(new Color(41, 128, 185));
        
        JLabel title = new JLabel("🏪 SmartBill Pro", JLabel.CENTER);
        title.setForeground(Color.WHITE);
        title.setFont(new Font("Segoe UI", Font.BOLD, 36));
        
        JLabel subtitle = new JLabel("Loading Professional Modules...", JLabel.CENTER);
        subtitle.setForeground(new Color(200, 200, 200));
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        
        content.add(title, BorderLayout.CENTER);
        content.add(subtitle, BorderLayout.SOUTH);
        content.setBorder(new EmptyBorder(40, 40, 40, 40));
        
        splash.add(content);
        splash.setSize(400, 250);
        splash.setLocationRelativeTo(null);
        
        splash.setVisible(true);
        
        // Close splash after 2.5 seconds
        try { Thread.sleep(2500); } catch (InterruptedException e) {}
        splash.dispose();
    }

    public static void main(String[] args) {
        // Show Splash Screen on a separate thread before loading UI
        Thread splashThread = new Thread(() -> {
            showSplashScreen();
            SwingUtilities.invokeLater(() -> new BillingSystemFinal());
        });
        splashThread.start();
    }
}