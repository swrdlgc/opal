/*******************************************************************************
 * Copyright (c) 2011 Laurent CARON All rights reserved. This program and the accompanying materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Laurent CARON (laurent.caron at gmail dot com) - Initial implementation and API Eugene Ryzhikov - Author of the Oxbow Project (http://code.google.com/p/oxbow/) - Inspiration
 *******************************************************************************/
package org.mihalis.opal.opalDialog;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.mihalis.opal.utils.ResourceManager;
import org.mihalis.opal.utils.SWTGraphicUtil;

/**
 * Instances of this class are dialog box
 */
public class Dialog {

	/**
	 * Types of opal dialog
	 */
	public enum OpalDialogType {
		CLOSE, YES_NO, OK, OK_CANCEL, SELECT_CANCEL, NO_BUTTON, OTHER, NONE
	}

	public enum CenterOption {
		CENTER_ON_SCREEN, CENTER_ON_DIALOG
	}

	private CenterOption centerPolicy = CenterOption.CENTER_ON_SCREEN;

	private String title;
	OpalDialogType buttonType;
	private final MessageArea messageArea;
	private final FooterArea footerArea;
	final Shell shell;

	private int minimumWidth = 300;
	private int minimumHeight = 150;

	/**
	 * Constructor
	 */
	public Dialog() {
		this(null);
	}

	/**
	 * Constructor
	 * 
	 * @param resizable if <code>true</code>, the window is resizable
	 */
	public Dialog(final boolean resizable) {
		this(null, resizable);
	}

	/**
	 * Constructor
	 * 
	 * @param parent parent shell
	 */
	public Dialog(final Shell parent) {
		this(parent, false);
	}

