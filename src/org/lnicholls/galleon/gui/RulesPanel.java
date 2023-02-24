package org.lnicholls.galleon.gui;

/*
 * Copyright (C) 2005 Leon Nicholls
 * 
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation; either version 2 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program; if not, write to the Free
 * Software Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 * 
 * See the file "COPYING" for more details.
 */

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Iterator;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

import org.apache.log4j.Logger;
import org.lnicholls.galleon.server.TiVo;
import org.lnicholls.galleon.togo.Rule;
import org.lnicholls.galleon.util.NameValue;
import org.lnicholls.galleon.util.Tools;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.factories.ButtonBarFactory;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

public class RulesPanel extends JPanel implements ActionListener, KeyListener {

	private static Logger log = Logger.getLogger(RulesPanel.class.getName());

	private static class ColumnData {
		public String mTitle;

		public int mWidth;

		public int mAlignment;

		public ColumnData(String title, int width, int alignment) {
			mTitle = title;
			mWidth = width;
			mAlignment = alignment;
		}
	}

	private static final ColumnData mColumns[] = {
			new ColumnData("Criteria", 50, JLabel.LEFT),
			new ColumnData("Comparison", 50, JLabel.LEFT),
			new ColumnData("Value", 200, JLabel.LEFT),
			new ColumnData("TiVo", 50, JLabel.LEFT),
			new ColumnData("Download", 30, JLabel.CENTER) };

	class ReloadWrapper extends NameValue {
		public ReloadWrapper(String name, String value) {
			super(name, value);
		}

		public String toString() {
			return getName();
		}
	}

