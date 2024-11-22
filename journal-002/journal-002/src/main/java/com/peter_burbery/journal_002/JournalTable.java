package com.peter_burbery.journal_002;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;
import java.awt.*;
import java.sql.*;

public class JournalTable {

	// Database connection and schema details
	private static final String DB_URL = "jdbc:oracle:thin:@localhost:1521/JOURNAL_002.localdomain";
	private static final String DB_SYS_USER = "sys as sysdba";
	private static final String DB_PASSWORD = "1234";
	private static final String SCHEMA = "JOURNAL_002";

	private static Connection connection = null;

	public static void main(String[] args) {
		SwingUtilities.invokeLater(() -> {
			if (initializeDatabaseConnection()) {
				JFrame frame = new JFrame("Journal Tracker");
				frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				frame.setLayout(new BorderLayout());

				// Define table model with three columns
				DefaultTableModel model = new DefaultTableModel(
						new Object[] { "Journal Entry", "JOURNAL_002_ID", "Edit" }, 0) {
					private static final long serialVersionUID = 1L;

					@Override
					public boolean isCellEditable(int row, int column) {
						return column == 0 || column == 2; // Only "Journal Entry" and "Edit" columns are editable
					}
				};

				JTable table = new JTable(model);

				// Disable auto-resize behavior to allow custom resizing
				table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

				// Set fixed width for the "Edit" column
				table.getColumnModel().getColumn(2).setPreferredWidth(60);
				table.getColumnModel().getColumn(2).setMaxWidth(60);
				table.getColumnModel().getColumn(2).setMinWidth(60);

				// Hide the ID column (JOURNAL_002_ID)
				table.removeColumn(table.getColumnModel().getColumn(1));

				JScrollPane scrollPane = new JScrollPane(table);
				frame.add(scrollPane, BorderLayout.CENTER);

				// Add ComponentListener for dynamic resizing
				scrollPane.addComponentListener(new java.awt.event.ComponentAdapter() {
					@Override
					public void componentResized(java.awt.event.ComponentEvent e) {
						int totalWidth = scrollPane.getViewport().getWidth();
						int editColumnWidth = 60; // Fixed width for "Edit" column
						int availableWidth = totalWidth - editColumnWidth - table.getIntercellSpacing().width * 2;

						if (availableWidth > 100) {
							table.getColumnModel().getColumn(0).setPreferredWidth(availableWidth);
						} else {
							table.getColumnModel().getColumn(0).setPreferredWidth(100); // Minimum width for "Journal
																						// Entry"
						}

						table.revalidate();
						table.repaint();
					}
				});

				// Set cell renderers/editors for the "Edit" column
				table.getColumn("Edit").setCellRenderer(new ButtonRenderer());
				table.getColumn("Edit").setCellEditor(new ButtonEditor(table, model, connection));

				// Add TableModelListener to handle direct edits in the "Journal Entry" column
				model.addTableModelListener(e -> {
					if (e.getType() == TableModelEvent.UPDATE) {
						int row = e.getFirstRow();
						int column = e.getColumn();

						if (column == 0) { // "Journal Entry" column
							String updatedEntry = (String) model.getValueAt(row, 0);
							String entryId = (String) model.getValueAt(row, 1); // Hidden JOURNAL_002_ID
							updateDatabase(entryId, updatedEntry);
						}
					}
				});

				// Add and Remove buttons
				JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 5, 5));
				JButton addButton = new JButton("Add Entry");
				JButton removeButton = new JButton("Remove Entry");

				addButton.addActionListener(e -> {
					String newEntry = "Entry";
					String newEntryId = addToDatabase(newEntry); // Add entry to DB and get the generated ID
					if (newEntryId != null) {
						model.addRow(new Object[] { newEntry, newEntryId, "Edit" }); // Add to table
					}
				});

				removeButton.addActionListener(e -> {
					int selectedRow = table.getSelectedRow();
					if (selectedRow != -1) {
						String entryId = (String) model.getValueAt(selectedRow, 1); // Get hidden JOURNAL_002_ID
						model.removeRow(selectedRow); // Remove row from table
						deleteFromDatabase(entryId); // Delete entry from DB
					} else {
						JOptionPane.showMessageDialog(frame, "No row selected!", "Error", JOptionPane.WARNING_MESSAGE);
					}
				});

				buttonPanel.add(addButton);
				buttonPanel.add(removeButton);
				frame.add(buttonPanel, BorderLayout.SOUTH);

				// Load existing journal entries into the table
				loadJournalData(model);

				frame.setSize(600, 400);
				frame.setVisible(true);
			} else {
				JOptionPane.showMessageDialog(null, "Failed to connect to the database.", "Database Error",
						JOptionPane.ERROR_MESSAGE);
				System.exit(1);
			}
		});
	}

	private static boolean initializeDatabaseConnection() {
		try {
			connection = DriverManager.getConnection(DB_URL, DB_SYS_USER, DB_PASSWORD);
			try (PreparedStatement alterSessionStmt = connection
					.prepareStatement("ALTER SESSION SET CURRENT_SCHEMA = " + SCHEMA)) {
				alterSessionStmt.execute();
			}
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}

	private static void loadJournalData(DefaultTableModel model) {
		String query = "SELECT JOURNAL_002, JOURNAL_002_ID FROM JOURNAL_002";
		try (Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery(query)) {
			while (rs.next()) {
				model.addRow(new Object[] { rs.getString("JOURNAL_002"), rs.getString("JOURNAL_002_ID"), "Edit" });
			}
		} catch (SQLException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, "Error loading data: " + e.getMessage(), "Database Error",
					JOptionPane.ERROR_MESSAGE);
		}
	}

	private static String addToDatabase(String entry) {
		String insertSQL = "INSERT INTO JOURNAL_002 (JOURNAL_002) VALUES (?)";
		try (PreparedStatement pstmt = connection.prepareStatement(insertSQL, new String[] { "JOURNAL_002_ID" })) {
			pstmt.setString(1, entry);
			pstmt.executeUpdate();
			try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
				if (generatedKeys.next()) {
					return generatedKeys.getString(1);
				}
			}
		} catch (SQLException ex) {
			ex.printStackTrace();
		}
		return null;
	}

	private static void deleteFromDatabase(String entryId) {
		String deleteSQL = "DELETE FROM JOURNAL_002 WHERE JOURNAL_002_ID = ?";
		try (PreparedStatement pstmt = connection.prepareStatement(deleteSQL)) {
			pstmt.setString(1, entryId);
			pstmt.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	// Custom button renderer for the "Edit" button
	static class ButtonRenderer extends JButton implements TableCellRenderer {
		private static final long serialVersionUID = 1L;

		public ButtonRenderer() {
			setText("Edit");
		}

		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
				int row, int column) {
			return this;
		}
	}

	private static void updateDatabase(String entryId, String updatedText) {
		String updateSQL = "UPDATE JOURNAL_002 SET JOURNAL_002 = ? WHERE JOURNAL_002_ID = ?";
		try (PreparedStatement pstmt = connection.prepareStatement(updateSQL)) {
			pstmt.setString(1, updatedText);
			pstmt.setString(2, entryId);
			pstmt.executeUpdate();
			System.out.println("Updated journal entry for JOURNAL_002_ID: " + entryId);
		} catch (SQLException ex) {
			ex.printStackTrace();
			JOptionPane.showMessageDialog(null, "Failed to update database: " + ex.getMessage(), "Database Error",
					JOptionPane.ERROR_MESSAGE);
		}
	}

}
