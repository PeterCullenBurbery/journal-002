package com.peter_burbery.journal_006;

import javax.swing.*;
import java.awt.*;

public class SortingOptionsDialog extends JDialog {

    private JCheckBox enableSortingCheckbox;
    private JComboBox<String> sortColumnDropdown;
    private JComboBox<String> sortDirectionDropdown;
    private JComboBox<String> nullHandlingDropdown;

    private boolean sortingEnabled = false;
    private String sortColumn = "date_created";
    private String sortDirection = "Ascending";
    private String nullHandling = "Nulls First";

    private final Runnable reloadDataCallback; // Callback to reload data

    public SortingOptionsDialog(JFrame parent, Runnable reloadDataCallback) {
        super(parent, "Sorting Options", true);
        this.reloadDataCallback = reloadDataCallback;

        setSize(400, 300);
        setLayout(new BorderLayout());
        setLocationRelativeTo(parent);

        JPanel mainPanel = new JPanel(new GridLayout(4, 2, 10, 10));

        // Enable sorting checkbox
        enableSortingCheckbox = new JCheckBox("Enable Sorting", true); // Default enabled
        enableSortingCheckbox.addActionListener(e -> updateComponentsState());
        mainPanel.add(new JLabel("Enable Sorting:"));
        mainPanel.add(enableSortingCheckbox);

        // Sort column dropdown
        sortColumnDropdown = new JComboBox<>(new String[]{"date_created", "date_updated"});
        sortColumnDropdown.setSelectedItem("date_created"); // Default sort column
        mainPanel.add(new JLabel("Sort Column:"));
        mainPanel.add(sortColumnDropdown);

        // Sort direction dropdown
        sortDirectionDropdown = new JComboBox<>(new String[]{"Ascending", "Descending"});
        sortDirectionDropdown.setSelectedItem("Descending"); // Default sort direction
        mainPanel.add(new JLabel("Sort Direction:"));
        mainPanel.add(sortDirectionDropdown);

        // Null handling dropdown
        nullHandlingDropdown = new JComboBox<>(new String[]{"Nulls First", "Nulls Last"});
        nullHandlingDropdown.setSelectedItem("Nulls Last"); // Default null handling
        mainPanel.add(new JLabel("Null Handling:"));
        mainPanel.add(nullHandlingDropdown);

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton saveButton = new JButton("Save");
        JButton cancelButton = new JButton("Cancel");

        saveButton.addActionListener(e -> {
            savePreferences();
            reloadDataCallback.run(); // Trigger the reload
            setVisible(false);
        });

        cancelButton.addActionListener(e -> setVisible(false));

        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);

        add(mainPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        updateComponentsState();
    }


    private void updateComponentsState() {
        boolean enabled = enableSortingCheckbox.isSelected();
        sortColumnDropdown.setEnabled(enabled);
        sortDirectionDropdown.setEnabled(enabled);
        nullHandlingDropdown.setEnabled(enabled);
    }

    private void savePreferences() {
        sortingEnabled = enableSortingCheckbox.isSelected();
        sortColumn = (String) sortColumnDropdown.getSelectedItem();
        sortDirection = (String) sortDirectionDropdown.getSelectedItem();
        nullHandling = (String) nullHandlingDropdown.getSelectedItem();
    }

    public boolean isSortingEnabled() {
        return sortingEnabled;
    }

    public String getSortColumn() {
        return sortColumn;
    }

    public String getSortDirection() {
        return sortDirection;
    }

    public String getNullHandling() {
        return nullHandling;
    }
}
