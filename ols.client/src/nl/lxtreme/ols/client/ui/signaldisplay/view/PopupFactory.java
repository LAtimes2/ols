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
 * Copyright (C) 2010-2012 J.W. Janssen, www.lxtreme.nl
 */
package nl.lxtreme.ols.client.ui.signaldisplay.view;


import java.awt.*;

import javax.swing.*;

import nl.lxtreme.ols.client.ui.*;
import nl.lxtreme.ols.client.ui.action.*;
import nl.lxtreme.ols.client.ui.action.manager.*;
import nl.lxtreme.ols.client.ui.signaldisplay.*;
import nl.lxtreme.ols.client.ui.signaldisplay.marker.*;
import nl.lxtreme.ols.client.ui.signaldisplay.signalelement.*;
import nl.lxtreme.ols.client.ui.signaldisplay.signalelement.SignalElement.SignalElementType;


/**
 * Provides a helper class for handling popups in the various views.
 */
public final class PopupFactory
{
  // VARIABLES

  private final SignalDiagramController controller;

  // CONSTRUCTORS

  /**
   * Creates a new {@link PopupFactory} instance.
   */
  PopupFactory( final SignalDiagramController aController )
  {
    this.controller = aController;
  }

  // METHODS

  /**
   * Creates the context-sensitive popup menu for channel labels.
   * 
   * @param aRelativePoint
   *          the current mouse location to show the popup menu, cannot be
   *          <code>null</code>.
   * @param aLocationOnScreen
   *          the location on screen, cannot be <code>null</code>.
   * @return a popup menu, can be <code>null</code> if the given mouse point is
   *         not above a channel.
   */
  public JPopupMenu createChannelLabelPopup( final Point aRelativePoint, final Point aLocationOnScreen )
  {
    return createPopup( aRelativePoint, aLocationOnScreen, null, false /* aShowMarkers */);
  }

  /**
   * Creates the context-sensitive popup menu for markers.
   * 
   * @param aMarker
   *          the optional hovered marker to create the popup for, can be
   *          <code>null</code>;
   * @param aPoint
   *          the current mouse location to show the marker, cannot be
   *          <code>null</code>;
   * @param aLocationOnScreen
   *          the location on screen, cannot be <code>null</code>.
   * @return a popup menu, never <code>null</code>.
   */
  public JPopupMenu createCursorPopup( final Marker aMarker, final Point aPoint, final Point aLocationOnScreen )
  {
    return createPopup( aPoint, aLocationOnScreen, aMarker, true /* aShowMarkers */);
  }

  /**
   * @param aMenu
   *          the popup menu to add the marker actions to;
   * @param aMarker
   *          the current marker, can be <code>null</code> if not hovering above
   *          a marker;
   * @param aPoint
   *          the current mouse location, cannot be <code>null</code>.
   */
  private void addMarkerActions( final JPopupMenu aMenu, final Marker aMarker, final Point aPoint )
  {
    if ( aMarker != null )
    {
      // Hovering above existing marker, show properties & remove items...
      aMenu.add( new EditMarkerPropertiesAction( aMarker ) );

      if ( aMarker.isMoveable() )
      {
        // Only cursors can be removed; so don't bother to show this option for
        // trigger(s)...
        aMenu.addSeparator();

        aMenu.add( new DeleteMarkerAction( aMarker ) );
      }
    }
    else
    {
      final SignalDiagramModel model = this.controller.getSignalDiagramModel();

      // Not hovering above existing marker, show add menu...
      final Marker[] markers = model.getMoveableMarkers();
      for ( Marker marker : markers )
      {
        final SetMarkerAction action = new SetMarkerAction( marker );
        aMenu.add( new JCheckBoxMenuItem( action ) );
      }

      aMenu.putClientProperty( SetMarkerAction.KEY, getMarkerDropPoint( aPoint ) );
    }
  }

  /**
   * @param aPoint
   * @param aLocationOnScreen
   * @param aHoveredCursor
   * @param aShowCursorSection
   * @return
   */
  private JPopupMenu createPopup( final Point aPoint, final Point aLocationOnScreen, final Marker aHoveredCursor,
      final boolean aShowCursorSection )
  {
    ActionManager actionMgr = Client.getInstance().getActionManager();

    final JPopupMenu contextMenu = new JPopupMenu();

    // when an action is selected, we *no* longer know where the point was
    // where the user clicked. Therefore, we need to store it separately
    // for later use...
    contextMenu.putClientProperty( "mouseLocation", aPoint );

    boolean elementAdded = false;

    if ( aHoveredCursor == null )
    {
      final SignalElement signalElement = findSignalElement( aPoint );
      if ( signalElement == null )
      {
        // Not above a cursor, nor above a signal element...
        return null;
      }

      if ( signalElement.isSignalGroup() )
      {
        contextMenu.add( new SetSignalGroupVisibilityAction( this.controller, signalElement,
            SignalElementType.DIGITAL_SIGNAL ) );
        contextMenu.add( new SetSignalGroupVisibilityAction( this.controller, signalElement,
            SignalElementType.GROUP_SUMMARY ) );
        contextMenu.add( new SetSignalGroupVisibilityAction( this.controller, signalElement,
            SignalElementType.ANALOG_SIGNAL ) );
      }
      else
      {
        contextMenu.add( new EditSignalElementPropertiesAction( this.controller, signalElement, aLocationOnScreen ) );

        contextMenu.addSeparator();

        contextMenu.add( new SetSignalElementVisibilityAction( this.controller, signalElement ) );
      }

      if ( signalElement.isDigitalSignal() )
      {
        contextMenu.add( new RemoveChannelAnnotations( this.controller, signalElement ) );
      }

      elementAdded = true;
    }

    if ( aShowCursorSection )
    {
      if ( elementAdded )
      {
        contextMenu.addSeparator();
      }

      addMarkerActions( contextMenu, aHoveredCursor, aPoint );
    }

    contextMenu.addSeparator();

    contextMenu.add( actionMgr.getAction( DeleteAllCursorsAction.ID ) );

    return contextMenu;
  }

  /**
   * Finds the channel under the given point.
   * 
   * @param aPoint
   *          the coordinate of the potential channel, cannot be
   *          <code>null</code>.
   * @return the channel index, or -1 if not found.
   */
  private SignalElement findSignalElement( final Point aPoint )
  {
    return getModel().findSignalElement( aPoint );
  }

  /**
   * Calculates the drop point for the marker under the given coordinate.
   * 
   * @param aCoordinate
   *          the coordinate to return the marker drop point for, cannot be
   *          <code>null</code>.
   * @return a drop point, never <code>null</code>.
   */
  private Long getMarkerDropPoint( final Point aCoordinate )
  {
    int result = aCoordinate.x;

    if ( getModel().isSnapCursorMode() )
    {
      final MeasurementInfo signalHover = getModel().getSignalHover( aCoordinate );
      if ( ( signalHover != null ) && !signalHover.isEmpty() )
      {
        result = signalHover.getMidSamplePos().intValue();
      }
    }

    long markerLocation = getModel().locationToTimestamp( new Point( result, 0 ) ); // XXX
    if ( markerLocation < 0L )
    {
      return null;
    }

    return Long.valueOf( markerLocation );
  }

  /**
   * @return the signal diagram model, never <code>null</code>.
   */
  private SignalDiagramModel getModel()
  {
    return this.controller.getSignalDiagramModel();
  }
}