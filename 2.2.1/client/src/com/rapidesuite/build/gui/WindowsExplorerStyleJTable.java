package com.rapidesuite.build.gui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;

import org.springframework.util.Assert;

import com.rapidesuite.client.common.util.FileUtils;

public class WindowsExplorerStyleJTable extends JTable
{
	private static final long serialVersionUID = 2369604009805950335L;

	public WindowsExplorerStyleJTable() {
		super();
		dispatcherSettings();
	}

	public WindowsExplorerStyleJTable(final DefaultTableModel param) {
		super(param);
		dispatcherSettings();
	}

	private void dispatcherSettings() {
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(new KeyEventDispatcher() {
	        @Override
	        public boolean dispatchKeyEvent(KeyEvent e) {
	        	if (KeyStroke.getKeyStroke(KeyEvent.VK_UP, InputEvent.CTRL_MASK).equals(KeyStroke.getAWTKeyStrokeForEvent(e))) { //ctrl+up
	        		return true;
				} else if (KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, InputEvent.CTRL_MASK).equals(KeyStroke.getAWTKeyStrokeForEvent(e))) { //ctrl+down
					return true;
				} else {
					return false;
				}

	        }
        });
	}

	private boolean isDragging = false;
	private Rectangle selectionBox = null;
	private boolean ignoreDefaultSelectionChange = false;
	private Point dragStartPoint = null;

	protected StringBuffer typedCharacters = null;
	private Date lastCharacterTypedTimestamp = null;

	private JScrollPane scrollPane = null;

	private void updateSelectionBox(final boolean isDragging, final Rectangle selectionBox) {
		this.isDragging = isDragging;
		this.selectionBox = selectionBox;
	}

	private void updateSelectionBox(final Rectangle selectionBox) {
		this.selectionBox = selectionBox;
	}

	public void setScrollPane(final JScrollPane scrollPane) {
		this.scrollPane = scrollPane;
	}


    public void resizeColumnByProportion(final TableColumn column, final double proportion) {
        column.setPreferredWidth((int) (this.getPreferredSize().getWidth() * proportion));
        column.setWidth((int) (this.getSize().getWidth() * proportion));
        column.setMaxWidth((int) (this.getMaximumSize().getWidth() * proportion));
    }



	@Override
	public Component prepareRenderer(TableCellRenderer renderer, int rowIndex, final int columnIndex)
	{
		Component comp = super.prepareRenderer(renderer, rowIndex, columnIndex);
		comp.setForeground(Color.black);
		final boolean isSelectedRow = getSelectionModel().isSelectedIndex(rowIndex);
		comp.setBackground(isSelectedRow?new Color(0x99ccff):Color.white); //isSelectedRow?light blue:white
		Assert.isTrue(comp instanceof JComponent, "comp must be instanceof JComponent");
		final JComponent jComp = (JComponent) comp;
		jComp.setBorder(BorderFactory.createEmptyBorder());
		return jComp;
	}

	@Override
	protected void paintComponent(final Graphics g) {
		super.paintComponent(g);
		if (isDragging) {
			Graphics2D g2 = (Graphics2D) g;
			final Stroke s = new BasicStroke(1.5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{1}, 0);
			g2.setStroke(s);
			g2.draw(selectionBox);
		}
	}

	@Override
	public void changeSelection(final int rowIndex, final int columnIndex, final boolean toggle, final boolean extend) {
		if (!ignoreDefaultSelectionChange) {
			super.changeSelection(rowIndex, columnIndex, toggle, extend);
		}

		ignoreDefaultSelectionChange = false;
	}

	protected List<JMenuItem> getRightClickMenuItems(final MouseEvent e) {
		final List<JMenuItem> output = new ArrayList<JMenuItem>();

		final JMenuItem tickSelectedItem = new JMenuItem("Tick");
		tickSelectedItem.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				for (final Integer row : getSelectedRows()) {
					updateRow(true, row);
				}
			}

		});

		output.add(tickSelectedItem);

		final JMenuItem untickSelectedItem = new JMenuItem("UnTick");
		untickSelectedItem.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				for (final Integer row : getSelectedRows()) {
					updateRow(false, row);
				}
			}

		});

		output.add(untickSelectedItem);

		final JMenuItem selectAllItem = new JMenuItem("Select All");
		selectAllItem.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if (getRowCount() > 0) {
					setRowSelectionInterval(0, getRowCount()-1);
				}

			}

		});

		output.add(selectAllItem);

		final JMenuItem selectAboveItem = new JMenuItem("Select Above");
		selectAboveItem.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				onShiftHome();
			}
		});
		output.add(selectAboveItem);

		final JMenuItem selectBelowItem = new JMenuItem("Select Below");
		selectBelowItem.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				onShiftEnd();
			}
		});
		output.add(selectBelowItem);

		return output;
	}

	public class DefaultMouseListener extends MouseAdapter {
		@Override
		public void mousePressed(MouseEvent e)
		{
			dragStartPoint = new Point(e.getX(), e.getY());
			updateSelectionBox(new Rectangle());
			final Point p = e.getPoint();
			final int row = rowAtPoint(p);

			if (e.getClickCount() == 1) {
				if (SwingUtilities.isRightMouseButton(e)) {
					if (!getSelectionModel().isSelectedIndex(row)) {
						clearSelection();
						setRowSelectionInterval(row, row);
					}

					final JPopupMenu onRightClickMenu = new JPopupMenu();
					final List<JMenuItem> menuItems = getRightClickMenuItems(e);
					if (menuItems == null || !menuItems.isEmpty()) {
						for (final JMenuItem menuItem : menuItems) {
							onRightClickMenu.add(menuItem);
						}
					}

					onRightClickMenu.show(e.getComponent(), e.getX(), e.getY());
				} else if (SwingUtilities.isLeftMouseButton(e) && !e.isAltDown() && !e.isControlDown() && !e.isShiftDown()
						&& getSelectedRows().length > 1) {
					getSelectionModel().clearSelection();
					getSelectionModel().setSelectionInterval(row, row);
				}
			}
		}

		@Override
	    public void mouseReleased(MouseEvent e) {
			final Rectangle selectionBox = null;
			if (isDragging && dragStartPoint != null) {
				final int minY = Math.min(e.getY(), dragStartPoint.y);
				final int maxY = Math.max(e.getY(), dragStartPoint.y);

				Integer minIndex = null;
				Integer maxIndex = null;

				for (int i = 0 ; i < getRowCount() ; i++) {
					final Rectangle rect = getCellRect(i, 0, true);

					if (rect.y + rect.height >= minY && rect.y <= maxY) {
						if (minIndex == null) {
							minIndex = i;
						}

						maxIndex = i;
					}
				}

				if (minIndex != null) {
					setRowSelectionInterval(minIndex, maxIndex);
				}
			}
			updateSelectionBox(false, selectionBox);
			dragStartPoint = null;
			repaint();
	    }
	}

	private void updateRow(boolean status, int index)
	{
		final TableModel model = getModel();
		Assert.isTrue(model instanceof DefaultTableModel, "model must be instance of DefaultTableModel");
		final DefaultTableModel defaultTableModel = (DefaultTableModel) model;
		if ( defaultTableModel.isCellEditable(index, 0) )
		{
	        defaultTableModel.setValueAt(status, index, 0);
	        defaultTableModel.fireTableCellUpdated(index, 0);
		}
	}

	public class DefaultMouseMotionListener implements MouseMotionListener {

		@Override
		public void mouseDragged(MouseEvent e) {
			try {
				if (SwingUtilities.isLeftMouseButton(e) && dragStartPoint != null) {
					final boolean isDragging = true;

					final int minX = Math.min(e.getX(), dragStartPoint.x);
					final int maxX = Math.max(e.getX(), dragStartPoint.x);
					final int minY = Math.min(e.getY(), dragStartPoint.y);
					final int maxY = Math.max(e.getY(), dragStartPoint.y);

					final Rectangle selectionBox = new Rectangle(minX, minY, maxX-minX, maxY-minY);

					updateSelectionBox(isDragging, selectionBox);

					if (getRowCount() > 0) {
						final Rectangle topMostRectangle = getCellRect(0, 0, true);
						final int topMostY = topMostRectangle.y;
						final Rectangle bottomMostRectangle = getCellRect(getRowCount()-1, 0, true);
						final int bottomMostY = bottomMostRectangle.y + bottomMostRectangle.height;
						if (e.getPoint().y <= topMostY) {
							scrollRectToVisible(topMostRectangle);
						} else if (e.getPoint().y >= bottomMostY) {
							scrollRectToVisible(bottomMostRectangle);
						} else {
							scrollRectToVisible(getCellRect(rowAtPoint(e.getPoint()), 0, true));
						}
					}

					Integer minIndex = null;
					Integer maxIndex = null;

					for (int i = 0 ; i < getRowCount() ; i++) {
						final Rectangle cellRectangle = getCellRect(i, 0, true);
						if (cellRectangle.y + cellRectangle.height >= minY) {
							if (cellRectangle.y <= maxY) {
								if (minIndex == null) {
									minIndex = i;
								}

								maxIndex = i;
							} else {
								break;
							}
						}
					}

					if (minIndex != null) {
						setRowSelectionInterval(minIndex, maxIndex);
					}

					if (columnAtPoint(dragStartPoint) == 0) {
						editCellAt(rowAtPoint(dragStartPoint), 1);
						editCellAt(rowAtPoint(e.getPoint()), 1);
					}

					ignoreDefaultSelectionChange = true;
				}

				repaint();
			} catch (final Throwable t) {
				t.printStackTrace();
				FileUtils.printStackTrace(t);
			}
		}

		@Override
		public void mouseMoved(MouseEvent e) {
			// TODO Auto-generated method stub

		}

	}

	public class DefaultKeyListener implements KeyListener {

		private final Integer columnIndex;

		public DefaultKeyListener(final int columnIndex) {
			this.columnIndex = columnIndex;
		}

		public DefaultKeyListener() {
			this.columnIndex = null;
		}

		protected boolean isTargetRow(final int rowIndex) {
			if (this.columnIndex == null) {
				throw new UnsupportedOperationException("You have to override this method");
			}
			Assert.isTrue(getValueAt(rowIndex, columnIndex) instanceof String, "The column must be a string column");
			return String.valueOf(getValueAt(rowIndex, columnIndex)).startsWith(typedCharacters.toString());
		}

		@Override
		public void keyTyped(KeyEvent e) {
			final Date now = new Date();
			if (lastCharacterTypedTimestamp == null || now.getTime() - lastCharacterTypedTimestamp.getTime() > 1000) {
				typedCharacters = new StringBuffer();
			}
			typedCharacters.append(e.getKeyChar());
			lastCharacterTypedTimestamp = now;
			for (int i = 0 ; i < getRowCount() ; i++) {
				if (isTargetRow(i)) {
					final Rectangle rect = getCellRect(i, 0, true);
					scrollRectToVisible(rect);
					setRowSelectionInterval(i, i);
					getSelectionModel().setAnchorSelectionIndex(i);
					ignoreDefaultSelectionChange = true;
					editCellAt(i, 1);
					break;
				}
			}
			if (scrollPane != null) {
				scrollPane.repaint();
			}
		}

		@Override
		public void keyPressed(KeyEvent e) {
			if (KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0).equals(KeyStroke.getAWTKeyStrokeForEvent(e))) { //space
				final int numberOfRows = getSelectedRows().length;
				int numberOfTickedCheckboxes = 0;
				int numberOfUntickedCheckboxes = 0;
				for (final int selectedRow : getSelectedRows()) {
					if ((boolean) getModel().getValueAt(selectedRow, 0)) {
						numberOfTickedCheckboxes++;
					} else {
						numberOfUntickedCheckboxes++;
					}
				}
				boolean newValue;
				if (numberOfTickedCheckboxes == numberOfRows) { //all ticked
					newValue = false;
				} else if (numberOfUntickedCheckboxes == numberOfRows) { //all unticked
					newValue = true;
				} else { //some ticked, some unticked
					newValue = true;
				}
				for (final int selectedRow : getSelectedRows()) {
					updateRow(newValue, selectedRow);
				}
				e.consume();
				repaint();
			} else if (KeyStroke.getKeyStroke(KeyEvent.VK_UP, InputEvent.CTRL_MASK+InputEvent.SHIFT_DOWN_MASK).equals(KeyStroke.getAWTKeyStrokeForEvent(e))) { //ctrl+shift+up

				if (getSelectedRows().length > 0) {
					final int previousRow = getSelectionModel().getAnchorSelectionIndex();
					final int newRow = previousRow-1;
					ignoreDefaultSelectionChange = true;
					if (newRow >= 0) {
						if (!getSelectionModel().isSelectedIndex(newRow) && newRow < getSelectionModel().getMinSelectionIndex()) {
							final int maxIndex = getSelectionModel().getMaxSelectionIndex();
							setRowSelectionInterval(newRow, maxIndex);
						}
						getSelectionModel().setAnchorSelectionIndex(newRow);
						scrollRectToVisible(getCellRect(newRow, 0, true));
					}
				}
			} else if (KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, InputEvent.CTRL_MASK+InputEvent.SHIFT_DOWN_MASK).equals(KeyStroke.getAWTKeyStrokeForEvent(e))) { //ctrl+shift+down

				if (getSelectedRows().length > 0) {
					final int previousRow = getSelectionModel().getAnchorSelectionIndex();
					final int newRow = previousRow+1;
					ignoreDefaultSelectionChange = true;
					if (newRow < getRowCount()) {
						if (!getSelectionModel().isSelectedIndex(newRow) && newRow > getSelectionModel().getMaxSelectionIndex()) {

							final int minIndex = getSelectionModel().getMinSelectionIndex();
							setRowSelectionInterval(minIndex, newRow);
						}
						getSelectionModel().setAnchorSelectionIndex(newRow);
						scrollRectToVisible(getCellRect(newRow, 0, true));
					}
				}
			} else if (KeyStroke.getKeyStroke(KeyEvent.VK_HOME, InputEvent.SHIFT_DOWN_MASK).equals(KeyStroke.getAWTKeyStrokeForEvent(e))) { //shift+home
				onShiftHome();
			} else if (KeyStroke.getKeyStroke(KeyEvent.VK_END, InputEvent.SHIFT_DOWN_MASK).equals(KeyStroke.getAWTKeyStrokeForEvent(e))) { //shift+end
				onShiftEnd();
			}

			repaint();
		}

		@Override
		public void keyReleased(KeyEvent e) {
			// TODO Auto-generated method stub

		}
	}

	private void onShiftHome() {
		if (getSelectedRows().length > 0) {
			ignoreDefaultSelectionChange = true;
			final int boundaryIndex = getSelectionModel().getAnchorSelectionIndex();
			setRowSelectionInterval(0, boundaryIndex);
			final Rectangle firstRowRect = getCellRect(0, 0, false);
			scrollRectToVisible(firstRowRect);
			getSelectionModel().setAnchorSelectionIndex(0);
		}
	}

	private void onShiftEnd() {
		if (getSelectedRows().length > 0) {
			ignoreDefaultSelectionChange = true;
			final int boundaryIndex = getSelectionModel().getAnchorSelectionIndex();
			setRowSelectionInterval(boundaryIndex, getRowCount()-1);
			final Rectangle lastRowRect = getCellRect(getRowCount()-1, getRowCount()-1, false);
			scrollRectToVisible(lastRowRect);
			getSelectionModel().setAnchorSelectionIndex(getRowCount()-1);
		}
	}
}
