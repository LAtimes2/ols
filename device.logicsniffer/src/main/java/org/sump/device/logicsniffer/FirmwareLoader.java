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

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;

import javax.swing.SwingWorker;

/**
 * Provides an about box dialog.
 */
public class FirmwareLoader
{
  public static class ProcessWorker extends SwingWorker<Void, String>
  {
    private FirmwareDialog firmwareDialog;
    private String command;

    public ProcessWorker(String command, FirmwareDialog firmwareDialog)
    {
        this.command = command;
        this.firmwareDialog = firmwareDialog;
    }

    @Override
    protected Void doInBackground() throws Exception
    {
      String line;

      publish( command + "\n" );
//      Thread.sleep(1000);

      Process p = Runtime.getRuntime().exec( command );

      // Read Process Stream Output
      final BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));

      // while command is executing and user has not stopped it
      while ((line = input.readLine()) != null && firmwareDialog.isVisible())
      {
        publish( line + "\n" );
      }
      input.close();

      // if dialog was closed, stop the download
      if (!firmwareDialog.isVisible())
      {
        p.destroyForcibly();
        System.out.println("stopping load (if possible)");
      }
      publish( "<Command complete>\n" );

      // wait for user to read the text
      Thread.sleep(10000);

      return null;
    }

    @Override
    protected void done()
    {
      this.firmwareDialog.close();
    }

    @Override
    protected void process( List<String> text)
    {
      for (int index = 0; index < text.size(); ++index)
      {
        this.firmwareDialog.addText( text.get( index ) );
      }
    }
}

  // VARIABLES
  
  ProcessWorker aProcessWorker;

  // CONSTRUCTORS

  /**
   * Creates a new AboutBox instance.
   */
  public FirmwareLoader()
  {
  }

  // METHODS

  /**
   * Cancels loading firmware
   */
  public final void cancel()
  {
    if (aProcessWorker != null)
    {
      // kill the process
      aProcessWorker.cancel( true );
    }
  }

  /**
   * Starts loading firmware
   */
  public final void start( String command, FirmwareDialog firmwareDialog ) {
    aProcessWorker = new ProcessWorker( command, firmwareDialog );
    aProcessWorker.execute();
  }

}
