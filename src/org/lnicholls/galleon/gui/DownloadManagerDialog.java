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

import java.awt.*;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.WindowEvent;
import java.awt.event.*;
import java.net.URL;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
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

import org.apache.log4j.Logger;
import org.lnicholls.galleon.database.Video;
import org.lnicholls.galleon.gui.MainFrame.ServerDialog.NameValueWrapper;
import org.lnicholls.galleon.gui.RecordedPanel.CustomTableCellRender;
import org.lnicholls.galleon.gui.RecordedPanel.ShowTableData;
import org.lnicholls.galleon.server.DownloadConfiguration;
import org.lnicholls.galleon.server.ServerConfiguration;
import org.lnicholls.galleon.util.NameValue;
import org.lnicholls.galleon.util.Tools;
import org.lnicholls.galleon.downloads.*;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.factories.ButtonBarFactory;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.stanford.ejalbert.BrowserLauncher;

public class DownloadManagerDialog extends JDialog implements ActionListener {
	
	private static Logger log = Logger.getLogger(DownloadManagerDialog.class.getName());
	
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

    private static final ColumnData mColumns[] = { new ColumnData(" ", 5, JLabel.CENTER),
            new ColumnData("Location", 200, JLabel.LEFT), new ColumnData("Status", 100, JLabel.CENTER),
            new ColumnData("KB/s", 40, JLabel.RIGHT), new ColumnData("Elapsed Time", 40, JLabel.RIGHT) };
    
    class NameValueWrapper extends NameValue {
		public NameValueWrapper(String name, String value) {
			super(name, value);
		}

		public String toString() {
			return getName();
		}
	}    

