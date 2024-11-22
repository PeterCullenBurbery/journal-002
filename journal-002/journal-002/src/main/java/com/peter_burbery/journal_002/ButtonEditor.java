package com.peter_burbery.journal_002;

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
	private Connection connection; // Use shared connection

	public ButtonEditor(JTable table, DefaultTableModel model, Connection connection) {
		this.table = table;
		this.model = model;
		this.connection = connection;

		button = new JButton("Edit");
		button.addActionListener(e -> {
			int row = table.getEditingRow();
			if (row >= 0) {
				String currentText = (String) model.getValueAt(row, 0); // Get the current text from the row
				showLargeTextEditDialog(currentText, row);
			}
			fireEditingStopped();
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

	private void showLargeTextEditDialog(String currentText, int row) {
		JFrame editFrame = new JFrame("Edit Journal Entry");
		editFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		editFrame.setLayout(new BorderLayout());
		editFrame.setSize(600, 400);
		editFrame.setResizable(true);
		editFrame.setLocationRelativeTo(null);

		JTextArea textArea = new JTextArea(currentText);
		textArea.setLineWrap(true);
		textArea.setWrapStyleWord(true);

		JScrollPane scrollPane = new JScrollPane(textArea);
		editFrame.add(scrollPane, BorderLayout.CENTER);

		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		JButton saveButton = new JButton("Save");
		JButton cancelButton = new JButton("Cancel");

		saveButton.addActionListener(e -> saveChanges(row, textArea.getText(), editFrame));
		cancelButton.addActionListener(e -> editFrame.dispose());

		buttonPanel.add(saveButton);
		buttonPanel.add(cancelButton);
		editFrame.add(buttonPanel, BorderLayout.SOUTH);

		editFrame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				saveChanges(row, textArea.getText(), editFrame);
			}
		});

		editFrame.setVisible(true);
	}

	private void saveChanges(int row, String updatedText, JFrame editFrame) {
		model.setValueAt(updatedText, row, 0);
		updateDatabase(row, updatedText);
		editFrame.dispose();
	}

	private void updateDatabase(int row, String updatedText) {
		String entryId = (String) model.getValueAt(row, 1);
		String updateSQL = "UPDATE JOURNAL_002 SET JOURNAL_002 = ? WHERE JOURNAL_002_ID = ?";
		try (PreparedStatement pstmt = connection.prepareStatement(updateSQL)) {
			pstmt.setString(1, updatedText);
			pstmt.setString(2, entryId);
			pstmt.executeUpdate();
		} catch (SQLException ex) {
			ex.printStackTrace();
			JOptionPane.showMessageDialog(table, "Failed to update database: " + ex.getMessage(), "Database Error",
					JOptionPane.ERROR_MESSAGE);
		}
	}
}
