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

import java.io.*;
import java.awt.*;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Iterator;

import javax.swing.*;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.*;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

import org.apache.log4j.Logger;

import org.lnicholls.galleon.server.*;
import org.lnicholls.galleon.util.*;
import org.lnicholls.galleon.togo.*;
import org.lnicholls.galleon.database.*;
import org.lnicholls.galleon.gui.RecordedPanel.ShowComparator;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.factories.ButtonBarFactory;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

public class DownloadedPanel extends JPanel implements ActionListener {

    private static Logger log = Logger.getLogger(DownloadedPanel.class.getName());

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

    private static final ColumnData mColumns[] = { new ColumnData("Title", 180, JLabel.LEFT),
            new ColumnData("Episode", 200, JLabel.LEFT), new ColumnData("Date Recorded", 80, JLabel.RIGHT),
            new ColumnData("Duration", 30, JLabel.RIGHT), new ColumnData("Size", 30, JLabel.RIGHT),
            new ColumnData("Status", 30, JLabel.RIGHT) };

    public DownloadedPanel() {
        super();

        setLayout(new BorderLayout());

        mShows = new ArrayList(); ///

        final ShowTableData showsTableData = new ShowTableData();
        mTable = new JTable();
        mTable.setDefaultRenderer(Object.class, new CustomTableCellRender());

        mTitleField = new JLabel(" ", JLabel.LEADING);
        Font font = mTitleField.getFont().deriveFont(Font.BOLD, mTitleField.getFont().getSize());
        mTitleField.setFont(font);
        mDescriptionField = new JLabel(" ", JLabel.LEADING);
        mDateField = new JLabel(" ", JLabel.LEADING);
        mChannelStationField = new JLabel(" ", JLabel.TRAILING);
        mRatingField = new JLabel(" ", JLabel.LEADING);
        mQualityField = new JLabel(" ", JLabel.TRAILING);

        FormLayout layout = new FormLayout("left:pref, 3dlu, right:pref:grow", "pref, " + //title
                "3dlu, " + "pref, " + //description
                "3dlu, " + "pref, " + //date/channel
                "3dlu, " + "pref " //rating/quality
        );

        PanelBuilder builder = new PanelBuilder(layout);
        builder.setDefaultDialogBorder();

        CellConstraints cc = new CellConstraints();

        builder.add(mTitleField, cc.xyw(1, 1, 3));
        builder.add(mDescriptionField, cc.xyw(1, 3, 3));
        builder.add(mDateField, cc.xy(1, 5));
        builder.add(mChannelStationField, cc.xy(3, 5));
        builder.add(mRatingField, cc.xy(1, 7));
        builder.add(mQualityField, cc.xy(3, 7));

        add(builder.getPanel(), BorderLayout.NORTH);

        JPanel tablePanel = new JPanel();
        tablePanel.setLayout(new BorderLayout());

        JButton[] array = new JButton[2];
        JPanel buttonPanel = new JPanel();
        mDeleteButton = new JButton("Delete");
        array[0] = mDeleteButton;
        mReloadButton = new JButton("Download Again");
        array[1] = mReloadButton;
        buttonPanel.add(mDeleteButton);
        mDeleteButton.setActionCommand("delete");
        mDeleteButton.addActionListener(this);
        mDeleteButton.setEnabled(false);
        buttonPanel.add(mReloadButton);
        mReloadButton.setActionCommand("reload");
        mReloadButton.addActionListener(this);
        mReloadButton.setEnabled(false);
        JPanel buttons = ButtonBarFactory.buildCenteredBar(array);
        buttons.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        tablePanel.add(buttons, BorderLayout.NORTH);

        mTable.setModel(showsTableData);
        TableColumn column = null;
        for (int i = 0; i < 6; i++) {
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
                    int selectedRow = lsm.getMinSelectionIndex();
                    mTitleField.setText((String) mTable.getModel().getValueAt(selectedRow, 0));
                    mDescriptionField.setText((String) mTable.getModel().getValueAt(selectedRow, 6));
                    mDateField.setText((String) mTable.getModel().getValueAt(selectedRow, 2));
                    mChannelStationField.setText((String) mTable.getModel().getValueAt(selectedRow, 7));
                    mRatingField.setText((String) mTable.getModel().getValueAt(selectedRow, 8));
                    mQualityField.setText((String) mTable.getModel().getValueAt(selectedRow, 9));
                    mDeleteButton.setEnabled(true);
                    mReloadButton.setEnabled(true);
                } else {
                    mTitleField.setText(" ");
                    mDescriptionField.setText(" ");
                    mDateField.setText(" ");
                    mChannelStationField.setText(" ");
                    mRatingField.setText(" ");
                    mQualityField.setText(" ");
                    mDeleteButton.setEnabled(false);
                    mReloadButton.setEnabled(false);
                }
            }
        });
        if (mTable.getModel().getRowCount() > 0)
            mTable.setRowSelectionInterval(0, 0);
        else
            mTitleField.setText("No recordings downloaded");

        JTableHeader header = mTable.getTableHeader();
        header.setUpdateTableInRealTime(true);
        header.addMouseListener(showsTableData.new ColumnListener(mTable));
        header.setReorderingAllowed(true);

        JScrollPane scrollPane = new JScrollPane();
        scrollPane.getViewport().add(mTable);
        tablePanel.add(scrollPane, BorderLayout.CENTER);

        add(tablePanel, BorderLayout.CENTER);
    }

    public void actionPerformed(ActionEvent e) {
        if ("delete".equals(e.getActionCommand())) {
            if (JOptionPane.showConfirmDialog(this, "Are you sure you want to permanently delete these recording(s) ?", "Warning",
                    JOptionPane.WARNING_MESSAGE | JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                mUpdating = true;
                try {
                    ShowTableData model = (ShowTableData) mTable.getModel();
                    int[] selectedRows = mTable.getSelectedRows();
                    if (selectedRows.length > 0) {
                        for (int i = 0; i < selectedRows.length; i++) {
                            Video video = (Video) mShows.get(selectedRows[i]);
                            if (Galleon.isFileExists(video.getPath()))
                            	Galleon.deleteFile(video.getPath());
                            video.setPath(null);
                            video.setDownloadSize(0);
                            video.setDownloadTime(0);
                            video.setStatus(Video.STATUS_DELETED);
                            Galleon.updateVideo(video);
                            model.removeRow(selectedRows[i]);
                        }
                    }
                } catch (Exception ex) {
                    Tools.logException(DownloadedPanel.class, ex);
                }
                mUpdating = false;
                activate();
            }
        }
        else
    	if ("reload".equals(e.getActionCommand())) {
            mUpdating = true;
            try {
                ShowTableData model = (ShowTableData) mTable.getModel();
                int[] selectedRows = mTable.getSelectedRows();
                if (selectedRows.length > 0) {
                    for (int i = 0; i < selectedRows.length; i++) {
                        Video video = (Video) mShows.get(selectedRows[i]);
                        if (Galleon.isFileExists(video.getPath()))
                        	Galleon.deleteFile(video.getPath());
                        video.setPath(null);
                        video.setDownloadSize(0);
                        video.setDownloadTime(0);
                        if (video.getAvailability()!=null && video.getAvailability().intValue()==Video.RECORDING_AVAILABLE)
                        	video.setStatus(Video.STATUS_USER_SELECTED);
                        else
                        	video.setStatus(Video.STATUS_DELETED);
                        Galleon.updateVideo(video);
                        model.removeRow(selectedRows[i]);
                    }
                }
            } catch (Exception ex) {
                Tools.logException(DownloadedPanel.class, ex);
            }
            mUpdating = false;
            activate();
        }
    }

    class ShowTableData extends AbstractTableModel {

        protected int mSortCol = 2;

        protected boolean mSortAsc = true;

        protected SimpleDateFormat mDateFormat;

        protected SimpleDateFormat mTimeFormat;

        protected GregorianCalendar mCalendar;

        protected DecimalFormat mNumberFormat;

        public ShowTableData() {
            mDateFormat = new SimpleDateFormat();
            mDateFormat.applyPattern("EEE M/d hh:mm a");
            mTimeFormat = new SimpleDateFormat();
            mTimeFormat.applyPattern("H:mm");
            mCalendar = new GregorianCalendar();
            mNumberFormat = new DecimalFormat("###,###");
        }

        public int getRowCount() {
            return mShows == null ? 0 : mShows.size();
        }

        public int getColumnCount() {
            return 6;
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
            Video video = (Video) mShows.get(nRow);
            switch (nCol) {
            case 0:
                return video.getTitle()!=null?video.getTitle():"";
            case 1:
                return video.getEpisodeTitle()!=null?video.getEpisodeTitle():"";
            case 2:
                // Round off to the closest minutes; TiVo seems to start recordings 2 seconds before the scheduled time
                mCalendar.setTime(video.getDateRecorded());
                mCalendar.set(GregorianCalendar.MINUTE, (mCalendar.get(GregorianCalendar.MINUTE) * 60
                        + mCalendar.get(GregorianCalendar.SECOND) + 30) / 60);
                mCalendar.set(GregorianCalendar.SECOND, 0);
                return mDateFormat.format(mCalendar.getTime());
            case 3:
                // Round off to closest minute
                //int duration = Math.round((show.getDuration()/1000/60+0.5f)/10)*10;
                int duration = Math.round(video.getDuration() / 1000 / 60 + 0.5f);
                mCalendar.setTime(new Date(Math.round((video.getDuration() / 1000 / 60 + 0.5f) / 10) * 10));
                mCalendar.set(GregorianCalendar.HOUR_OF_DAY, duration / 60);
                mCalendar.set(GregorianCalendar.MINUTE, duration % 60);
                mCalendar.set(GregorianCalendar.SECOND, 0);
                return mTimeFormat.format(mCalendar.getTime());
            case 4:
                return mNumberFormat.format(video.getSize() / (1024 * 1024)) + " MB";
            case 5:
                return video.getStatusString();
            case 6:
                return (video.getDescription()!=null && video.getDescription().length() != 0) ? video.getDescription() : " ";
            case 7:
                return video.getChannel()!=null?video.getChannel():""+ " " + video.getStation()!=null?video.getStation():"";
            case 8:
                return (video.getRating()!=null && video.getRating().length() != 0) ? video.getRating() : "No rating";
            case 9:
                return video.getRecordingQuality()!=null?video.getRecordingQuality():"";
            }
            return " ";
        }

        public void setValueAt(Object value, int row, int col) {
            Video show = null;
            if (row < mShows.size())
                show = (Video) mShows.get(row);

            if (show == null) {
                show = new Video();
                mShows.add(row, show);
            }
            fireTableDataChanged();
        }

        public void removeRow(int row) {
            mShows.remove(row);
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

                Collections.sort(mShows, new ShowComparator(modelIndex, mSortAsc));
                mTable.tableChanged(new TableModelEvent(ShowTableData.this));
                mTable.repaint();
                if (mTable.getModel().getRowCount() > 0)
                    mTable.setRowSelectionInterval(0, 0);
            }
        }

    }

    class ShowComparator implements Comparator {
        protected int mSortCol;

        protected boolean mSortAsc;

        public ShowComparator(int sortCol, boolean sortAsc) {
            mSortCol = sortCol;
            mSortAsc = sortAsc;
        }

        public int compare(Object o1, Object o2) {
            Video show1 = (Video) o1;
            Video show2 = (Video) o2;
            int result = 0;
            double d1, d2;
            switch (mSortCol) {
            case 0:
                result = show1.getTitle().compareTo(show2.getTitle());
                break;
            case 1:
                result = show1.getEpisodeTitle().compareTo(show2.getEpisodeTitle());
                break;
            case 2:
                result = show1.getDateRecorded().compareTo(show2.getDateRecorded());
                break;
            case 3:
                Integer duration1 = new Integer(show1.getDuration());
                Integer duration2 = new Integer(show2.getDuration());
                result = duration1.compareTo(duration2);
                break;
            case 4:
                Long size1 = new Long(show1.getSize());
                Long size2 = new Long(show2.getSize());
                result = size1.compareTo(size2);
                break;
            case 5:
                result = show1.getStatusString().compareTo(show2.getStatusString());
                break;
            }

            if (!mSortAsc)
                result = -result;
            return result;
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
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        mShows.clear();
        List recordings = Galleon.getRecordings();
        if (recordings!=null)
        {
	        Iterator iterator = recordings.iterator();
	        while (iterator.hasNext())
	        {
	            Video video = (Video)iterator.next();
	            if (video.getStatus()==Video.STATUS_DOWNLOADED)
	            {
	            	try
	            	{
	            		if (Galleon.isFileExists(video.getPath()))
	            			mShows.add(video);
		            } catch (Exception ex) {
		                Tools.logException(DownloadedPanel.class, ex);
		            }
	            }
	        }
        }
        ShowTableData model = (ShowTableData) mTable.getModel();
        Collections.sort(mShows, new ShowComparator(2, false));
        model.fireTableDataChanged();

        if (model.getRowCount() > 0)
            mTable.setRowSelectionInterval(0, 0);
        setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
    }

    private JTable mTable;

    private JLabel mTitleField;

    private JLabel mDescriptionField;

    private JLabel mDateField;

    private JLabel mChannelStationField;

    private JLabel mRatingField;

    private JLabel mQualityField;

    private ArrayList mShows;

    private boolean mUpdating;

    private JButton mDeleteButton;

    private JButton mReloadButton;
}
