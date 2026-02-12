🏪 SmartBill Pro
JavaSwingLicense

A robust, user-friendly Point of Sale (POS) and billing management application built using Java Swing. SmartBill Pro allows users to generate invoices, manage taxes/discounts, print receipts, and export data to Text or PDF formats.

✨ Features
Feature	Description
📝 Billing Management	Add items with dynamic pricing and quantity calculation.
💰 Automatic Calculations	Real-time calculation of Subtotals, Discounts, Tax (GST), and Grand Total.
👁️ Live Preview	View the receipt layout in real-time within the application.
📤 Export Options	Save invoices as formatted Text (.txt) files or professional PDF documents.
🖨️ Printing Support	Native support for printing receipts directly to connected printers.
⚙️ Customizable Settings	Apply global discount or tax percentages to the entire bill.
🎨 Modern UI	Professional interface with gradient headers, splash screens, and tabbed navigation.

📸 Screenshots




🛠️ Technology Stack
Language: Java
GUI Framework: Java Swing (javax.swing)

📋 Prerequisites
Before running this project, ensure you have the following installed:
Java Development Kit (JDK) 8 or higher.
An Integrated Development Environment (IDE):
1.Eclipse
2.IntelliJ IDEA
3.VS Code
4.NetBeans


💻 Usage Guide
1.Start Billing:
Enter the Customer Name in the top header.
In the "Billing" tab, enter Item Name, Price, and Quantity.
Click "Add to Bill" (or press Enter in the Qty field).
2.Apply Adjustments:
Switch to the "Report & Settings" tab.
Enter a global Discount % or Tax %.
Click "Recalculate Totals" to see the updated Grand Total.
3.Export/Print:
Save Text: Saves a simple text receipt.
Export PDF: Generates a professional formatted invoice (Requires iText library).
Print: Opens the system print dialog.


