package com.peter_burbery.journal_006;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.util.regex.Pattern;

public class FindWindow extends JFrame {

    private final JTable table;
    private final DefaultTableModel originalModel;

    public FindWindow(JTable table, DefaultTableModel model) {
        super("Find Rows");
        this.table = table;
        this.originalModel = model;

        setSize(400, 200);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());
        setLocationRelativeTo(null);

        // Input field for regex pattern
        JTextField regexField = new JTextField(20);

        // Buttons for filtering
        JButton findButton = new JButton("Find");
        JButton resetButton = new JButton("Reset");

        // Layout for input and Find button
        JPanel inputPanel = new JPanel(new FlowLayout());
        inputPanel.add(new JLabel("Enter Regex:"));
        inputPanel.add(regexField);
        inputPanel.add(findButton);
        add(inputPanel, BorderLayout.CENTER);

        // Layout for Reset and Close buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        buttonPanel.add(resetButton);
        add(buttonPanel, BorderLayout.SOUTH);

        // Action listener for the Find button
        findButton.addActionListener(e -> {
            String regex = regexField.getText();
            if (!regex.isEmpty()) {
                applyRegexFilter(regex);
            } else {
                JOptionPane.showMessageDialog(this, "Please enter a regex pattern!", "Error",
                        JOptionPane.WARNING_MESSAGE);
            }
        });

        // Action listener for the Reset button
        resetButton.addActionListener(e -> resetFilter());
    }

    private void applyRegexFilter(String regex) {
        try {
            Pattern pattern = Pattern.compile(regex);

            // Create a TableRowSorter to apply the regex filter
            TableRowSorter<TableModel> sorter = new TableRowSorter<>(originalModel);
            table.setRowSorter(sorter);

            // Apply the regex filter to the "Journal Entry" column (index 0)
            sorter.setRowFilter(RowFilter.regexFilter(regex, 0));

            JOptionPane.showMessageDialog(this, "Search complete!", "Find Rows", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Invalid regex pattern!", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void resetFilter() {
        // Reset the table's row sorter to null to remove filtering
        table.setRowSorter(null);
        JOptionPane.showMessageDialog(this, "Filtering reset. All rows are now visible.", "Reset Filter",
                JOptionPane.INFORMATION_MESSAGE);
    }
}