	public DownloadManagerDialog(MainFrame frame, ServerConfiguration serverConfiguration) {
		super(frame, "Download Manager", true);
		mMainFrame = frame;
		
		mServerConfiguration = serverConfiguration;
		
		final DownloadConfiguration downloadConfiguration = mServerConfiguration.getDownloadConfiguration();
		
		mDownloads = new ArrayList();
		
		mCPUCombo = new JComboBox();
		mCPUCombo.addItem(new NameValueWrapper("Max", "10"));
		mCPUCombo.addItem(new NameValueWrapper("Normal", "5"));
		mCPUCombo.addItem(new NameValueWrapper("Min", "1"));
		defaultCombo(mCPUCombo, Integer.toString(downloadConfiguration.getCPU()));
		
		mBandwidthCombo = new JComboBox();
		mBandwidthCombo.addItem(new NameValueWrapper("Max", "10"));
		mBandwidthCombo.addItem(new NameValueWrapper("Normal", "5"));
		mBandwidthCombo.addItem(new NameValueWrapper("Min", "1"));
		defaultCombo(mBandwidthCombo, Integer.toString(downloadConfiguration.getBandwidth()));
		
        FormLayout layout = new FormLayout("left:pref, 3dlu, left:pref:grow", "pref, " + 
                "3dlu, " + "pref " 
        );

        PanelBuilder builder = new PanelBuilder(layout);
        builder.setDefaultDialogBorder();

        CellConstraints cc = new CellConstraints();

        builder.addLabel("CPU", cc.xy(1, 1));
		builder.add(mCPUCombo, cc.xy(3, 1));
		builder.addLabel("Bandwidth", cc.xy(1, 3));
		builder.add(mBandwidthCombo, cc.xy(3, 3));

		getContentPane().add(builder.getPanel(), BorderLayout.NORTH);		
		
		final ShowTableData downloadsTableData = new ShowTableData();
        mTable = new JTable();
		
		JPanel tablePanel = new JPanel();
        tablePanel.setLayout(new BorderLayout());
        
        JButton[] array = new JButton[1];
        JPanel buttonPanel = new JPanel();
        mDeleteButton = new JButton("Delete");
        array[0] = mDeleteButton;
        buttonPanel.add(mDeleteButton);
        mDeleteButton.setActionCommand("delete");
        mDeleteButton.addActionListener(this);
        mDeleteButton.setEnabled(false);
        JPanel buttons = ButtonBarFactory.buildCenteredBar(array);
        buttons.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        tablePanel.add(buttons, BorderLayout.NORTH);

        mTable.setModel(downloadsTableData);
        TableColumn column = null;
        for (int i = 0; i < 4; i++) {
            column = mTable.getColumnModel().getColumn(i);
            column.setPreferredWidth(mColumns[i].mWidth);
        }
        mTable.setDragEnabled(true);
        mTable.setRowSelectionAllowed(true);
        //mTable.setCellSelectionEnabled(false);
        mTable.getColumnModel().getColumn(2).setCellRenderer( new ProgressBarCellRenderer() );
        mTable.getColumnModel().getColumn(3).setCellRenderer( new TextCellRenderer(JLabel.RIGHT) );
        mTable.getColumnModel().getColumn(4).setCellRenderer( new TextCellRenderer(JLabel.RIGHT) );
        mTable.setColumnSelectionAllowed(false);
        ListSelectionModel selectionModel = mTable.getSelectionModel();
        selectionModel.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        selectionModel.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                //Ignore extra messages.
                if (e.getValueIsAdjusting())
                {
                    return;
                }
                
                ListSelectionModel lsm = (ListSelectionModel) e.getSource();
                if (!lsm.isSelectionEmpty()) {
                    if (!mUpdating) {
                        int selectedRow = lsm.getMinSelectionIndex();
                        //mTitleField.setText((String) mTable.getModel().getValueAt(selectedRow, 1));
                    }
                    mDeleteButton.setEnabled(true);
                } else {
                    if (!mUpdating) {
                        //mTitleField.setText(" ");
                    }
                    mDeleteButton.setEnabled(false);
                }
            }
        });
        if (mTable.getModel().getRowCount() > 0)
            mTable.setRowSelectionInterval(0, 0);

        JTableHeader header = mTable.getTableHeader();
        header.setUpdateTableInRealTime(true);
        header.addMouseListener(downloadsTableData.new ColumnListener(mTable));
        header.setReorderingAllowed(true);

        JScrollPane scrollPane = new JScrollPane();
        scrollPane.getViewport().add(mTable);
        tablePanel.add(scrollPane, BorderLayout.CENTER);

		getContentPane().add(tablePanel, "Center");

		array = new JButton[3];
		array[0] = new JButton("OK");
		array[0].setActionCommand("ok");
		array[0].addActionListener(this);
		array[1] = new JButton("Cancel");
		array[1].setActionCommand("cancel");
		array[1].addActionListener(this);
		array[2] = new JButton("Help");
		array[2].setActionCommand("help");
		array[2].addActionListener(this);
		buttons = ButtonBarFactory.buildCenteredBar(array);

		buttons.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		getContentPane().add(buttons, "South");
		
		setSize(650, 400);
		setLocationRelativeTo(frame);
		
		addWindowListener(new WindowAdapter(){
			public void windowClosed(WindowEvent event)
			{
				if (mRefreshThread!=null && mRefreshThread.isAlive())
					mRefreshThread.interrupt();
				mRefreshThread = null;
			}
		});
		
		refresh();
	}
	
	public void defaultCombo(JComboBox combo, String value) {
		for (int i = 0; i < combo.getItemCount(); i++) {
			if (((NameValue) combo.getItemAt(i)).getValue().equals(value)) {
				combo.setSelectedIndex(i);
				return;
			}
		}
	}

	public void actionPerformed(ActionEvent e) {
		if ("ok".equals(e.getActionCommand())) {
			this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
			DownloadConfiguration downloadConfiguration = new DownloadConfiguration();
			try {
				downloadConfiguration.setCPU(Integer.parseInt(((NameValue) mCPUCombo.getSelectedItem())
							.getValue()));
				downloadConfiguration.setBandwidth(Integer.parseInt(((NameValue) mBandwidthCombo.getSelectedItem())
						.getValue()));
				Galleon.updateDownloadConfiguration(downloadConfiguration);
			} catch (Throwable ex) {
				Tools.logException(MainFrame.class, ex, "Could not configure download manager");
				
				JOptionPane.showMessageDialog(
						this,
						Tools.getCause(ex),
						"Error", JOptionPane.ERROR_MESSAGE);
				return;
			}
			finally
			{
				this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));	
			}
		} else if ("help".equals(e.getActionCommand())) {
            this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            try {
				BrowserLauncher.openURL("http://galleon.tv/content/view/97/45/");
			} catch (Exception ex) {
			}
            this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
            return;
        } 
		else
		if ("delete".equals(e.getActionCommand())) {
            if (JOptionPane.showConfirmDialog(this, "Are you sure you want to delete these download(s) ?", "Warning",
                    JOptionPane.WARNING_MESSAGE | JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                mUpdating = true;
                try {
                    ShowTableData model = (ShowTableData) mTable.getModel();
                    int[] selectedRows = mTable.getSelectedRows();
                    if (selectedRows.length > 0) {
                        for (int i = 0; i < selectedRows.length; i++) {
                        	Download download = (Download) mDownloads.get(selectedRows[i]);
                            Galleon.stopDownload(download);
                            model.removeRow(selectedRows[i]);
                        }
                    }
                } catch (Exception ex) {
                    Tools.logException(RulesPanel.class, ex);
                }
                mUpdating = false;
                refresh();
            }
            return;
        }			
		
		this.setVisible(false);
	}
	
    class ShowTableData extends AbstractTableModel {

        protected int mSortCol = 1;

        protected boolean mSortAsc = true;

        protected NumberFormat mNumberFormat = NumberFormat.getInstance();

        public ShowTableData() {
            mNumberFormat = NumberFormat.getInstance();
            mNumberFormat.setMaximumFractionDigits(2);
        }

        public int getRowCount() {
            return mDownloads == null ? 0 : mDownloads.size();
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

        public Class getColumnClass(int c) {
            return getValueAt(0, c).getClass();
        }

        public boolean isCellEditable(int nRow, int nCol) {
            if (nCol == 0)
            {
                Download download = (Download) mDownloads.get(nRow);
                return download.getStatus()!=ThreadStatus.COMPLETED.getID();  // completed
            }
            else
                return false;
        }

        public Object getValueAt(int nRow, int nCol) {
            if (nRow < 0 || nRow >= getRowCount())
                return "";
            Download download = (Download) mDownloads.get(nRow);
            switch (nCol) {
            case 0:
                return new Boolean(download.getStatus()>=ThreadStatus.IN_PROGRESS.getID() && download.getStatus()!=ThreadStatus.PAUSED.getID());
            case 1:
                return download.getURL()==null?"":download.getURL().toString();
            case 2:
            	if ( download.getStatus()>=ThreadStatus.IN_PROGRESS.getID() ) {
            		long tb = download.getBytesCompleted();
            		long ts = download.getSize();
            		return new Integer((int) (tb*100/ts));
            	}
            	return ThreadStatus.getStatus(download.getStatus()).getDescription();            	
            case 3:
            	int t = download.getElapsedTime();
            	long b = download.getBytesCompleted();
                long s = download.getSize();
            	return (t==0? "0" : mNumberFormat.format( b/(t*1000.0)) )+" KB/s";
            case 4:
            	t = download.getElapsedTime();
            	int h = t/3600;
                int m = (t-h*3600)/60;
                int sc = t-h*3600-m*60;
            	return h+":"+m+":"+sc;
            }
            return " ";
        }

        public void setValueAt(Object value, final int row, int col) {
            Download download = null;
            if (row < mDownloads.size())
                download = (Download) mDownloads.get(row);

            if (col == 0) {
                javax.swing.SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        mTable.setRowSelectionInterval(0, row);
                    }
                });
                
                Boolean status = (Boolean)value;
                if (status.booleanValue())
                {
                	try
                	{
                		Galleon.resumeDownload(download);
                	}
                	catch (Exception ex)
                	{
                		Tools.logException(DownloadManagerDialog.class, ex, "Could not resume download: "+download.getURL());		
                	}
                	refresh();
                }
                else 
                {
                	try
                	{
                		Galleon.pauseDownload(download);
                	}
                	catch (Exception ex)
                	{
                		Tools.logException(DownloadManagerDialog.class, ex, "Could not resume download: "+download.getURL());		
                	}
                	refresh();
                }
            }
            fireTableDataChanged();
        }

        public void removeRow(int row) {
            mDownloads.remove(row);
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

                Collections.sort(mDownloads, new DownloadComparator(modelIndex, mSortAsc));
                mTable.tableChanged(new TableModelEvent(ShowTableData.this));
                mTable.repaint();
                if (mTable.getModel().getRowCount() > 0)
                    mTable.setRowSelectionInterval(0, 0);
            }
        }

    }
    
    class DownloadComparator implements Comparator {
        protected int mSortCol;

        protected boolean mSortAsc;

        public DownloadComparator(int sortCol, boolean sortAsc) {
            mSortCol = sortCol;
            mSortAsc = sortAsc;
        }

        public int compare(Object o1, Object o2) {
            Download download1 = (Download) o1;
            Download download2 = (Download) o2;
            int result = 0;
            double d1, d2;
            switch (mSortCol) {
            case 0:
                result = download1.getURL().toString().compareTo(download2.getURL().toString());
                break;
            case 1:
            	result = download1.getURL().toString().compareTo(download2.getURL().toString());
                break;
            case 2:
            	result = ThreadStatus.getStatus(download1.getStatus()).getDescription().compareTo(ThreadStatus.getStatus(download2.getStatus()).getDescription()); 
                break;
            case 3:
                if (download1.getBytesCompleted()>download2.getBytesCompleted())
                	return 1;
                else
            	if (download1.getBytesCompleted()<download2.getBytesCompleted())
                	return -1;
                else        
                	return 0;
            case 4:
            	if (download1.getElapsedTime()>download2.getElapsedTime())
                	return 1;
                else
            	if (download1.getElapsedTime()<download2.getElapsedTime())
                	return -1;
                else        
                	return 0;
            }

            if (!mSortAsc)
                result = -result;
            return result;
        }
    }
    
    public class TextCellRenderer extends javax.swing.table.DefaultTableCellRenderer {
        
        int a = JLabel.LEFT;
        
        /** Creates a new instance of TextCellRenderer */
        public TextCellRenderer() {
        }

        public TextCellRenderer(int align) {
            
            a = align;
        }
        
        public void setTextAlignment(int a) {
            
            this.a = a;
        }
        
        public java.awt.Component getTableCellRendererComponent(javax.swing.JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            
            JLabel l = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            l.setHorizontalAlignment( a );
            return l;
        }
    }
    
    public class ProgressBarCellRenderer extends javax.swing.JProgressBar implements javax.swing.table.TableCellRenderer {
        
        public ProgressBarCellRenderer() {
        }
        
        public java.awt.Component getTableCellRendererComponent(javax.swing.JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            
            int current = 0;

            try {
                current = ((Integer) value).intValue();
            }
            catch (Exception e) {
                TextCellRenderer tcr = new TextCellRenderer( javax.swing.JLabel.CENTER );

                return tcr.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            }

            setMinimum( 0 );
            setValue( current );
            setMaximum( 100 );

            setStringPainted( true );
            
            return this;
        }
        
        public void validate() {
        }
        
        public void firePropertyChange(String propertyName, boolean oldValue, boolean newValue)  {
        }
        
        protected void firePropertyChange(String propertyName, Object oldValue, Object newValue) {
        }
        
        public boolean isOpaque() {
            return true;
        }
        
        public void repaint(long tm, int x, int y, int width, int height) {
        }
        
        public void repaint(java.awt.Rectangle r) {
        }
        
        public void revalidate() {
        }

    }

    public void refresh() {
        if (mRefreshThread!=null && mRefreshThread.isAlive())
        {
        	mRefreshThread.interrupt();
        }
        
        mRefreshThread = new Thread() {
            public void run()
            {
            	while (true)
            	{
            		try
                    {
            			mDownloads = Galleon.getDownloads();
            			Collections.sort(mDownloads, new DownloadComparator(2, false));
                        ShowTableData model = (ShowTableData) mTable.getModel();
                        model.fireTableDataChanged();
                        if (model.getRowCount() > 0)
                            mTable.setRowSelectionInterval(0, 0);
                        sleep(1000*1);
                    }
                    catch (Exception ex)
                    {
                        return;
                    }
            	}
            }
        };
        mRefreshThread.start();
    }

    private MainFrame mMainFrame;
    
    private JComboBox mCPUCombo;
    
    private JComboBox mBandwidthCombo;
    
    private JTable mTable;

    private boolean mUpdating;

    private List mDownloads;		

	private ServerConfiguration mServerConfiguration;
	
	private Thread mRefreshThread;
	
	private HelpDialog mHelpDialog;
	
	private JButton mDeleteButton;
}