	/**
	 * Constructor
	 * 
	 * @param parent parent shell
	 * @param resizable if <code>true</code>, the window is resizable
	 */
	public Dialog(final Shell parent, final boolean resizable) {
		if (parent == null) {
			this.shell = new Shell(Display.getCurrent(), SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL | (resizable ? SWT.RESIZE : SWT.NONE));
		} else {
			this.shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL | (resizable ? SWT.RESIZE : SWT.NONE));
		}
		this.messageArea = new MessageArea(this);
		this.footerArea = new FooterArea(this);
	}

	/**
	 * Show the dialog box
	 * 
	 * @return the index of the selected button
	 */
	public int show() {
		final GridLayout gd = new GridLayout(1, true);
		gd.horizontalSpacing = 0;
		gd.verticalSpacing = 0;
		gd.marginHeight = gd.marginWidth = 0;
		this.shell.setLayout(gd);

		this.messageArea.render();
		this.footerArea.render();
		if (this.title != null) {
			this.shell.setText(this.title);
		}
		pack();
		this.shell.open();

		final Display display = this.shell.getDisplay();
		while (!this.shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}

		return this.footerArea.getSelectedButton();
	}

	/**
	 * Close the dialog box
	 */
	public void close() {
		this.shell.dispose();

	}

	/**
	 * Compute the size of the shell
	 */
	void pack() {

		final Point preferredSize = this.shell.computeSize(SWT.DEFAULT, SWT.DEFAULT);

		if (preferredSize.x < this.minimumWidth) {
			preferredSize.x = this.minimumWidth;
		}

		if (preferredSize.y < this.minimumHeight) {
			preferredSize.y = this.minimumHeight;
		}

		final int centerX;
		final int centerY;

		if (this.centerPolicy == CenterOption.CENTER_ON_SCREEN || this.shell.getParent() == null) {
			final Rectangle monitorBounds = SWTGraphicUtil.getBoundsOfMonitorThatRuns(this.shell);
			centerX = (monitorBounds.width - preferredSize.x) / 2;
			centerY = (monitorBounds.height - preferredSize.y) / 2;
		} else {
			final Shell parent = (Shell) this.shell.getParent();
			centerX = parent.getLocation().x + (parent.getSize().x - preferredSize.x) / 2;
			centerY = parent.getLocation().y + (parent.getSize().y - preferredSize.y) / 2;
		}

		this.shell.setBounds(centerX, centerY, preferredSize.x, preferredSize.y);
	}

	// ------------------------------------------- Convenient methods

	/**
	 * Create a dialog box that asks a question
	 * 
	 * @param title title of the dialog box
	 * @param text text of the question
	 * @param defaultValue default value of the input
	 * @return the value typed by the user
	 */
	public static String ask(final String title, final String text, final String defaultValue) {
		return ask(null, title, text, defaultValue);
	}

	/**
	 * Create a dialog box that asks a question
	 * 
	 * @shell parent shell
	 * @param title title of the dialog box
	 * @param text text of the question
	 * @param defaultValue default value of the input
	 * @return the value typed by the user
	 */
	public static String ask(final Shell shell, final String title, final String text, final String defaultValue) {
		final Dialog dialog = new Dialog(shell);
		dialog.setTitle(ResourceManager.getLabel(ResourceManager.INPUT));
		dialog.getMessageArea().setTitle(title).setText(text).setIcon(Display.getCurrent().getSystemImage(SWT.ICON_INFORMATION)).addTextBox(defaultValue);
		dialog.setButtonType(OpalDialogType.OK_CANCEL);
		if (dialog.show() == 0) {
			return dialog.getMessageArea().getTextBoxValue();
		} else {
			return null;
		}
	}

	/**
	 * Create a dialog box that displays an error message
	 * 
	 * @param title title of the dialog box
	 * @param errorMessage Error message
	 */
	public static void error(final String title, final String errorMessage) {
		error(null, title, errorMessage);
	}

	/**
	 * Create a dialog box that displays an error message
	 * 
	 * @param shell parent shell
	 * @param title title of the dialog box
	 * @param errorMessage Error message
	 */
	public static void error(final Shell shell, final String title, final String errorMessage) {
		final Dialog dialog = new Dialog(shell);
		dialog.setTitle(ResourceManager.getLabel(ResourceManager.APPLICATION_ERROR));
		dialog.getMessageArea().setTitle(title).//
				setText(errorMessage).//
				setIcon(Display.getCurrent().getSystemImage(SWT.ICON_ERROR));
		dialog.setButtonType(OpalDialogType.OK);
		dialog.show();
	}

	/**
	 * Create a dialog box that inform the user
	 * 
	 * @param title title of the dialog box
	 * @param text text to display
	 */
	public static void inform(final String title, final String text) {
		inform(null, title, text);
	}

	/**
	 * Create a dialog box that inform the user
	 * 
	 * @param shell parent shell
	 * @param title title of the dialog box
	 * @param text text to display
	 */
	public static void inform(final Shell shell, final String title, final String text) {
		final Dialog dialog = new Dialog(shell);
		dialog.setTitle(ResourceManager.getLabel(ResourceManager.INFORMATION));
		dialog.getMessageArea().setTitle(title).setText(text).setIcon(Display.getCurrent().getSystemImage(SWT.ICON_INFORMATION));
		dialog.setButtonType(OpalDialogType.CLOSE);
		dialog.show();

	}

	/**
	 * Create a dialog box that asks the user a confirmation
	 * 
	 * @param title title of the dialog box
	 * @param text text to display
	 * @return <code>true</code> if the user confirmed, <code>false</code> otherwise
	 */
	public static boolean isConfirmed(final String title, final String text) {
		return isConfirmed(null, title, text, -1);
	}

	/**
	 * Create a dialog box that asks the user a confirmation
	 * 
	 * @param shell parent shell
	 * @param title title of the dialog box
	 * @param text text to display
	 * @return <code>true</code> if the user confirmed, <code>false</code> otherwise
	 */
	public static boolean isConfirmed(final Shell shell, final String title, final String text) {
		return isConfirmed(shell, title, text, -1);
	}

	/**
	 * Create a dialog box that asks the user a confirmation. The button "yes" is not enabled before timer seconds
	 * 
	 * @param title title of the dialog box
	 * @param text text to display
	 * @param timer number of seconds before enabling the yes button
	 * @return <code>true</code> if the user confirmed, <code>false</code> otherwise
	 */
	public static boolean isConfirmed(final String title, final String text, final int timer) {
		return isConfirmed(null, title, text, timer);
	}

	/**
	 * Create a dialog box that asks the user a confirmation. The button "yes" is not enabled before timer seconds
	 * 
	 * @param shell parent shell
	 * @param title title of the dialog box
	 * @param text text to display
	 * @param timer number of seconds before enabling the yes button
	 * @return <code>true</code> if the user confirmed, <code>false</code> otherwise
	 */
	public static boolean isConfirmed(final Shell shell, final String title, final String text, final int timer) {
		final Dialog dialog = new Dialog(shell);
		dialog.setTitle(ResourceManager.getLabel(ResourceManager.WARNING));
		dialog.getMessageArea().setTitle(title).setText(text).setIcon(Display.getCurrent().getSystemImage(SWT.ICON_WARNING));

		dialog.getFooterArea().setTimer(timer).setTimerIndexButton(0);
		dialog.setButtonType(OpalDialogType.YES_NO);
		return dialog.show() == 0;
	}

	/**
	 * Create a dialog box with a radio choice
	 * 
	 * @param title title of the dialog box
	 * @param text text to display
	 * @param defaultSelection index of the default selection
	 * @param values values to display
	 * @return the index of the selection
	 */
	public static int radioChoice(final String title, final String text, final int defaultSelection, final String... values) {
		return radioChoice(null, title, text, defaultSelection, values);
	}

	/**
	 * Create a dialog box with a radio choice
	 * 
	 * @param shell parent shell
	 * @param title title of the dialog box
	 * @param text text to display
	 * @param defaultSelection index of the default selection
	 * @param values values to display
	 * @return the index of the selection
	 */
	public static int radioChoice(final Shell shell, final String title, final String text, final int defaultSelection, final String... values) {
		final Dialog dialog = new Dialog(shell);
		dialog.setTitle(ResourceManager.getLabel(ResourceManager.CHOICE));
		dialog.getMessageArea().setTitle(title).setText(text).setIcon(Display.getCurrent().getSystemImage(SWT.ICON_QUESTION)).addRadioButtons(defaultSelection, values);
		dialog.setButtonType(OpalDialogType.SELECT_CANCEL);
		if (dialog.show() == 0) {
			return dialog.getMessageArea().getRadioChoice();
		} else {
			return -1;
		}
	}

	/**
	 * Display a dialog box with an exception
	 * 
	 * @param exception exception to display
	 */
	public static void showException(final Throwable exception) {
		final Dialog dialog = new Dialog();
		dialog.setTitle(ResourceManager.getLabel(ResourceManager.EXCEPTION));

		final String msg = exception.getMessage();
		final String className = exception.getClass().getName();
		final boolean noMessage = msg == null || msg.trim().length() == 0;

		dialog.getMessageArea().setTitle(noMessage ? className : msg).//
				setText(noMessage ? "" : className).//
				setIcon(Display.getCurrent().getSystemImage(SWT.ICON_ERROR)).//
				setException(exception);

		dialog.getFooterArea().setExpanded(true);

		dialog.setButtonType(OpalDialogType.CLOSE);
		dialog.show();
	}

	/**
	 * Create a dialog box with a choice
	 * 
	 * @param title title of the dialog box
	 * @param text text to display
	 * @param defaultSelection index of the default selection
	 * @param items items to display
	 * @return the index of the selected value
	 */
	public static int choice(final String title, final String text, final int defaultSelection, final ChoiceItem... items) {
		return choice(null, title, text, defaultSelection, items);
	}

	/**
	 * Create a dialog box with a choice
	 * 
	 * @param shell parent shell
	 * @param title title of the dialog box
	 * @param text text to display
	 * @param defaultSelection index of the default selection
	 * @param items items to display
	 * @return the index of the selected value
	 */
	public static int choice(final Shell shell, final String title, final String text, final int defaultSelection, final ChoiceItem... items) {
		final Dialog dialog = new Dialog(shell);
		dialog.setTitle(ResourceManager.getLabel(ResourceManager.CHOICE));
		dialog.getMessageArea().setTitle(title).setText(text).setIcon(Display.getCurrent().getSystemImage(SWT.ICON_QUESTION)).addChoice(defaultSelection, items);
		dialog.setButtonType(OpalDialogType.NONE);
		dialog.show();
		return dialog.getMessageArea().getChoice();
	}

	// ------------------------------------------- Getters & Setters

	/**
	 * @return the title
	 */
	public String getTitle() {
		return this.title;
	}

	/**
	 * @param title the title to set
	 */
	public void setTitle(final String title) {
		this.title = title;
	}

	/**
	 * @return the buttonType
	 */
	public OpalDialogType getButtonType() {
		return this.buttonType;
	}

	/**
	 * @param buttonType the buttonType to set
	 */
	public void setButtonType(final OpalDialogType buttonType) {
		this.buttonType = buttonType;

		switch (buttonType) {
			case CLOSE:
				this.footerArea.setButtonLabels(ResourceManager.getLabel(ResourceManager.CLOSE)).setDefaultButtonIndex(0);
				break;
			case NO_BUTTON:
				break;
			case OK:
				this.footerArea.setButtonLabels(ResourceManager.getLabel(ResourceManager.OK)).setDefaultButtonIndex(0);
				break;
			case OK_CANCEL:
				this.footerArea.setButtonLabels(ResourceManager.getLabel(ResourceManager.OK), ResourceManager.getLabel(ResourceManager.CANCEL)).setDefaultButtonIndex(-1);
				break;
			case SELECT_CANCEL:
				this.footerArea.setButtonLabels(ResourceManager.getLabel(ResourceManager.SELECT), ResourceManager.getLabel(ResourceManager.CANCEL)).setDefaultButtonIndex(-1);
				break;
			case YES_NO:
				this.footerArea.setButtonLabels(ResourceManager.getLabel(ResourceManager.YES), ResourceManager.getLabel(ResourceManager.NO)).setDefaultButtonIndex(0);
				break;
			default:
				break;
		}

	}

	/**
	 * @return the messageArea
	 */
	public MessageArea getMessageArea() {
		return this.messageArea;
	}

	/**
	 * @return the footerArea
	 */
	public FooterArea getFooterArea() {
		return this.footerArea;
	}

	/**
	 * @return the shell
	 */
	public Shell getShell() {
		return this.shell;
	}

	/**
	 * @return the index of the selected button
	 */
	public int getSelectedButton() {
		return getFooterArea().getSelectedButton();
	}

	/**
	 * @return the selection state of the checkbox
	 */
	public boolean getCheckboxValue() {
		return this.footerArea.getCheckBoxValue();
	}

	/**
	 * @return the minimum width of the dialog box
	 */
	public int getMinimumWidth() {
		return this.minimumWidth;
	}

	/**
	 * @param minimumWidth the minimum width of the dialog box to set
	 */
	public void setMinimumWidth(final int minimumWidth) {
		this.minimumWidth = minimumWidth;
	}

	/**
	 * @return the minimum height of the dialog box
	 */
	public int getMinimumHeight() {
		return this.minimumHeight;
	}

	/**
	 * @param minimumHeight the minimum height of the dialog box to set
	 */
	public void setMinimumHeight(final int minimumHeight) {
		this.minimumHeight = minimumHeight;
	}

	/**
	 * @return the center policy (Dialog centered on screen or centered in the center of the parent window)
	 */
	public CenterOption getCenterPolicy() {
		return this.centerPolicy;
	}

	/**
	 * @param centerPolicy center policy
	 */
	public void setCenterPolicy(final CenterOption centerPolicy) {
		this.centerPolicy = centerPolicy;
	}

}