	public RulesPanel(RecordedPanel recordedPanel) {
		super();

		mRecordedPanel = recordedPanel;
		// TODO Option to download recordings into folders?

		setLayout(new BorderLayout());

		mRules = Galleon.getRules();
		if (log.isDebugEnabled())
			log.debug("mRules=" + mRules);

		final ShowTableData showsTableData = new ShowTableData();
		mTable = new JTable();
		mTable.setDefaultRenderer(Object.class, new CustomTableCellRender());

		mCriteriaField = new JComboBox();
		mCriteriaField
				.setToolTipText("Select a criteria for automatic downloads of TiVo recordings");
		mCriteriaField.addItem(new ReloadWrapper("Title", Rule.CRITERIA_TITLE));
		mCriteriaField.addItem(new ReloadWrapper("Description",
				Rule.CRITERIA_DESCRIPTION));
		mCriteriaField.addItem(new ReloadWrapper("Channel",
				Rule.CRITERIA_CHANNEL));
		mCriteriaField.addItem(new ReloadWrapper("Station",
				Rule.CRITERIA_STATION));
		mCriteriaField
				.addItem(new ReloadWrapper("Rating", Rule.CRITERIA_RATING));
		mCriteriaField.addItem(new ReloadWrapper("Quality",
				Rule.CRITERIA_QUALITY));
		mCriteriaField.addItem(new ReloadWrapper("Genre", Rule.CRITERIA_GENRE));
		mCriteriaField.addItem(new ReloadWrapper("Type", Rule.CRITERIA_TYPE));
		mCriteriaField.addItem(new ReloadWrapper("Date", Rule.CRITERIA_DATE));
		mCriteriaField.addItem(new ReloadWrapper("Duration (mins)",
				Rule.CRITERIA_DURATION));
		mCriteriaField.addItem(new ReloadWrapper("Size (MB)",
				Rule.CRITERIA_SIZE));
		mCriteriaField.addItem(new ReloadWrapper("Flag", Rule.CRITERIA_FLAG));
		mComparisonField = new JComboBox();
		mComparisonField
				.setToolTipText("Select a comparison for the selected criteria");
		mComparisonField.addItem(new ReloadWrapper("Contains",
				Rule.COMPARISON_CONTAINS));
		mComparisonField.addItem(new ReloadWrapper("Equals",
				Rule.COMPARISON_EQUALS));
		mComparisonField.addItem(new ReloadWrapper("Starts With",
				Rule.COMPARISON_STARTS_WITH));
		mComparisonField.addItem(new ReloadWrapper("Ends With",
				Rule.COMPARISON_ENDS_WITH));
		mComparisonField.addItem(new ReloadWrapper("More Than",
				Rule.COMPARISON_MORE_THAN));
		mComparisonField.addItem(new ReloadWrapper("Less Than",
				Rule.COMPARISON_LESS_THAN));
		mValueField = new JTextField(30);
		mValueField
				.setToolTipText("Enter a value for the comparison and criteria. Empty for all");
		mValueField.addKeyListener(this);
		mTiVoField = new JComboBox();
		mTiVoField.setToolTipText("Select TiVo to download from");
		mTiVoField.addItem(new ReloadWrapper("Any", Rule.ANY_TIVO));
		List tivos = Galleon.getTiVos();
		Iterator iterator = tivos.iterator();
		while (iterator.hasNext())
		{
			TiVo tivo = (TiVo) iterator.next();
			mTiVoField.addItem(new ReloadWrapper(tivo.getName(), tivo.getName()));
		}
		mDownloadField = new JCheckBox("Download");
		mDownloadField.setToolTipText("Check to specify a rule that will automatically download recordings");
		mDownloadField.setSelected(true);

		FormLayout layout = new FormLayout(
				"left:pref, 3dlu, left:pref, right:pref:grow ", "pref, " + // criteria
						"3dlu, " + "pref, " + // comparison
						"3dlu, " + "pref, " + // value
						"3dlu, " + "pref, " + // tivo
						"3dlu, " + "pref " // download
		);

		PanelBuilder builder = new PanelBuilder(layout);
		builder.setDefaultDialogBorder();

		CellConstraints cc = new CellConstraints();

		builder.addLabel("Criteria", cc.xy(1, 1));
		builder.add(mCriteriaField, cc.xy(3, 1));
		builder.addLabel("Comparison", cc.xy(1, 3));
		builder.add(mComparisonField, cc.xy(3, 3));
		builder.addLabel("Value", cc.xy(1, 5));
		builder.add(mValueField, cc.xy(3, 5));
		builder.addLabel("TiVo", cc.xy(1, 7));
		builder.add(mTiVoField, cc.xy(3, 7));
		builder.addLabel("", cc.xy(1, 9));
		builder.add(mDownloadField, cc.xy(3, 9));

		add(builder.getPanel(), BorderLayout.NORTH);

		JPanel tablePanel = new JPanel();
		tablePanel.setLayout(new BorderLayout());

		JButton[] array = new JButton[3];
		JPanel buttonPanel = new JPanel();
		mAddButton = new JButton("Add");
		array[0] = mAddButton;
		buttonPanel.add(mAddButton);
		mAddButton.setActionCommand("add");
		mAddButton.addActionListener(this);
		mAddButton.setEnabled(true);
		mModifyButton = new JButton("Modify");
		array[1] = mModifyButton;
		buttonPanel.add(mModifyButton);
		mModifyButton.setActionCommand("modify");
		mModifyButton.addActionListener(this);
		mModifyButton.setEnabled(false);
		mDeleteButton = new JButton("Delete");
		array[2] = mDeleteButton;
		buttonPanel.add(mDeleteButton);
		mDeleteButton.setActionCommand("delete");
		mDeleteButton.addActionListener(this);
		mDeleteButton.setEnabled(false);
		JPanel buttons = ButtonBarFactory.buildCenteredBar(array);
		buttons.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		tablePanel.add(buttons, BorderLayout.NORTH);

		mTable.setModel(showsTableData);
		TableColumn column = null;
		for (int i = 0; i < 5; i++) {
			column = mTable.getColumnModel().getColumn(i);
			column.setPreferredWidth(mColumns[i].mWidth);
		}
		mTable.setDragEnabled(true);
		mTable.setRowSelectionAllowed(true);
		mTable.setColumnSelectionAllowed(false);
		ListSelectionModel selectionModel = mTable.getSelectionModel();
		selectionModel.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		selectionModel.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				// Ignore extra messages.
				if (e.getValueIsAdjusting())
					return;

				ListSelectionModel lsm = (ListSelectionModel) e.getSource();
				if (!lsm.isSelectionEmpty()) {
					if (!mUpdating) {
						int selectedRow = lsm.getMinSelectionIndex();
						Rule rule = (Rule) mRules.get(selectedRow);
						ShowTableData model = (ShowTableData) mTable.getModel();
						defaultCombo(mCriteriaField, (String) model.getValue(
								selectedRow, 0));
						defaultCombo(mComparisonField, (String) model.getValue(
								selectedRow, 1));
						mValueField.setText((String) model.getValue(
								selectedRow, 2));
						defaultCombo(mTiVoField, rule.getTiVo());
						mDownloadField.setSelected(((Boolean) model.getValue(
								selectedRow, 4)).booleanValue());
					}

					checkButtonStates();
					mDeleteButton.setEnabled(true);
				} else {
					if (!mUpdating) {
						defaultCombo(mCriteriaField, Rule.CRITERIA_TITLE);
						defaultCombo(mComparisonField, Rule.COMPARISON_CONTAINS);
						mValueField.setText(" ");
						defaultCombo(mTiVoField, Rule.ANY_TIVO);
						mDownloadField.setSelected(false);
					}

					mDeleteButton.setEnabled(false);
					checkButtonStates();
				}
			}
		});
		if (mTable.getModel().getRowCount() > 0)
			mTable.setRowSelectionInterval(0, 0);

		JTableHeader header = mTable.getTableHeader();
		header.setUpdateTableInRealTime(true);
		header.addMouseListener(showsTableData.new ColumnListener(mTable));
		header.setReorderingAllowed(true);

		JScrollPane scrollPane = new JScrollPane();
		scrollPane.getViewport().add(mTable);
		tablePanel.add(scrollPane, BorderLayout.CENTER);

		add(tablePanel, BorderLayout.CENTER);
	}

	public void defaultCombo(JComboBox combo, String value) {
		for (int i = 0; i < combo.getItemCount(); i++) {
			if (((NameValue) combo.getItemAt(i)).getValue().equals(value)) {
				combo.setSelectedIndex(i);
				return;
			}
		}
	}

	public void keyTyped(KeyEvent e) {
	}

	public void keyPressed(KeyEvent e) {
	}

	public void keyReleased(KeyEvent e) {
		checkButtonStates();
	}

	public void checkButtonStates() {
		boolean filled = mValueField.getText().trim().length() != 0;
		filled = true;

		if (filled) {
			mAddButton.setEnabled(true);
			if (mTable.getSelectedRowCount() > 0)
				mModifyButton.setEnabled(true);
		} else {
			mAddButton.setEnabled(false);
			mModifyButton.setEnabled(false);
		}
	}

	public void actionPerformed(ActionEvent e) {
		if ("add".equals(e.getActionCommand())) {
			mUpdating = true;
			try {
				TableModel model = mTable.getModel();
				int nextRow = model.getRowCount();
				model.setValueAt(((NameValue) mCriteriaField.getSelectedItem()).getValue(), nextRow, 0);
				model.setValueAt(((NameValue) mComparisonField.getSelectedItem()).getValue(), nextRow, 1);
				model.setValueAt(mValueField.getText(), nextRow, 2);
				model.setValueAt(((NameValue) mTiVoField.getSelectedItem()).getValue(), nextRow, 3);
				model.setValueAt(new Boolean(mDownloadField.isSelected()), nextRow, 4);
			} catch (Exception ex) {
				Tools.logException(RulesPanel.class, ex);
			}
			Galleon.updateRules(mRules);
			mRecordedPanel.updateTable();
			mUpdating = false;
		} else if ("modify".equals(e.getActionCommand())) {
			mUpdating = true;
			try {
				TableModel model = mTable.getModel();
				int selectedRow = mTable.getSelectedRow();
				if (selectedRow != -1) {
					// Why do I need to do this...?
					Object[] values = new Object[5];
					values[0] = ((NameValue) mCriteriaField.getSelectedItem()).getValue();
					values[1] = ((NameValue) mComparisonField.getSelectedItem()).getValue();
					values[2] = mValueField.getText();
					values[3] = ((NameValue) mTiVoField.getSelectedItem()).getValue();
					values[4] = new Boolean(mDownloadField.isSelected());

					for (int i = 0; i < values.length; i++) {
						model.setValueAt(values[i], selectedRow, i);
						log.info(values[i]);
					}
					mTable.addRowSelectionInterval(selectedRow, selectedRow);

					Galleon.updateRules(mRules);
					mRecordedPanel.updateTable();
				}
			} catch (Exception ex) {
				Tools.logException(RulesPanel.class, ex);
			}
			mUpdating = false;
		} else if ("delete".equals(e.getActionCommand())) {
			mUpdating = true;
			try {
				ShowTableData model = (ShowTableData) mTable.getModel();
				int[] selectedRows = mTable.getSelectedRows();
				if (selectedRows.length > 0) {
					for (int i = 0; i < selectedRows.length; i++) {
						model.removeRow(selectedRows[i]);
					}
				}
				Galleon.updateRules(mRules);
				mRecordedPanel.updateTable();
			} catch (Exception ex) {
				Tools.logException(RulesPanel.class, ex);
			}
			mUpdating = false;
		}
	}

	class ShowTableData extends AbstractTableModel {

		protected int mSortCol = 0;

		protected boolean mSortAsc = true;

		public ShowTableData() {

		}

		public int getRowCount() {
			return mRules == null ? 0 : mRules.size();
		}

		public int getColumnCount() {
			return 5;
		}

		public String getColumnName(int column) {
			String str = mColumns[column].mTitle;
			if (column == mSortCol)
				str += mSortAsc ? " »" : " «";
			return str;
		}

		public boolean isCellEditable(int nRow, int nCol) {
			return false;
		}

		public Object getValueAt(int nRow, int nCol) {
			if (nRow < 0 || nRow >= getRowCount())
				return "";
			Rule rule = (Rule) mRules.get(nRow);
			switch (nCol) {
			case 0:
				return rule.getCriteriaString();
			case 1:
				return rule.getComparisonString();
			case 2:
				return rule.getValue();
			case 3:
				if (rule.getTiVo().equals(Rule.ANY_TIVO))
					return "Any";
				else
					return rule.getTiVo();
			case 4:
				return rule.getDownload() ? "Yes" : "No";
			}
			return " ";
		}

		public Object getValue(int nRow, int nCol) {
			if (nRow < 0 || nRow >= getRowCount())
				return "";
			Rule rule = (Rule) mRules.get(nRow);
			switch (nCol) {
			case 0:
				return rule.getCriteria();
			case 1:
				return rule.getComparison();
			case 2:
				return rule.getValue();
			case 3:
				if (rule.getTiVo().equals(Rule.ANY_TIVO))
					return "Any";
				else
					return rule.getTiVo();
			case 4:
				return new Boolean(rule.getDownload());
			}
			return " ";
		}

		public void setValueAt(Object value, int row, int col) {
			Rule rule = null;
			if (row < mRules.size())
				rule = (Rule) mRules.get(row);

			if (rule == null) {
				rule = new Rule();
				setRuleProperty(col, value, rule);
				mRules.add(row, rule);
			} else {
				setRuleProperty(col, value, rule);
			}
			fireTableDataChanged();
		}

		private void setRuleProperty(int col, Object value, Rule rule) {
			switch (col) {
			case 0:
				rule.setCriteria((String) value);
				break;
			case 1:
				rule.setComparison((String) value);
				break;
			case 2:
				rule.setValue((String) value);
				break;
			case 3:
				rule.setTiVo((String) value);
				break;
			case 4:
				rule.setDownload(((Boolean) value).booleanValue());
				break;
			default:
				break;
			}
		}

		public void removeRow(int row) {
			mRules.remove(row);
			fireTableDataChanged();
		}

		class ColumnListener extends MouseAdapter {
			protected JTable mTable;

			public ColumnListener(JTable table) {
				mTable = table;
			}

			public void mouseClicked(MouseEvent e) {
				TableColumnModel colModel = mTable.getColumnModel();
				int columnModelIndex = colModel.getColumnIndexAtX(e.getX());
				int modelIndex = colModel.getColumn(columnModelIndex)
						.getModelIndex();

				if (modelIndex < 0)
					return;
				if (mSortCol == modelIndex)
					mSortAsc = !mSortAsc;
				else
					mSortCol = modelIndex;

				for (int i = 0; i < mColumns.length; i++) {
					TableColumn column = colModel.getColumn(i);
					column
							.setHeaderValue(getColumnName(column
									.getModelIndex()));
					column.setPreferredWidth(mColumns[i].mWidth);
				}
				mTable.getTableHeader().repaint();

				Collections.sort(mRules, new RuleComparator(modelIndex,
						mSortAsc));
				mTable.tableChanged(new TableModelEvent(ShowTableData.this));
				mTable.repaint();
				if (mTable.getModel().getRowCount() > 0)
					mTable.setRowSelectionInterval(0, 0);
			}
		}

		class RuleComparator implements Comparator {
			protected int mSortCol;

			protected boolean mSortAsc;

			public RuleComparator(int sortCol, boolean sortAsc) {
				mSortCol = sortCol;
				mSortAsc = sortAsc;
			}

			public int compare(Object o1, Object o2) {
				Rule rule1 = (Rule) o1;
				Rule rule2 = (Rule) o2;
				int result = 0;
				double d1, d2;
				switch (mSortCol) {
				case 0:
					result = rule1.getCriteria().compareTo(rule2.getCriteria());
					break;
				case 1:
					result = rule1.getComparison().compareTo(
							rule2.getComparison());
					break;
				case 2:
					result = rule1.getValue().compareTo(rule2.getValue());
					break;
				case 3:
					result = rule1.getTiVo().compareTo(rule2.getTiVo());
					break;
				case 4:
					Boolean download1 = new Boolean(rule1.getDownload());
					Boolean download2 = new Boolean(rule2.getDownload());
					result = download1.toString().compareTo(
							download2.toString());
					break;
				}

				if (!mSortAsc)
					result = -result;
				return result;
			}
		}
	}

	class CustomTableCellRender extends DefaultTableCellRenderer {
		public CustomTableCellRender() {
			super();
		}

		public Component getTableCellRendererComponent(JTable table,
				Object value, boolean isSelected, boolean hasFocus, int row,
				int column) {

			super.getTableCellRendererComponent(table, value, isSelected,
					hasFocus, row, column);

			if (mFont == null) {
				mFont = table.getFont().deriveFont(Font.BOLD,
						table.getFont().getSize());
			}

			setHorizontalAlignment(mColumns[column].mAlignment);
			// setFont(mFont);

			return this;
		}

		private Font mFont;
	}

	private JTable mTable;

	private JComboBox mCriteriaField;

	private JComboBox mComparisonField;

	private JTextField mValueField;

	private JComboBox mTiVoField;

	private JCheckBox mDownloadField;

	private JButton mAddButton;

	private JButton mModifyButton;

	private JButton mDeleteButton;

	private boolean mUpdating;

	private List mRules;

	private RecordedPanel mRecordedPanel;
}
