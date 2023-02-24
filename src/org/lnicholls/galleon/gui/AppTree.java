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

import java.awt.Component;
import java.awt.Dimension;

import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import org.apache.log4j.Logger;

final class AppTree extends JTree implements TreeSelectionListener {

    private static Logger log = Logger.getLogger(AppTree.class.getName());

    public AppTree(MainFrame main, DefaultTreeModel model) {
        super(model);
        mMainFrame = main;
        putClientProperty("JTree.lineStyle", "None");
        setRootVisible(false);
        setShowsRootHandles(true);
        setCellRenderer(new TreeCellRenderer());
        getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        addTreeSelectionListener(this);

        DefaultMutableTreeNode root = (DefaultMutableTreeNode) model.getRoot();
        if (root.getChildCount() > 0) {
            DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode) root.getChildAt(0);
            if (treeNode != null)
                setSelectionPath(new TreePath(treeNode.getPath()));
        }
    }

    public void valueChanged(TreeSelectionEvent e) {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) getLastSelectedPathComponent();

        if (node != null) {
            Object userObject = node.getUserObject();
            if (userObject != null && userObject instanceof AppNode) {
                AppNode appNode = (AppNode) userObject;
                mMainFrame.handleAppSelection(appNode);
            }
        }
    }

    public void addApp(AppNode appNode) {
        DefaultTreeModel treeModel = (DefaultTreeModel) getModel();
        DefaultMutableTreeNode root = (DefaultMutableTreeNode) getModel().getRoot();
        DefaultMutableTreeNode node = new DefaultMutableTreeNode(appNode);
        treeModel.insertNodeInto(node, root, root.getChildCount());
        treeModel.nodeStructureChanged(root);
        scrollPathToVisible(new TreePath(node.getPath()));
        clearSelection();
        addSelectionPath(new TreePath(node.getPath()));
    }

    public void removeApp(AppNode appNode) {
        DefaultTreeModel model = (DefaultTreeModel) getModel();
        DefaultMutableTreeNode root = (DefaultMutableTreeNode) model.getRoot();
        int count = root.getChildCount();
        for (int i = 0; i < count; i++) {
            DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode) root.getChildAt(i);
            AppNode nodeApp = (AppNode) treeNode.getUserObject();
            if (appNode.getAppContext().getClass().equals(nodeApp.getAppContext().getClass())
                    && appNode.getTitle().equals(nodeApp.getTitle())) {
                root.remove(treeNode);
                model.nodeStructureChanged(root);
                if (root.getChildCount() > 0) {
                    treeNode = (DefaultMutableTreeNode) root.getChildAt(0);
                    scrollPathToVisible(new TreePath(treeNode.getPath()));
                    clearSelection();
                    addSelectionPath(new TreePath(treeNode.getPath()));
                }
                return;
            }
        }
    }

    public void refresh() {
        DefaultTreeModel model = (DefaultTreeModel) getModel();
        DefaultMutableTreeNode root = (DefaultMutableTreeNode) model.getRoot();
        TreePath path = getSelectionPath();
        if (path == null) {
            if (root.getChildCount() > 0) {
                DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode) root.getChildAt(0);
                if (treeNode != null)
                    path = new TreePath(treeNode.getPath());
            }
        }
        model.nodeStructureChanged(root);
        setSelectionPath(path);
    }

    public void updateUI() {
        super.updateUI();
        setSelectionPath(getSelectionPath());
        setCellRenderer(new TreeCellRenderer());
    }

    private static class TreeCellRenderer extends DefaultTreeCellRenderer {

        public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded,
                boolean leaf, int row, boolean focus) {
            super.getTreeCellRendererComponent(tree, value, selected, expanded, false, row, focus);
            Object userObject = ((DefaultMutableTreeNode) value).getUserObject();
            if (userObject != null && userObject instanceof AppNode) {
                AppNode appNode = (AppNode) userObject;
                setForeground(sel ? getTextSelectionColor() : getTextNonSelectionColor());
                selected = sel;
                setIcon(appNode.getIcon());
                setText(appNode.getTitle());
                setPreferredSize(new Dimension(150, 20));
            } else
                return super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, focus);
            return this;
        }

        private TreeCellRenderer() {
        }

    }

    private MainFrame mMainFrame;
}