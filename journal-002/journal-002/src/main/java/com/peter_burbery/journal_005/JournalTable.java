package com.peter_burbery.journal_005;

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

				DefaultTableModel model = new DefaultTableModel(
						new Object[] { "Journal Entry", "JOURNAL_002_ID", "Edit", "Date Created", "Date Updated" }, 0) {
					private static final long serialVersionUID = 1L;

					@Override
					public boolean isCellEditable(int row, int column) {
						return column == 0 || column == 2; // Only "Journal Entry" and "Edit" columns are editable
					}
				};

				JTable table = new JTable(model);

				// Disable auto-resize behavior to allow custom resizing
				table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
				int editColumnWidth = 60; // Fixed width for "Edit" column
				int dateCreatedWidth = 180; // Fixed width for "Date Created" column
				int dateUpdatedWidth = 180; // Fixed width for "Date Updated" column

				// Configure column widths
				table.getColumnModel().getColumn(2).setPreferredWidth(editColumnWidth); // Edit Button
				table.getColumnModel().getColumn(2).setMaxWidth(editColumnWidth);
				table.getColumnModel().getColumn(2).setMaxWidth(editColumnWidth);
				table.getColumnModel().getColumn(3).setPreferredWidth(dateCreatedWidth); // Date Created
				table.getColumnModel().getColumn(3).setMinWidth(dateCreatedWidth);
				table.getColumnModel().getColumn(3).setMaxWidth(dateCreatedWidth);
				table.getColumnModel().getColumn(4).setPreferredWidth(dateUpdatedWidth); // Date Updated
				table.getColumnModel().getColumn(4).setMaxWidth(dateUpdatedWidth);
				table.getColumnModel().getColumn(4).setMaxWidth(dateUpdatedWidth);
				// Hide the ID column (JOURNAL_002_ID)
				table.removeColumn(table.getColumnModel().getColumn(1));

				// Configure "Edit" column renderer and editor
				table.getColumn("Edit").setCellRenderer(new ButtonRenderer());
				table.getColumn("Edit").setCellEditor(new ButtonEditor(table, model, connection));

				JScrollPane scrollPane = new JScrollPane(table);
				frame.add(scrollPane, BorderLayout.CENTER);

				// Add ComponentListener for dynamic resizing
				scrollPane.addComponentListener(new java.awt.event.ComponentAdapter() {
					@Override
					public void componentResized(java.awt.event.ComponentEvent e) {
						int totalWidth = scrollPane.getViewport().getWidth();
						int editColumnWidth = 60; // Fixed width for "Edit" column
						int dateCreatedWidth = 180; // Fixed width for "Date Created" column
						int dateUpdatedWidth = 180; // Fixed width for "Date Updated" column

						int fixedWidth = editColumnWidth + dateCreatedWidth + dateUpdatedWidth;
						int availableWidth = totalWidth - fixedWidth - table.getIntercellSpacing().width * 2;

						if (availableWidth > 100) {
							table.getColumnModel().getColumn(0).setPreferredWidth(availableWidth); // Journal Entry
																									// column
						} else {
							table.getColumnModel().getColumn(0).setPreferredWidth(100); // Minimum width for Journal
																						// Entry
						}

						// Set fixed widths for other columns
						table.getColumnModel().getColumn(1).setPreferredWidth(editColumnWidth); // Edit Button
						table.getColumnModel().getColumn(1).setMaxWidth(editColumnWidth);
						table.getColumnModel().getColumn(1).setMaxWidth(editColumnWidth);
						table.getColumnModel().getColumn(2).setPreferredWidth(dateCreatedWidth); // Date Created
						table.getColumnModel().getColumn(2).setMinWidth(dateCreatedWidth);
						table.getColumnModel().getColumn(2).setMaxWidth(dateCreatedWidth);
						table.getColumnModel().getColumn(3).setPreferredWidth(dateUpdatedWidth); // Date Updated
						table.getColumnModel().getColumn(3).setMaxWidth(dateUpdatedWidth);
						table.getColumnModel().getColumn(3).setMaxWidth(dateUpdatedWidth);
						table.revalidate();
						table.repaint();
					}
				});

				// Declare sortingDialog first, initialize later
				SortingOptionsDialog[] sortingDialog = new SortingOptionsDialog[1];
				sortingDialog[0] = new SortingOptionsDialog(frame, () -> loadJournalData(model, sortingDialog[0]));

				// Configure Add and Remove buttons
				JPanel buttonPanel = new JPanel(new BorderLayout());
				JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
				JButton addButton = new JButton("Add Entry");
				JButton removeButton = new JButton("Remove Entry");

				addButton.addActionListener(e -> {
					String newEntry = "Entry";
					String newEntryId = addToDatabase(newEntry);
					if (newEntryId != null) {
						model.addRow(new Object[] { newEntry, newEntryId, "Edit", null, null });
					}
				});

				removeButton.addActionListener(e -> {
					int selectedRow = table.getSelectedRow();
					if (selectedRow != -1) {
						String entryId = (String) model.getValueAt(selectedRow, 1);
						model.removeRow(selectedRow);
						deleteFromDatabase(entryId);
					} else {
						JOptionPane.showMessageDialog(frame, "No row selected!", "Error", JOptionPane.WARNING_MESSAGE);
					}
				});

				leftPanel.add(addButton);
				leftPanel.add(removeButton);
				buttonPanel.add(leftPanel, BorderLayout.WEST);

				JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 5));
				JButton sortingOptionsButton = new JButton("Sorting Options");

				sortingOptionsButton.addActionListener(e -> sortingDialog[0].setVisible(true));
                // Add top button panel
                JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
                JButton findButton = new JButton("Find");
                topPanel.add(findButton);
                frame.add(topPanel, BorderLayout.NORTH);
                // Find button launches FindDialog
                findButton.addActionListener(e -> new FindDialog(frame, table, model).setVisible(true));

				rightPanel.add(sortingOptionsButton);
				buttonPanel.add(rightPanel, BorderLayout.EAST);

				frame.add(buttonPanel, BorderLayout.SOUTH);

				// Load data initially
				loadJournalData(model, sortingDialog[0]);

				frame.setSize(1400, 800);
				frame.setLocationRelativeTo(null); // Center the frame on the screen
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


	private static void loadJournalData(DefaultTableModel model, SortingOptionsDialog sortingOptionsDialog) {
	    model.setRowCount(0); // Clear existing rows

	    String query = "SELECT JOURNAL_002, JOURNAL_002_ID, date_created, date_updated " +
	                   "FROM JOURNAL_002 " +
	                   "ORDER BY date_created DESC"; // Default sorting by date_created DESC

	    // Apply sorting logic if sorting is enabled
	    if (sortingOptionsDialog != null && sortingOptionsDialog.isSortingEnabled()) {
	        query = "SELECT JOURNAL_002, JOURNAL_002_ID, date_created, date_updated FROM JOURNAL_002 " +
	                "ORDER BY " + sortingOptionsDialog.getSortColumn() + 
	                (sortingOptionsDialog.getSortDirection().equals("Ascending") ? " ASC" : " DESC") + 
	                (sortingOptionsDialog.getNullHandling().equals("Nulls First") ? " NULLS FIRST" : " NULLS LAST");
	    }

	    try (Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery(query)) {
	        while (rs.next()) {
	            model.addRow(new Object[] {
	                rs.getString("JOURNAL_002"), // Journal Entry
	                rs.getString("JOURNAL_002_ID"), // Journal Entry ID (hidden)
	                "Edit", // Edit Button
	                rs.getString("date_created"), // Date Created
	                rs.getString("date_updated")  // Date Updated
	            });
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
