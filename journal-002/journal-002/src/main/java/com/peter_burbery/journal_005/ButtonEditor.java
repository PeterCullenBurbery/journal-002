package com.peter_burbery.journal_005;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

class ButtonEditor extends AbstractCellEditor implements TableCellEditor {
	private static final long serialVersionUID = 1L;

	private JButton button;
	private JTable table;
	private DefaultTableModel model;
	private Connection connection;

	public ButtonEditor(JTable table, DefaultTableModel model, Connection connection) {
		this.table = table;
		this.model = model;
		this.connection = connection;

		button = new JButton("Edit");
		button.addActionListener(e -> {
			int row = table.getEditingRow();
			if (row >= 0) {
				String currentText = (String) model.getValueAt(row, 0); // Get the current journal entry
				String entryId = (String) model.getValueAt(row, 1); // Get the hidden ID
				showEditDialog(currentText, row, entryId);
			}
			fireEditingStopped(); // Ensure the cell exits edit mode
		});
	}

	@Override
	public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
		return button;
	}

	@Override
	public Object getCellEditorValue() {
		return "Edit";
	}

	private void showEditDialog(String currentText, int row, String entryId) {
		JFrame editFrame = new JFrame("Edit Journal Entry");
		editFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		editFrame.setLayout(new BorderLayout());
		editFrame.setSize(400, 300);
		editFrame.setResizable(true);
		editFrame.setLocationRelativeTo(null);

		// Text area for editing the entry
		JTextArea textArea = new JTextArea(currentText);
		textArea.setLineWrap(true);
		textArea.setWrapStyleWord(true);

		JScrollPane scrollPane = new JScrollPane(textArea);
		editFrame.add(scrollPane, BorderLayout.CENTER);

		// Buttons for Save and Cancel
		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		JButton saveButton = new JButton("Save");
		JButton cancelButton = new JButton("Cancel");

		saveButton.addActionListener(e -> {
			String updatedText = textArea.getText();
			updateDatabase(entryId, updatedText); // Update in the database
			model.setValueAt(updatedText, row, 0); // Update in the table
			editFrame.dispose();
		});

		cancelButton.addActionListener(e -> editFrame.dispose());

		buttonPanel.add(saveButton);
		buttonPanel.add(cancelButton);
		editFrame.add(buttonPanel, BorderLayout.SOUTH);

		editFrame.setVisible(true);
	}

	private void updateDatabase(String entryId, String updatedText) {
		String updateSQL = "UPDATE JOURNAL_002 SET JOURNAL_002 = ? WHERE JOURNAL_002_ID = ?";
		try (PreparedStatement pstmt = connection.prepareStatement(updateSQL)) {
			pstmt.setString(1, updatedText);
			pstmt.setString(2, entryId);
			pstmt.executeUpdate();
			System.out.println("Updated journal entry for JOURNAL_002_ID: " + entryId);
		} catch (SQLException ex) {
			ex.printStackTrace();
			JOptionPane.showMessageDialog(table, "Failed to update database: " + ex.getMessage(), "Database Error",
					JOptionPane.ERROR_MESSAGE);
		}
	}
}
