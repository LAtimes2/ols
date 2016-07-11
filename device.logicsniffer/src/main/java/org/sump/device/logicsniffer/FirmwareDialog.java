/*
 * OpenBench LogicSniffer / SUMP project 
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or (at
 * your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin St, Fifth Floor, Boston, MA 02110, USA
 *
 * Copyright (C) 2006-2010 Michael Poppitz, www.sump.org
 * Copyright (C) 2010 J.W. Janssen, www.lxtreme.nl
 */
package org.sump.device.logicsniffer;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.text.DefaultCaret;

import nl.lxtreme.ols.util.swing.StandardActionFactory;
import nl.lxtreme.ols.util.swing.SwingComponentUtils;
import nl.lxtreme.ols.util.swing.StandardActionFactory.CloseAction.Closeable;

/**
 * Provides an about box dialog.
 */
public class FirmwareDialog extends JDialog implements Closeable
{
  // CONSTANTS

  private static final long serialVersionUID = 1L;

  // VARIABLES
  
  final JTextArea textArea;
  
  // CONSTRUCTORS

  /**
   * Creates a new AboutBox instance.
   */
  public FirmwareDialog()
  {
    setTitle( "Firmware Loader" );

    final JComponent buttonPane = SwingComponentUtils.createButtonPane( StandardActionFactory.createCloseButton() );

    final JPanel contentPane = new JPanel( new GridBagLayout() );

    // create a JTextArea
    textArea = new JTextArea(18, 80);

    // always scroll to newest text
    DefaultCaret caret = (DefaultCaret) textArea.getCaret();
    caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);

    textArea.setText("start\n");
    textArea.setEditable(false);
    
    // wrap a scrollpane around it
    JScrollPane scrollPane = new JScrollPane(textArea);

    contentPane.add( scrollPane, //
        new GridBagConstraints( 0, 0, 1, 1, 1.0, 1.0, GridBagConstraints.NORTH, GridBagConstraints.BOTH, //
            new Insets( 0, 0, 5, 0 ), 0, 0 ) );
    contentPane.add( buttonPane, //
        new GridBagConstraints( 0, 1, 1, 1, 1.0, 0.0, GridBagConstraints.SOUTH, GridBagConstraints.HORIZONTAL,
            new Insets( 0, 0, 5, 0 ), 0, 0 ) );

    setContentPane( contentPane );

    pack();

    setLocationRelativeTo( getOwner() );
    setResizable( false );

  }

  // METHODS

  /**
   * Closes this dialog and disposes it.
   */
  public final void close() {
    setVisible(false);
  }

  /**
   * add text to text area
   */
  public void addText( String text ) {
    System.out.print( text );
    textArea.append( text );
  }

  /**
   * clear text area
   */
  public void clearText() {
    textArea.setText("");
  }

  /**
   * @see java.awt.Dialog#show()
   */
  public void showDialog() {
    setVisible(true);
  }
}
