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
import java.awt.Cursor;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Collections;
import java.util.Comparator;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
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
import org.lnicholls.galleon.database.Video;
import org.lnicholls.galleon.gui.RecordedPanel.ShowComparator;
import org.lnicholls.galleon.gui.RecordedPanel.ShowTableData;
import org.lnicholls.galleon.server.*;
import org.lnicholls.galleon.util.*;
import org.lnicholls.galleon.server.*;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.factories.ButtonBarFactory;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

public class TiVoPanel extends JPanel implements ActionListener, KeyListener {

    private static Logger log = Logger.getLogger(TiVoPanel.class.getName());

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

    private static final ColumnData mColumns[] = { new ColumnData("Name", 50, JLabel.LEFT),
            new ColumnData("IP Address", 50, JLabel.LEFT), new ColumnData("Capacity", 5, JLabel.RIGHT), new ColumnData("Space", 5, JLabel.RIGHT) };

    public TiVoPanel() {
        super();

        setLayout(new BorderLayout());

        mTiVos = Galleon.getTiVos();
        if (log.isDebugEnabled())
            log.debug("mTiVos=" + mTiVos);

        final ShowTableData showsTableData = new ShowTableData();
        mTable = new JTable();
        mTable.setDefaultRenderer(Object.class, new CustomTableCellRender());

        mNameField = new JTextField(30);
        mNameField.setToolTipText("Enter the name of your TiVo");
        mNameField.addKeyListener(this);
        mAddressField = new JTextField(30);
        mAddressField.setToolTipText("Enter IP address of your TiVo");
        mAddressField.addKeyListener(this);
        
        mCapacityField = new JTextField(5);
        mCapacityField.setToolTipText("Enter storage capacity of TiVo in GB");
        mCapacityField.addKeyListener(this);

        FormLayout layout = new FormLayout("left:pref, 3dlu, left:pref, right:pref:grow ", "pref, " + //name
                "3dlu, " + "pref " + //address
                "3dlu, " + "pref " //capacity                
        );

        PanelBuilder builder = new PanelBuilder(layout);
        builder.setDefaultDialogBorder();

        CellConstraints cc = new CellConstraints();

        builder.addLabel("Name", cc.xy(1, 1));
        builder.add(mNameField, cc.xy(3, 1));
        builder.addLabel("TiVo IP Address", cc.xy(1, 3));
        builder.add(mAddressField, cc.xy(3, 3));
        builder.addLabel("Capacity (GB)", cc.xy(1, 5));
        builder.add(mCapacityField, cc.xy(3, 5));

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
        for (int i = 0; i < 3; i++) {
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
                //Ignore extra messages.
                if (e.getValueIsAdjusting())
                    return;

                ListSelectionModel lsm = (ListSelectionModel) e.getSource();
                if (!lsm.isSelectionEmpty()) {
                    if (!mUpdating) {
                        int selectedRow = lsm.getMinSelectionIndex();
                        ShowTableData model = (ShowTableData) mTable.getModel();
                        mNameField.setText((String) model.getValue(selectedRow, 0));
                        mAddressField.setText((String) model.getValue(selectedRow, 1));
                        mCapacityField.setText((String) model.getValue(selectedRow, 2));
                    }

                    checkButtonStates();
                    mDeleteButton.setEnabled(true);
                } else {
                    if (!mUpdating) {
                        mNameField.setText(" ");
                        mAddressField.setText(" ");
                        mCapacityField.setText(" ");
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

    public void keyTyped(KeyEvent e) {
    }

    public void keyPressed(KeyEvent e) {
    }

    public void keyReleased(KeyEvent e) {
        checkButtonStates();
    }

    public void checkButtonStates() {
        boolean filled = mNameField.getText().trim().length() != 0 && mAddressField.getText().trim().length() != 0;

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
                model.setValueAt(mNameField.getText(), nextRow, 0);
                model.setValueAt(mAddressField.getText(), nextRow, 1);
                model.setValueAt(mCapacityField.getText(), nextRow, 2);
            } catch (Exception ex) {
                Tools.logException(TiVoPanel.class, ex);
            }
            Galleon.updateTiVos(mTiVos);
            mUpdating = false;
        } else if ("modify".equals(e.getActionCommand())) {
            mUpdating = true;
            try {
                TableModel model = mTable.getModel();
                int selectedRow = mTable.getSelectedRow();
                if (selectedRow != -1) {
                    // Why do I need to do this...?
                    Object[] values = new Object[4];
                    values[0] = mNameField.getText();
                    values[1] = mAddressField.getText();
                    values[2] = mCapacityField.getText();

                    for (int i = 0; i < values.length; i++) {
                        model.setValueAt(values[i], selectedRow, i);
                        log.info(values[i]);
                    }
                    mTable.addRowSelectionInterval(selectedRow, selectedRow);

                    Galleon.updateTiVos(mTiVos);
                }
            } catch (Exception ex) {
                Tools.logException(TiVoPanel.class, ex);
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
                Galleon.updateTiVos(mTiVos);
            } catch (Exception ex) {
                Tools.logException(TiVoPanel.class, ex);
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
            return mTiVos == null ? 0 : mTiVos.size();
        }

        public int getColumnCount() {
            return 4;
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
            TiVo tivo = (TiVo) mTiVos.get(nRow);
            switch (nCol) {
            case 0:
                return tivo.getName();
            case 1:
                return tivo.getAddress();
            case 2:
                return String.valueOf(tivo.getCapacity());                
            case 3:
                return String.valueOf(calculateSpace(tivo));
            }
            return " ";
        }

        public Object getValue(int nRow, int nCol) {
            if (nRow < 0 || nRow >= getRowCount())
                return "";
            TiVo tivo = (TiVo) mTiVos.get(nRow);
            switch (nCol) {
            case 0:
                return tivo.getName();
            case 1:
                return tivo.getAddress();
            case 2:
                return String.valueOf(tivo.getCapacity());
            case 3:
                return String.valueOf(calculateSpace(tivo));                
            }
            return " ";
        }

        public void setValueAt(Object value, int row, int col) {
            TiVo tivo = null;
            if (row < mTiVos.size())
                tivo = (TiVo) mTiVos.get(row);

            if (tivo == null) {
                tivo = new TiVo();
                setTiVoProperty(col, value, tivo);
                mTiVos.add(row, tivo);
            } else {
                setTiVoProperty(col, value, tivo);
            }
            fireTableDataChanged();
        }

        private void setTiVoProperty(int col, Object value, TiVo tivo) {
            switch (col) {
            case 0:
                tivo.setName((String) value);
                break;
            case 1:
                tivo.setAddress((String) value);
                break;
            case 2:
                try
                {
                    tivo.setCapacity(Integer.parseInt((String) value));
                }
                catch (NumberFormatException ex){}
                break;                
            default:
                break;
            }
        }

        public void removeRow(int row) {
            mTiVos.remove(row);
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
                int modelIndex = colModel.getColumn(columnModelIndex).getModelIndex();

                if (modelIndex < 0)
                    return;
                if (mSortCol == modelIndex)
                    mSortAsc = !mSortAsc;
                else
                    mSortCol = modelIndex;

                for (int i = 0; i < mColumns.length; i++) {
                    TableColumn column = colModel.getColumn(i);
                    column.setHeaderValue(getColumnName(column.getModelIndex()));
                    column.setPreferredWidth(mColumns[i].mWidth);
                }
                mTable.getTableHeader().repaint();

                Collections.sort(mTiVos, new TiVoComparator(modelIndex, mSortAsc));
                mTable.tableChanged(new TableModelEvent(ShowTableData.this));
                mTable.repaint();
                if (mTable.getModel().getRowCount() > 0)
                    mTable.setRowSelectionInterval(0, 0);
            }
        }

        class TiVoComparator implements Comparator {
            protected int mSortCol;

            protected boolean mSortAsc;

            public TiVoComparator(int sortCol, boolean sortAsc) {
                mSortCol = sortCol;
                mSortAsc = sortAsc;
            }

            public int compare(Object o1, Object o2) {
                TiVo tivo1 = (TiVo) o1;
                TiVo tivo2 = (TiVo) o2;
                int result = 0;
                double d1, d2;
                switch (mSortCol) {
                case 0:
                    result = tivo1.getName().compareTo(tivo2.getName());
                    break;
                case 1:
                    result = tivo1.getAddress().compareTo(tivo2.getAddress());
                    break;
                case 2:
                    if (tivo1.getCapacity() == tivo2.getCapacity())
                    	return 0;
                    else
                	if (tivo1.getCapacity() < tivo2.getCapacity())
                    	return -1;
                	else
                		return 1;
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

        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                boolean hasFocus, int row, int column) {

            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

            if (mFont == null) {
                mFont = table.getFont().deriveFont(Font.BOLD, table.getFont().getSize());
            }

            setHorizontalAlignment(mColumns[column].mAlignment);
            //setFont(mFont);

            return this;
        }

        private Font mFont;
    }

    public void activate() {
        if (mTiVos.size() == 0) {
            JOptionPane
                    .showMessageDialog(
                            this,
                            "Galleon could not automatically find any TiVo's on your network. Add your TiVo information manually for ToGo downloading to work.",
                            "Error", JOptionPane.ERROR_MESSAGE);
        }
        
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        
        if (mRecorded!=null)
        	mRecorded.clear();
        new Thread() {
            public void run()
            {
                while (true)
                {
                    try
                    {
                        mTiVos = Galleon.getTiVos();
                        mRecorded = Galleon.getRecordings();
                        if (mRecorded==null || TiVoPanel.this.mRecorded.size()==0)
                            sleep(1000*5);
                        else
                        {
                        	ArrayList shows = new ArrayList();
                        	
                        	List recordings = Galleon.getRecordings();
                            Iterator iterator = recordings.iterator();
                            while (iterator.hasNext())
                            {
                                Video video = (Video)iterator.next();
                                if (video.getStatus()==Video.STATUS_RECORDING)
	                            {
                                	
	                            }
                                else
                                if (video.getStatus()==Video.STATUS_DOWNLOADED)
	                            {
	                        		/*
	                        		if (Galleon.isFileExists(video.getPath()))
	                        		{
	                                	shows.add(video);
	                        		}
	                        		*/
	                            }
	                        	else
	                        	{
	                        		if (video.getStatus()==Video.STATUS_DELETED)
	                        		{
	                        			if (video.getAvailability()!=null && video.getAvailability().intValue()==Video.RECORDING_AVAILABLE)
	                        				shows.add(video);
	                        		}
	                        		else
	                        			shows.add(video);
	                        	}
                            }
                            mRecorded = shows;
                            ShowTableData model = (ShowTableData) mTable.getModel();
                            model.fireTableDataChanged();
                            if (model.getRowCount() > 0)
                                mTable.setRowSelectionInterval(0, 0);
                            setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                            return;
                        }
                    }
                    catch (Exception ex)
                    {
                    	Tools.logException(RecordedPanel.class, ex); 
                    	return;
                    }
                }
            }
        }.start();
    }
    
    private int calculateSpace(TiVo tivo)
    {
    	if (mRecorded!=null)
    	{
    		long size = 0;
    		Iterator iterator = mRecorded.iterator();
	        while (iterator.hasNext())
	        {
	            Video video = (Video)iterator.next();
	            size = size + video.getSize();
	        }
	        return (int)(((tivo.getCapacity() * 1024) - size / (1024 * 1024)) / 1024);
    	}
    	
    	return 0;
    }

    private JTable mTable;

    private JTextField mNameField;

    private JTextField mAddressField;
    
    private JTextField mCapacityField;

    private JButton mAddButton;

    private JButton mModifyButton;

    private JButton mDeleteButton;

    private boolean mUpdating;

    private List mTiVos;
    
    private List mRecorded;
}
